package com.example.a436;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import java.lang.Math;

import java.util.ArrayList;

public class LevelResults extends Activity {
    LevelResultsView drawView;
    ArrayList<Double> XList;
    ArrayList<Double> YList;
    int middleX, middleY;
    private static final String TAG = "MyActivity";



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_level_results);
        XList = (ArrayList<Double>) getIntent().getSerializableExtra("XList");
        YList = (ArrayList<Double>) getIntent().getSerializableExtra("YList");

        middleX = getIntent().getIntExtra("middleX", 0);
        middleY = getIntent().getIntExtra("middleY", 0);

        // for each x and y compute the distance from middle x to middle y
        double distSum = 0;
        for (int i = 0; i < XList.size(); i++) {
            distSum += Math.sqrt(Math.pow(middleX - XList.get(i), 2) +
                    Math.pow(middleY - YList.get(i) , 2));
        }
        Double avgDist = distSum / XList.size();
        avgDist = (double) Math.round(avgDist * 100.0);
        avgDist /= 100;

        if (!((MyApp) getApplication()).getTestMode()) {
            SharedPreferences pref = getPreferences(Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = pref.edit();
            editor.putFloat("level", avgDist.floatValue());
            editor.commit();
        }
        Log.d(TAG, "Avg distance is: " + avgDist);

//        drawView = new LevelResultsView(this);
        drawView = (LevelResultsView) findViewById(R.id.resultsView);
        drawView.setLists(XList, YList);
        Log.d(TAG, "The size is " + XList.size());
        drawView.setBackgroundColor(Color.WHITE);


        TextView score = (TextView) findViewById(R.id.score);
        score.setText("Score: " + avgDist);

    }
}
