package com.example.a436;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.SystemClock;
import android.os.Bundle;
import android.view.View;
import android.widget.Chronometer;
import android.widget.TextView;

public class CurlActivity extends Activity implements SensorEventListener {

    private SensorManager mSensorManager;
    private Sensor proximity;
    private Sensor accelerometer;
    private Sensor magnetometer;
    private TextView instructions;
    private TextView curlCount;
    float distance;
    private final float[] mAccelerometerReading = new float[3];
    private final float[] mMagnetometerReading = new float[3];

    private final float[] mRotationMatrix = new float[9];
    private final float[] mOrientationAngles = new float[3];

    private final float[] START_ORIENTATION = new float[3];
    private final float[] HALF_ORIENTATION = new float[3];
    private final float[] COMPLETE_ORIENTATION = new float[3];

    private final String START_TAG = "START";
    private final String HALF_TAG = "HALF";
    private final String COMPLETE_TAG = "COMPLETE";

    private Chronometer chron;
    private boolean half = false;
    private boolean complete = false;
    private boolean backAtHalf = false;
    private int reps = 0;
    private final int MAX_REPS = 10;
    private final float margin = .1F;
    private long totalTime;


    /* Tested Angle Values
     START
     1: -.501
     2: -0.094
     3: -0.056

     HALF
     1: -0.762
     2: -0.062
     3: 1.896

     COMPLETE
     1: 0.510
     2: -0.059
     3: -3.112


     margin of .1

     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_curl);
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        proximity = mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        curlCount = (TextView) findViewById(R.id.curlCount);
        chron = new Chronometer(getApplicationContext());
    }

    /*
        For when you want to force calibration
     */
    public void calibrate(View v) {
        Intent intent = new Intent(CurlActivity.this, CurlCalibration.class);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // check here for calibration values and send to calibrate if missing
        SharedPreferences pref = getApplicationContext().getSharedPreferences(MyApp.PREF_NAME,
                Context.MODE_PRIVATE);
        float startVal = Float.MAX_VALUE;
        float halfVal = Float.MAX_VALUE;
        float completeVal = Float.MAX_VALUE;
        int actualVal = 0;
        for (int i = 0; i < START_ORIENTATION.length; i++) {
            actualVal = i + 1;
            startVal = pref.getFloat(START_TAG + "_" + actualVal, Float.MAX_VALUE);
            halfVal = pref.getFloat(START_TAG + "_" + actualVal, Float.MAX_VALUE);
            completeVal = pref.getFloat(START_TAG + "_" + actualVal, Float.MAX_VALUE);

            if (startVal == Float.MAX_VALUE || halfVal == Float.MAX_VALUE ||
                    completeVal == Float.MAX_VALUE) {
                // start the calibration activity
                Intent intent = new Intent(CurlActivity.this, CurlCalibration.class);
                startActivity(intent);
            } else {
                START_ORIENTATION[i] = startVal;
                HALF_ORIENTATION[i] = halfVal;
                COMPLETE_ORIENTATION[i] = completeVal;
            }
        }



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

        // orientation is fixed at this point
        // so check to see where in a rep we are, if we complete then we
        // increase rep count and reset variables
        if (reps < MAX_REPS) {
            if (half) {
                // at least half way
                if (complete) {

                    if (backAtHalf) {
                        // check for backAtStart, if so then reset and increment reps
                        if (compareAngles(mOrientationAngles, START_ORIENTATION)) {
                            reps++;
                            backAtHalf = false;
                            complete = false;
                            half = false;
                            curlCount.setText("Curl Count: "+ reps);
                        }
                    } else {
                        if (compareAngles(mOrientationAngles, HALF_ORIENTATION))
                            backAtHalf = true;
                    }
                } else {
                    // check for complete
                    if (compareAngles(mOrientationAngles, COMPLETE_ORIENTATION) &&
                            distance - margin == 0.0)
                        complete = true;
                }
            } else {
                // not half way, so check if we are
                if (compareAngles(mOrientationAngles, HALF_ORIENTATION))
                    half = true;
            }
        } else {
            chron.stop();
            totalTime = SystemClock.elapsedRealtime() - chron.getBase();
            curlCount.setText("Curl Count: 10\nTime: " + totalTime + "milliseconds");
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

    public void startTimer(View v) {
        chron.start();
    }

    private boolean compareAngles(float[] floats1, float[] floats2) {
        float val1 = Math.abs(floats1[0] - floats2[0]);
        float val2 = Math.abs(floats1[1] - floats2[1]);
        float val3 = Math.abs(floats1[2] - floats2[2]);

        if (val1 <= margin && val2 <= margin && val3 <= margin) {
            return true;
        } else {
            return false;
        }
    }
}
