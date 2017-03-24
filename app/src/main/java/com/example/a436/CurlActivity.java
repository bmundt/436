package com.example.a436;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Chronometer;
import android.widget.TextView;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class CurlActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager mSensorManager;
    private Sensor proximity;
//    private Sensor accelerometer;
//    private Sensor magnetometer;
    private Sensor rotation;
//    private Sensor linear_accelerometer;
    private TextView instructions;
    private TextView curlCount;
    float distance;

    private final float[] mRotationMatrix = new float[9];
    private final float[] mOrientationAngles = new float[3];

    private float START_ORIENTATION = Float.MAX_VALUE;
    private float ELBOW_ORIENTATION = Float.MAX_VALUE;
    private float SHOULDER_ORIENTATION = Float.MAX_VALUE;

    private final String START_TAG = "START";
    private final String ELBOW_TAG = "ELBOW";
    private final String SHOULDER_TAG = "SHOULDER";

    private Chronometer chron;
    private boolean elbow;
    private boolean shoulder;
    private boolean backAtElbow;
    private boolean isStarted;
    private int reps;
    private final int MAX_REPS = 10;
    private final float margin = .3F;
    private long totalTime;


    // get accelerometer force until 0.0
    // then track it again backwards


    /* Tested Angle Values
     START
     3: -0.056

     HALF
     3: 1.896

     COMPLETE
     3: -3.112


     margin of .1

     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_curl);
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
//        accelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
//        magnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        rotation = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
//        linear_accelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        proximity = mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        curlCount = (TextView) findViewById(R.id.curlCount);
        chron = new Chronometer(getApplicationContext());
        elbow = false;
        backAtElbow = false;
        shoulder = false;
        isStarted = false;
        reps = 0;
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
        SharedPreferences pref = getApplicationContext().getSharedPreferences(MyApp.PREF_NAME,
                Context.MODE_PRIVATE);

        START_ORIENTATION = pref.getFloat(START_TAG, Float.MAX_VALUE);
        ELBOW_ORIENTATION = pref.getFloat(ELBOW_TAG, Float.MAX_VALUE);
        SHOULDER_ORIENTATION = pref.getFloat(SHOULDER_TAG, Float.MAX_VALUE);
        Log.d(START_TAG, String.valueOf(SHOULDER_ORIENTATION));
        Log.d(ELBOW_TAG, String.valueOf(ELBOW_ORIENTATION));
        Log.d(SHOULDER_TAG, String.valueOf(SHOULDER_ORIENTATION));

        if (START_ORIENTATION == Float.MAX_VALUE || ELBOW_ORIENTATION == Float.MAX_VALUE ||
                SHOULDER_ORIENTATION == Float.MAX_VALUE) {
            // start the calibration activity
            Intent intent = new Intent(CurlActivity.this, CurlCalibration.class);
            startActivity(intent);
        }

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
                // now we check to see where the phone has been
                float zAngle = event.values[2];
                Log.d("zAngle", String.valueOf(zAngle));
                if (reps < MAX_REPS && isStarted) {
                    if (elbow) {
                        // we are at least at elbow, check for shoulder
                        if (shoulder) {
                            // we are at least at shoulder, check for backAtElbow
                            if (backAtElbow) {
                                // almost there check if at the beginning
                                if (compareAngles(START_ORIENTATION, zAngle)) {
                                    // completed curl reset everything and increase reps
                                    reps++;
                                    elbow = false;
                                    backAtElbow = false;
                                    shoulder = false;
                                    Log.d("COMPLETED CURL", String.valueOf(reps));
                                    curlCount.setText("Curl Count: " + String.valueOf(reps));
                                }
                            } else {
                                if (compareAngles(ELBOW_ORIENTATION, zAngle)) {
                                    backAtElbow = true;
                                    Log.d("BACK AT ELBOW", String.valueOf(zAngle));
                                }
                            }
                        } else {
                            // not at shoulder yet
                            if (compareAngles(SHOULDER_ORIENTATION, zAngle) &&
                                    distance == 0.0) {
                                shoulder = true;
                                Log.d("SHOULDER", String.valueOf(zAngle));
                            }
                        }
                    } else {
                        // check for elbow
                        if (compareAngles(ELBOW_ORIENTATION, zAngle)) {
                            elbow = true;
                            Log.d(ELBOW_TAG, String.valueOf(zAngle));
                        }
                    }
                } else if (reps == MAX_REPS) {
                    chron.stop();
                    totalTime = SystemClock.elapsedRealtime() - chron.getBase();
                    curlCount.setText("Curl Count: 10\nTime: " + totalTime + "milliseconds");
                    reps++;
                }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    public void startTimer(View v) {
        chron.start();
        isStarted = true;
    }

    private boolean compareAngles(float f1, float f2) {
        return Math.abs(f1 - f2) <= margin;
    }
}
