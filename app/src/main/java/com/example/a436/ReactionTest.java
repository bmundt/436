package com.example.a436;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Random;

public class ReactionTest extends Activity {

    private static final String TAG = "MyActivity";

    private long startTime;
    private ArrayList<Long> times = new ArrayList<Long>();
    private int counter = 0;
    private int left = 0;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reaction_test);
        final Button b = (Button) findViewById(R.id.rbb1);
        ///Getting the Screen dimensions for API 1+
        b.setVisibility(View.INVISIBLE);
        runTest();

    }

    public void runTest() {
        final Button l = (Button) findViewById(R.id.lefthand);
        final Button r = (Button) findViewById(R.id.righthand);
        if(left == 0) {
            left = 1;
            l.setVisibility(View.VISIBLE);
            r.setVisibility(View.INVISIBLE);
            l.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    l.setVisibility(View.GONE);
                    Clicky();
                }
            });
        } else {
            r.setVisibility(View.VISIBLE);
            l.setVisibility(View.GONE);
            r.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    r.setVisibility(View.GONE);
                    Clicky();
                }
            });
        }

    }

    public void Clicky() {
        final Button b = (Button) findViewById(R.id.rbb1);
        ///Getting the Screen dimensions for API 1+
        b.setVisibility(View.VISIBLE);
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) b.getLayoutParams();
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        counter++;
        ///Setting the buttons paramaters, again for API 1+

        startTime = System.currentTimeMillis();

        params.height = (int) Math.round(metrics.xdpi * .5);
        params.width = (int) Math.round(metrics.xdpi * .5);

        int tempWidth = (int) Math.round(metrics.xdpi * .5);
        int tempHeight = (int) Math.round(metrics.xdpi * .5);
        params.leftMargin = new Random().nextInt(metrics.widthPixels - 2*tempWidth);
        params.topMargin = new Random().nextInt(metrics.heightPixels - 2*tempHeight);

        b.setLayoutParams(params);
        b.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(counter == 20) {
                    final long elapsedTime = System.currentTimeMillis() - startTime;
                    times.add(elapsedTime);
                    Intent intent = new Intent(ReactionTest.this, ReactionResults.class);
                    intent.putExtra("TIMES", times);
                    startActivity(intent);
                } else if(counter == 10) {
                    final long elapsedTime = System.currentTimeMillis() - startTime;
                    times.add(elapsedTime);
                    b.setVisibility(View.GONE);
                    runTest();
                    // Perform action on click
                } else {
                    final long elapsedTime = System.currentTimeMillis() - startTime;
                    times.add(elapsedTime);
                    b.setVisibility(View.GONE);
                    Clicky();
                }

            }
        });
    }
}