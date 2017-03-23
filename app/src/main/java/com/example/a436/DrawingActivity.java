package com.example.a436;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AlertDialog;

//import android.support.v7.widget.Toolbar;
import android.view.MotionEvent;
import android.view.View;
import android.util.Log;
import android.content.pm.PackageManager;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;
import android.support.v4.app.ActivityCompat;
import android.Manifest;
import android.widget.ImageView;
import android.view.ViewGroup.LayoutParams;

import java.util.UUID;

public class DrawingActivity extends AppCompatActivity {

    public DrawingView drawView;
    public final int WRITE_EXTERNAL_STORAGE = 1;


    private String url;
    private String scoreString;

    String hand;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawing);
        drawView = (DrawingView) findViewById(R.id.drawing);
        hand = getIntent().getExtras().getString("hand");

    }



    @Override
    public void onBackPressed() {
        // Do nothing, so back button doesn't work
    }

    public void cancelDrawing(View v) {
        drawView.startOver();
    }

    public void saveDrawing(View v) {
        Log.d("saveDrawing", "reached saveDrawing");
        drawView.setDrawingCacheEnabled(true);
        if (ContextCompat.checkSelfPermission(DrawingActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(DrawingActivity.this,
                    new String[]{ Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    WRITE_EXTERNAL_STORAGE);
        } else {
            saveDrawingHelper();
        }
    }

    public Bitmap getScreenshot(View v) {
        View screenView = v.getRootView();
        screenView.setDrawingCacheEnabled(true);
        Bitmap bitmap = Bitmap.createBitmap(screenView.getDrawingCache());
        screenView.setDrawingCacheEnabled(false);
        return bitmap;
    }

    public String saveImage() {
        Bitmap screenshot = getScreenshot(getWindow().getDecorView());
        String imgSaved = MediaStore.Images.Media.insertImage(
                getContentResolver(), screenshot,
                UUID.randomUUID().toString() + ".png", "drawing");
        Log.d("saveImage", "image was saved");

        return imgSaved;
    }


    private void saveDrawingHelper() {
        url = saveImage();

        drawView.destroyDrawingCache();
        Context context = this;
        AlertDialog.Builder builder1 = new AlertDialog.Builder(context);
        builder1.setMessage("Saving Drawing To Gallery");

        AlertDialog alert11 = builder1.create();
        alert11.show();
        Handler handler = new Handler();

        ImageView imageSpiral = (ImageView) findViewById(R.id.imageView);

        double score = drawView.score(imageSpiral);
        scoreString = (new Double(score)).toString();

        handler.postDelayed(new Runnable() {

            @Override
            public void run() {
                Intent intent = new Intent(DrawingActivity.this, SpiralResults.class);
                intent.putExtra("URL", url);
                intent.putExtra("score", scoreString);
                intent.putExtra("hand", hand);
                startActivity(intent);
            }

        }, 1000);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch(requestCode) {
            case WRITE_EXTERNAL_STORAGE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    saveDrawingHelper();
                } else {
                    Log.d("permissionDenied", "permission to save picture was denied");
                }
            }
        }
    }


}
