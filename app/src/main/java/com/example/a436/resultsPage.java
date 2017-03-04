package com.example.a436;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.v7.app.AlertDialog;
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


public class resultsPage extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        System.out.println("In results page");
        setContentView(R.layout.activity_results_page);

        MyApp app = (MyApp) getApplication();
        // save results if not in testMode
        if (!app.getTestMode()) {
            SharedPreferences pref = getPreferences(Context.MODE_PRIVATE);
            // set the values for the different trials
            SharedPreferences.Editor editor = pref.edit();
            editor.putFloat("tapsAvg", (float) app.getAvgRight());
            editor.commit();
        }
        TextView leftHand = (TextView) findViewById(R.id.leftHand);
        leftHand.append(Double.toString(app.getAvgLeft()));

        TextView rightHand = (TextView) findViewById(R.id.rightHand);
        rightHand.append(Double.toString(app.getAvgRight()));


    }
}
