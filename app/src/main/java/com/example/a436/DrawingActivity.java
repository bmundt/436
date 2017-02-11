package com.example.a436;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.util.Log;
import android.content.pm.PackageManager;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;
import android.support.v4.app.ActivityCompat;
import android.Manifest;
import java.util.UUID;

public class DrawingActivity extends AppCompatActivity {

    public DrawingView drawView;
    public final int WRITE_EXTERNAL_STORAGE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawing);
        drawView = (DrawingView) findViewById(R.id.drawing);
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
            saveImage();
        }
        drawView.destroyDrawingCache();
    }

    public void saveImage() {
        String imgSaved = MediaStore.Images.Media.insertImage(
                getContentResolver(), drawView.getDrawingCache(),
                UUID.randomUUID().toString() + ".png", "drawing");
        Log.d("saveImage", "image was saved");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch(requestCode) {
            case WRITE_EXTERNAL_STORAGE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    saveImage();
                } else {
                    Log.d("permissionDenied", "permission to save picture was denied");
                }
            }
        }
    }

}
