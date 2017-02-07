package com.example.a436;

import android.content.DialogInterface;
import android.content.Intent;
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
        TextView leftHand = (TextView) findViewById(R.id.leftHand);
        leftHand.append(Double.toString(((MyApp) getApplication()).getAvgLeft()));

        TextView rightHand = (TextView) findViewById(R.id.rightHand);
        rightHand.append(Double.toString(((MyApp) getApplication()).getAvgRight()));

    }
}
