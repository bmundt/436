package com.example.a436;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;

import java.util.ArrayList;

public class LevelResults extends Activity {
    LevelResultsView drawView;
    ArrayList<Double> XList;
    ArrayList<Double> YList;
    private static final String TAG = "MyActivity";



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        XList = (ArrayList<Double>) getIntent().getSerializableExtra("XList");
        YList = (ArrayList<Double>) getIntent().getSerializableExtra("YList");

        drawView = new LevelResultsView(this);
        drawView.setLists(XList, YList);
        Log.d(TAG, "The size is " + XList.size());
        drawView.setBackgroundColor(Color.WHITE);
        setContentView(drawView);

    }
}
