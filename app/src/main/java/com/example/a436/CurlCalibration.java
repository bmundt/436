package com.example.a436;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class CurlCalibration extends Activity implements  SensorEventListener {

    int whichVal;
    private SensorManager mSensorManager;
    private Sensor proximity;
    private Sensor accelerometer;
    private Sensor magnetometer;
    private TextView distView;
    float distance;
    private final float[] mAccelerometerReading = new float[3];
    private final float[] mMagnetometerReading = new float[3];

    private final float[] mRotationMatrix = new float[9];
    private final float[] mOrientationAngles = new float[3];

    private final int START = 0;
    private final int HALF = 1;
    private final int COMPLETE = 2;

    private final String START_TAG = "START";
    private final String HALF_TAG = "HALF";
    private final String COMPLETE_TAG = "COMPLETE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_curl_calibration);
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        proximity = mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        distView = (TextView) findViewById(R.id.distance);
        whichVal = 0;
    }

    public void calibrate(View v) {
        updateOrientationAngles();
        String text = "Orientation Angles:\n" +
                "\tAngle 1: " + String.valueOf(mOrientationAngles[0]) + "\n" +
                "\tAngle 2: " + String.valueOf(mOrientationAngles[1]) + "\n" +
                "\tAngle 3: " + String.valueOf(mOrientationAngles[2]) + "\n" +
                "Distance: " + String.valueOf(distance);
        distView.setText(text);

        // here we will set the calibration numbers in the shared preferences
        if (whichVal <= COMPLETE) {

            SharedPreferences pref = getApplicationContext().getSharedPreferences(MyApp.PREF_NAME,
                    Context.MODE_PRIVATE);
            // set the values for the different trials
            SharedPreferences.Editor editor = pref.edit();
            String floatTag = "";
            switch (whichVal) {
                case START:
                    floatTag = START_TAG;
                    break;
                case HALF:
                    floatTag = HALF_TAG;
                    break;
                case COMPLETE:
                    floatTag = COMPLETE_TAG;
                    break;
            }

            editor.putFloat(floatTag + "_1", mOrientationAngles[0]);
            editor.putFloat(floatTag + "_2", mOrientationAngles[1]);
            editor.putFloat(floatTag + "_3", mOrientationAngles[2]);
            editor.commit();
            whichVal++;
        } else {
            // go back to actual test, possibly instructions
            Intent intent = new Intent(CurlCalibration.this, CurlActivity.class);
            startActivity(intent);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, accelerometer,
                SensorManager.SENSOR_DELAY_NORMAL, SensorManager.SENSOR_DELAY_UI);
        mSensorManager.registerListener(this, magnetometer,
                SensorManager.SENSOR_DELAY_NORMAL, SensorManager.SENSOR_DELAY_UI);
        mSensorManager.registerListener(this, proximity, SensorManager.SENSOR_DELAY_NORMAL,
                SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        Sensor sensor = event.sensor;
        int type = sensor.getType();
        switch (type) {
            case Sensor.TYPE_PROXIMITY:
                distance = event.values[0];
                break;
            case Sensor.TYPE_ACCELEROMETER:
                System.arraycopy(event.values, 0, mAccelerometerReading,
                        0, mAccelerometerReading.length);
                updateOrientationAngles();
                break;
            case Sensor.TYPE_MAGNETIC_FIELD:
                System.arraycopy(event.values, 0, mMagnetometerReading,
                        0, mMagnetometerReading.length);
                updateOrientationAngles();
                break;
        }
    }

    public void updateOrientationAngles() {
        // Update rotation matrix, which is needed to update orientation angles.
        mSensorManager.getRotationMatrix(mRotationMatrix, null,
                mAccelerometerReading, mMagnetometerReading);

        // "mRotationMatrix" now has up-to-date information.

        mSensorManager.getOrientation(mRotationMatrix, mOrientationAngles);

        // "mOrientationAngles" now has up-to-date information.
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }
}
