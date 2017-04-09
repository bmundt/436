package com.example.a436;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Binder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Sway extends Service {
    SensorManager a;
    Sensor xl;
    List<Float> measurements;
    private IBinder ibinder = new localBinder();
    public Sway() {
    }
    public class localBinder extends Binder {

        protected Sway GetService(){
            return Sway.this;

        }
    }

    public float[] getSensorReading(){
        synchronized (measurements){
            return new float[]{
                    measurements.get(0),
                    measurements.get(1),
                    measurements.get(2)
            };
        }

    }

    @Override
    public IBinder onBind(Intent intent) {

        measurements = Collections.synchronizedList(new ArrayList<Float>(3));

        HandlerThread handles = new HandlerThread("ThreadsHandler");
        handles.start();
        Handler handlerofHandler = new Handler(handles.getLooper());

        a = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        xl = a.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        SensorEventListener working = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                synchronized (measurements) {
                    measurements.add(0, event.values[0]);
                    measurements.add(1, event.values[1]);
                    measurements.add(2, event.values[2]);
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int i) {

            }
        };

        a.registerListener(working,xl,SensorManager.SENSOR_DELAY_NORMAL,handlerofHandler);
        return ibinder;
        // TODO: Return the communication channel to the service.

    }
}
