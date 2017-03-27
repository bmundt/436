package com.example.a436;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
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
    private Sensor rotation;
//    private Sensor accelerometer;
//    private Sensor magnetometer;
    private TextView distView;
    float distance;
    private final float[] mAccelerometerReading = new float[3];
    private final float[] mMagnetometerReading = new float[3];

    private final float[] mRotationMatrix = new float[9];
    private final float[] mOrientationAngles = new float[3];
    private float xAngle;
    private float yAngle;
    private float zAngle;

    private final int START = 0;
    private final int HALF = 1;
    private final int COMPLETE = 2;

    private final String START_TAG = "START";
    private final String ELBOW_TAG = "ELBOW";
    private final String SHOULDER_TAG = "SHOULDER";
    private final int CLOSE_ENOUGH = 2;

    protected Button calibrate_btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_curl_calibration);
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
//        accelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
//        magnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        proximity = mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        rotation = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        distView = (TextView) findViewById(R.id.distance);
        whichVal = 0;
        calibrate_btn = (Button) findViewById(R.id.calibrate_btn);
    }

    public void calibrate(View v) {
//        updateOrientationAngles();
        String text = "Orientation Angles:\n" +
                "\tX Angle: " + String.valueOf(xAngle) + "\n" +
                "\tY Angle: " + String.valueOf(yAngle) + "\n" +
                "\tZ Angle: " + String.valueOf(zAngle) + "\n" +
                "Distance: " + String.valueOf(distance);
        distView.setText(text);

        Log.d("xAngle", String.valueOf(xAngle));
        Log.d("yAngle", String.valueOf(yAngle));
        Log.d("zAngle", String.valueOf(zAngle));

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
                    floatTag = ELBOW_TAG;
                    break;
                case COMPLETE:
                    floatTag = SHOULDER_TAG;
                    break;
            }

            editor.putFloat(floatTag, xAngle);
            editor.commit();
            if (whichVal == COMPLETE && distance > CLOSE_ENOUGH) {

                // display dialog
                AlertDialog calibrationError = new AlertDialog.Builder(CurlCalibration.this).create();
                calibrationError.setTitle("Calibration Error");
                calibrationError.setMessage("The phone was not close enough to your shoulder during " +
                "the third calibration press. Please recalibrate");
                calibrationError.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                distView.setText(R.string.calibration_instructions);
                                dialog.dismiss();
                            }
                        });
                calibrationError.show();
                whichVal = START;
            } else if (whichVal == COMPLETE && distance <= CLOSE_ENOUGH) {
                distView.setText("Calibration Complete");
                calibrate_btn.setText("GO TO TEST");
                whichVal++;
            } else {
                whichVal++;
            }

        } else {
            // go back to actual test, possibly instructions
            Intent intent = new Intent(CurlCalibration.this, CurlActivity.class);
            startActivity(intent);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
//        mSensorManager.registerListener(this, accelerometer,
//                SensorManager.SENSOR_DELAY_NORMAL, SensorManager.SENSOR_DELAY_UI);
//        mSensorManager.registerListener(this, magnetometer,
//                SensorManager.SENSOR_DELAY_NORMAL, SensorManager.SENSOR_DELAY_UI);
        mSensorManager.registerListener(this, rotation, SensorManager.SENSOR_DELAY_NORMAL,
                SensorManager.SENSOR_DELAY_UI);
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
            case Sensor.TYPE_ROTATION_VECTOR:
                xAngle = event.values[0];
                yAngle = event.values[1];
                zAngle = event.values[2];
                break;
//            case Sensor.TYPE_ACCELEROMETER:
//                System.arraycopy(event.values, 0, mAccelerometerReading,
//                        0, mAccelerometerReading.length);
//                updateOrientationAngles();
//                break;
//            case Sensor.TYPE_MAGNETIC_FIELD:
//                System.arraycopy(event.values, 0, mMagnetometerReading,
//                        0, mMagnetometerReading.length);
//                updateOrientationAngles();
//                break;
        }
    }

//    public void updateOrientationAngles() {
//        // Update rotation matrix, which is needed to update orientation angles.
//        mSensorManager.getRotationMatrix(mRotationMatrix, null,
//                mAccelerometerReading, mMagnetometerReading);
//
//        // "mRotationMatrix" now has up-to-date information.
//
//        mSensorManager.getOrientation(mRotationMatrix, mOrientationAngles);
//
//        // "mOrientationAngles" now has up-to-date information.
//    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }
}
