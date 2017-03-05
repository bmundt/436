package com.example.a436;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
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
import android.widget.LinearLayout;
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
    private boolean switchHands = false;


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
        setTestText();

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
                    ((MyApp) getApplication()).newRightHandTest(totalTaps);
                }

                int testNum = getRealTestNum();
                if(testNum == 1 && hand.compareTo("left") == 0){
                    tryAgain.setVisibility(View.VISIBLE);
                    switchHands = true;
                } else if(testNum == 1){
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
        if (switchHands) {
            /* Will alert the instructions, also telling them to switch hands
                    Then after they hit okay will reset the page as right hand.*/
            AlertDialog instructions = new AlertDialog.Builder(TapTestActivity.this).create();
            instructions.setTitle("Instructions");
            instructions.setMessage("Please Switch to your right hand, and perform 3 trials");
            instructions.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent(TapTestActivity.this, TapTestActivity.class);
                            intent.putExtra("hand", "right");
                            startActivity(intent);
                            dialog.dismiss();
                        }
                    });
            instructions.show();
        } else {
            final Button tap = (Button) findViewById(R.id.tap);
            final Button tryAgain = (Button) findViewById(R.id.tryAgain);

            tryAgain.setVisibility(View.GONE);
            timerStarted = false;
            taps = 0;
            setTestText();
        }
    }

    public void toResultsButton(View v) {
        Intent resultsIntent = new Intent(TapTestActivity.this, resultsPage.class);
        startActivity(resultsIntent);
    }

    private void setTestText() {
        String handText = hand.substring(0,1).toUpperCase() + hand.substring(1);
        text.setText(handText + " Hand, Trial: " + getRealTestNum());
    }

    private int getRealTestNum() {
        return ((MyApp) getApplication()).getRealTestNum();
    }
}
