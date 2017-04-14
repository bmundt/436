package com.example.a436;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.util.ArrayList;
import java.util.List;

public class VelocityTest extends AppCompatActivity {

    private SensorManager mSensorManager;
    private Sensor mSensor;
    private List<Float> recordings;
    private boolean isRecording;
    private long startTime;
    private long endTime;
    private boolean isStarted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_velocity_test);

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        recordings = new ArrayList<>();



        mSensorManager.registerListener(new SensorEventListener(){

            @Override
            public void onSensorChanged(SensorEvent event) {
                if(isRecording && event.values[0] >=0) recordings.add(event.values[0]);

            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }
        }, mSensor, SensorManager.SENSOR_DELAY_FASTEST);
    }

    @Override
    protected void onPause() {
        super.onPause();
        //startActivity(new Intent(VelocityTest.this, VelocityTestInstructions.class));

    }

    public void startBtn(View v){
        Button startStopButton =  (Button) findViewById(R.id.startButton);
        if(isStarted == false){
            startTest(startStopButton);
        } else {
            endTest(v);
        }


    }

    public void resetBtn(View v){
        recordings = new ArrayList<>();
        isRecording = false;
        startTime = 0;
        isStarted = false;

        Button startStopButton =  (Button) findViewById(R.id.startButton);
        startStopButton.setText("START");
    }
    /* Calculate velocity */
    private void startTest(Button btn){

        btn.setText("END");
        isRecording = true;
        isStarted = true;
        startTime = System.currentTimeMillis();
    }

    private void endTest(View v) {
        isRecording = false;
        endTime = System.currentTimeMillis();

        Intent i = new Intent(VelocityTest.this, VelocityTestResult.class);
        i.putExtra("velocity", calculateVelocity());
        startActivity(i);
    }

    private float calculateVelocity(){
        float diffTime = (float) (endTime - startTime)/1000;
        float sumVelocity = 0;

        for(Float r : recordings){
            sumVelocity += r;
        }

        Log.d("difftime",String.valueOf(diffTime));
        Log.d("velocity sum", recordings.toString());


        /* Calculate velocity */
        return (sumVelocity/recordings.size());
    }


}
