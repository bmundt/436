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

import edu.umd.cmsc436.sheets.Sheets;

import static com.example.a436.MyApp.*;

public class CurlActivity extends SheetsActivity implements SensorEventListener {

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
    private final int CLOSE_ENOUGH = 2;

    private Chronometer chron;
    private boolean elbow;
    private boolean shoulder;
    private boolean backAtElbow;
    private boolean isStarted;
    private int reps;
    private final int MAX_REPS = 10;
    private final float margin = .25F;
    private long totalTime;

    private SharedPreferences pref;


    // get accelerometer force until 0.0
    // then track it again backwards


    /* Tested Angle Values
    START
    03-27 15:56:00.117 21996-21996/com.example.a436 D/xAngle: -0.05655693
    03-27 15:56:00.117 21996-21996/com.example.a436 D/yAngle: 0.036585867
    03-27 15:56:00.117 21996-21996/com.example.a436 D/zAngle: -0.8964851

    ELBOW
    03-27 15:56:03.607 21996-21996/com.example.a436 D/xAngle: 0.27685565
    03-27 15:56:03.607 21996-21996/com.example.a436 D/yAngle: -0.682732
    03-27 15:56:03.607 21996-21996/com.example.a436 D/zAngle: -0.63546443

    SHOULDER
    03-27 15:56:06.797 21996-21996/com.example.a436 D/xAngle: 0.82678956
    03-27 15:56:06.797 21996-21996/com.example.a436 D/yAngle: -0.5053381
    03-27 15:56:06.797 21996-21996/com.example.a436 D/zAngle: -0.18620798


     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_curl);
        findViewById(R.id.main_menu_btn).setVisibility(View.GONE);
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
//        accelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
//        magnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        rotation = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
//        linear_accelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        proximity = mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        instructions = (TextView) findViewById(R.id.instructions);
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
    public void onBackPressed() {}


    @Override
    protected void onResume() {
        super.onResume();
        pref = getApplicationContext().getSharedPreferences(MyApp.PREF_NAME,
                Context.MODE_PRIVATE);

        START_ORIENTATION = pref.getFloat(START_TAG, Float.MAX_VALUE);
        ELBOW_ORIENTATION = pref.getFloat(ELBOW_TAG, Float.MAX_VALUE);
        SHOULDER_ORIENTATION = pref.getFloat(SHOULDER_TAG, Float.MAX_VALUE);
        Log.d(START_TAG, String.valueOf(START_ORIENTATION));
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
                float xAngle = event.values[0];

                if (reps < MAX_REPS && isStarted) {
                    Log.d("xAngle", String.valueOf(xAngle));
                    if (elbow) {
                        // we are at least at elbow, check for shoulder
                        if (shoulder) {
                            // we are at least at shoulder, check for backAtElbow
                            if (backAtElbow) {
                                // almost there check if at the beginning
                                if (compareAngles(START_ORIENTATION, xAngle)) {
                                    // completed curl reset everything and increase reps
                                    reps++;
                                    elbow = false;
                                    backAtElbow = false;
                                    shoulder = false;
                                    Log.d("COMPLETED CURL", String.valueOf(reps));
                                    instructions.setText("BACK AT START OF CURL");
                                    curlCount.setText("Curl Count: " + String.valueOf(reps));
                                }
                            } else {
                                if (compareAngles(ELBOW_ORIENTATION, xAngle)) {
                                    backAtElbow = true;
                                    instructions.setText("HIT ELBOW ON BACKSWING");
                                    Log.d("BACK AT ELBOW", String.valueOf(xAngle));
                                }
                            }
                        } else {
                            // not at shoulder yet
                            if (compareAngles(SHOULDER_ORIENTATION, xAngle) &&
                                    distance <= CLOSE_ENOUGH) {
                                shoulder = true;
                                instructions.setText("AT SHOULDER");
                                Log.d("SHOULDER", String.valueOf(xAngle));
                            }
                        }
                    } else {
                        // check for elbow
                        if (compareAngles(ELBOW_ORIENTATION, xAngle)) {
                            elbow = true;
                            instructions.setText("AT ELBOW");
                            Log.d(ELBOW_TAG, String.valueOf(xAngle));
                        }
                    }
                } else if (reps == MAX_REPS) {
                    chron.stop();
                    totalTime = SystemClock.elapsedRealtime() - chron.getBase();
                    curlCount.setText("Curl Count: 10\nTime: " + totalTime + "milliseconds");
                    findViewById(R.id.main_menu_btn).setVisibility(View.VISIBLE);
                    reps++;
                    SharedPreferences.Editor editor = pref.edit();
                    editor.putFloat(CURL_R, ((float) totalTime) / 1000F);
                    editor.commit();
                    Log.d("CURL", String.valueOf(pref.getFloat(CURL_R, 0F)));
                    super.sendToSheets(Sheets.TestType.RH_CURL);
                }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    public void startTimer(View v) {
        chron.start();
        isStarted = true;
        Log.d("TIMER", "TIMER STARTED");
    }

    public void mainMenu(View v) {
        Intent intent = new Intent(CurlActivity.this, MainActivity.class);
        startActivity(intent);
    }

    private boolean compareAngles(float f1, float f2) {
        return Math.abs(f1 - f2) <= margin;
    }
}
