package com.example.a436;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.os.Vibrator;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;


public class SwayActivity extends AppCompatActivity {
    Sway a;
    boolean isBound;
    Vibrator v;
    Button mainButton;
    float[] initialMeasure = new float[3];
    final int PIXEL_SIZE = 900;
    final float ACCELERATION_LIMIT = 4.5f;
    final float CONSTANT = ((PIXEL_SIZE/2)/ACCELERATION_LIMIT);
    //holds a tuple, the 2 coordinates
    ArrayList<PointF> dataList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sway);
        mainButton = (Button) findViewById(R.id.swayButtonNext);
        //mainButton.setOnClickListener();
        v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        //gets permissions to save files/pictures
        getPermission();
        dataList = new ArrayList<>();
        // Vibrate for 500 milliseconds

    }

    public void onButtonClick(View view){
        mainButton.setText("started!");
        delay.start();
    }
    //connecting a service to the actibity
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            a = ((Sway.localBinder)service).GetService();
            isBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isBound = false;
        }


    };
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isBound) {
            unbindService(serviceConnection);
        }
    }
    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = new Intent(this, Sway.class);

        bindService(intent,serviceConnection, Context.BIND_AUTO_CREATE);

    }
    @Override
    protected void onStop(){
        super.onStop();

    }
    //vibrates and then slight delay for reaction time to start the test
    //gets initial measure of x y z accel
    CountDownTimer delay = new CountDownTimer(10000,1000) {
        @Override
        public void onTick(long l) {

            mainButton.setText("testing\n"+ l/1000);

            if(l < 2000) {
                v.vibrate(1500);
            }
        }

        @Override
        public void onFinish() {
            initialMeasure = a.getSensorReading();
            startTest.start();

        }
    };
    //gets continuous updates
    CountDownTimer startTest = new CountDownTimer(10000,100) {
        float[] newData = new float[3];

        @Override
        public void onTick(long l) {
            mainButton.setText("recording\n" + l/1000);
            newData = a.getSensorReading();
            PointF changes = new PointF(initialMeasure[0] - newData[0],initialMeasure[2] - newData[2]);
      //      change[0] = initialMeasure[0] - newData[0];
      //      change[1] = initialMeasure[2] - newData[2];
              dataList.add(changes);
        }

        @Override
        public void onFinish() {
            v.vibrate(1500);
            mainButton.setText("finished");
            Bitmap bitmap = getDrawing(dataList);
            String title = (new SimpleDateFormat("yyyddMM_HHmmss")).format(Calendar.getInstance().getTime());
            MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, title , "");
            Intent intent = new Intent(SwayActivity.this, SwayResults.class);
            intent.putExtra("points", dataList);
            startActivity(intent);
            mainButton.setText("check");
            finish();
        }
    };

    //inner class to draw it.

    private Bitmap getDrawing(ArrayList<PointF> l){
        Path path = new Path();
        Paint paint = new Paint();

        paint.setColor(Color.RED);
        paint.setAntiAlias(true);
        paint.setStrokeWidth(5);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeCap(Paint.Cap.ROUND);

        Bitmap Golub = Bitmap.createBitmap(
                PIXEL_SIZE,
                PIXEL_SIZE,
                Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(

                       Golub);
        path.moveTo(PIXEL_SIZE/2,PIXEL_SIZE/2);

        for(PointF p: l){

            path.lineTo((p.x * CONSTANT)+PIXEL_SIZE/2,(p.y * CONSTANT)+PIXEL_SIZE/2);
        }

        canvas.drawPath(path,paint);


        return Golub;
    }

    private void getPermission(){
        if (ContextCompat.checkSelfPermission(SwayActivity.this,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {


            if (ActivityCompat.shouldShowRequestPermissionRationale(SwayActivity.this,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

            } else {

                ActivityCompat.requestPermissions(SwayActivity.this,
                        new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        1);

            }

        }
    }
}
