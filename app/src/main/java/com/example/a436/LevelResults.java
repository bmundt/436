package com.example.a436;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.lang.Math;

import java.util.ArrayList;
import static com.example.a436.MyApp.*;

public class LevelResults extends Activity {
    LevelResultsView drawView;
    ArrayList<Double> XList;
    ArrayList<Double> YList;
    int middleX, middleY;
    private static final String TAG = "MyActivity";
    String hand;



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_level_results);
        hand = getIntent().getExtras().getString("hand");
        Log.d(TAG, "the hand is: " + hand);
        if(hand.equals("left")) {
            Button btn = (Button)findViewById(R.id.toRight);
            btn.setVisibility(View.VISIBLE);
            Button btn2 = (Button)findViewById(R.id.toHome);
            btn2.setVisibility(View.INVISIBLE);
        } else {
            Button btn = (Button)findViewById(R.id.toRight);
            btn.setVisibility(View.INVISIBLE);
            Button btn2 = (Button)findViewById(R.id.toHome);
            btn2.setVisibility(View.VISIBLE);
        }
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
            SharedPreferences pref = getApplicationContext().getSharedPreferences(MyApp.PREF_NAME,
                    Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = pref.edit();
            if(hand.equals("left")) {
                editor.putFloat(LEVEL_L, avgDist.floatValue());
            } else {
                editor.putFloat(LEVEL_R, avgDist.floatValue());
            }
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

    public void toHome(View v)
    {
        Intent intent = new Intent(LevelResults.this, MainActivity.class);
        startActivity(intent);
    }

    public void toRightTest(View v)
    {
        Intent intent = new Intent(LevelResults.this, Level.class);
        intent.putExtra("hand", "right");
        startActivity(intent);
    }


}
