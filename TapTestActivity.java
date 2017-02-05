package com.example.a436;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.app.Activity;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.SystemClock;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

/**
 * Created by brmun on 2/2/2017.
 */

public class TapTestActivity extends AppCompatActivity {

    private boolean timerStarted;
    private int taps;
    private int totalTaps;
    private CountDownTimer timer;
    public TextView text;
    private String hand;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tap_test_activity);
        text = (TextView) this.findViewById(R.id.timeLeft);
        hand = getIntent().getExtras().getString("hand");

        final Button tap = (Button) findViewById(R.id.tap);
        final Button tryAgain = (Button) findViewById(R.id.tryAgain);
        final Button toRightHand = (Button) findViewById(R.id.toRightHand);
        final Button toResults = (Button) findViewById(R.id.toResults);

        tryAgain.setVisibility(View.GONE);
        toRightHand.setVisibility(View.GONE);
        toResults.setVisibility(View.GONE);

        timer = new CountDownTimer(10000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                text.setText("Seconds remaining: " + millisUntilFinished / 1000);
            }

            @Override
            public void onFinish() {
                totalTaps = taps;
                text.setText("Total Taps: " + totalTaps);

                if (hand.compareTo("left") == 0) {
                    ((MyApp) getApplication()).newLeftHandTest(totalTaps);
                } else {
                    ((MyApp) getApplication()).newRigthHandTest(totalTaps);
                }


                int testNum = ((MyApp) getApplication()).getTestNum();
                if(testNum == 5 && hand.compareTo("left") == 0){
                    tap.setVisibility(View.GONE);
                    toRightHand.setVisibility(View.VISIBLE);
                } else if(testNum == 5){
                    tap.setVisibility(View.GONE);
                    toResults.setVisibility(View.VISIBLE);
                } else {
                    tryAgain.setVisibility(View.VISIBLE);
                }

            }
        };

        timerStarted = false;
        taps = 0;
    }

    public void tapButton(View v) {
        if (!timerStarted) { // only start timer if not already started
            timer.start();
            taps++;
            timerStarted = true;
        } else {
            taps++;
        }
    }

    public void tryAgainButton(View v){
        final Button tap = (Button) findViewById(R.id.tap);
        final Button tryAgain = (Button) findViewById(R.id.tryAgain);

        tryAgain.setVisibility(View.GONE);
        timerStarted = false;
        taps = 0;
    }

}
