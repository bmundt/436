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
import static com.example.a436.MyApp.*;
import static edu.umd.cmsc436.sheets.Sheets.TestType.*;

public class resultsPage extends SheetsActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        System.out.println("In results page");
        setContentView(R.layout.activity_results_page);

        MyApp app = (MyApp) getApplication();
        // save results if not in testMode
        if (!app.getTestMode()) {
            SharedPreferences pref = getApplicationContext().getSharedPreferences(MyApp.PREF_NAME,
                    Context.MODE_PRIVATE);
            // set the values for the different trials
            SharedPreferences.Editor editor = pref.edit();
            editor.putFloat(TAPS_L_AVG, (float) app.getAvgRight());
            editor.putFloat(TAPS_R_AVG, (float) app.getAvgLeft());
            editor.commit();
        }
        TextView leftHand = (TextView) findViewById(R.id.leftHand);
        leftHand.append(Double.toString(app.getAvgLeft()));

        TextView rightHand = (TextView) findViewById(R.id.rightHand);
        rightHand.append(Double.toString(app.getAvgRight()));

        super.sendToSheets(LH_TAP);
        super.sendToSheets(RH_TAP);

//        Intent intentL = new Intent(resultsPage.this, SendResults.class);
//        intentL.putExtra(Sheets.EXTRA_TYPE, Sheets.UpdateType.LH_TAP.ordinal());
//        startActivity(intentL);
//
//        Intent intentR = new Intent(resultsPage.this, SendResults.class);
//        intentR.putExtra(Sheets.EXTRA_TYPE, Sheets.UpdateType.RH_TAP.ordinal());
//        startActivity(intentR);
    }

    public void toHome(View v)
    {
        Intent intent = new Intent(resultsPage.this, MainActivity.class);
        startActivity(intent);
    }
}
