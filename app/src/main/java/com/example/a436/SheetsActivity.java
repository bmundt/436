package com.example.a436;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import edu.umd.cmsc436.sheets.Sheets;

import static com.example.a436.MyApp.SWAY;
import static com.example.a436.MyApp.TAPS_R_AVG;
import static edu.umd.cmsc436.sheets.Sheets.TestType.*;

import static com.example.a436.MyApp.CURL_L;
import static com.example.a436.MyApp.CURL_R;
import static com.example.a436.MyApp.LEVEL_L;
import static com.example.a436.MyApp.LEVEL_R;
import static com.example.a436.MyApp.REACTION_L;
import static com.example.a436.MyApp.REACTION_R;
import static com.example.a436.MyApp.SPIRAL_L;
import static com.example.a436.MyApp.SPIRAL_R;
import static com.example.a436.MyApp.TAPS;
import static com.example.a436.MyApp.TAPS_L_AVG;

public class SheetsActivity extends Activity implements Sheets.Host {

    private Sheets sheet;
    private int patientId;
    private String spreadsheetId;
    private String privateSpreadsheetId;
    private SharedPreferences pref;
    private MyApp myApp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        spreadsheetId = "1YvI3CjS4ZlZQDYi5PaiA7WGGcoCsZfLoSFM0IdvdbDU";
        privateSpreadsheetId = "13fbFdZSWjjP_DakUX2liVe8pgM4PovPHA96PNd2YYMk";
        sheet = new Sheets(this, this, getString(R.string.app_name),
                spreadsheetId, privateSpreadsheetId);
        pref = getApplicationContext().getSharedPreferences(MyApp.PREF_NAME,
                Context.MODE_PRIVATE);
        patientId = pref.getInt("patientID", -1);
        myApp = (MyApp) getApplication();
        super.onCreate(savedInstanceState);
    }


    protected void sendToSheets(Sheets.TestType type) {
        float data = 0;

        float[] trials = null;
        if (patientId == -1) {
            Log.d("SHEETS", "No patientID");
        } else {
            Log.d("SHEETS", "Type: " + String.valueOf(type));
            switch(type) {
                case LH_TAP:
                    trials = new float[myApp.getNumTrials() + 1];
                    for (int i = 0; i < myApp.getNumTrials(); i++) {
                        trials[i] = (float) pref.getInt(TAPS + "_LEFT_" + (i + 1), 0);
                    }
                    trials[myApp.getNumTrials()] = pref.getFloat(TAPS_L_AVG, 0.0F);
                    data = pref.getFloat(TAPS_L_AVG, 0.0F);
                    break;
                case RH_TAP:
                    trials = new float[myApp.getNumTrials() + 1];
                    for (int i = 0; i < myApp.getNumTrials(); i++) {
                        trials[i] = (float) pref.getInt(TAPS + "_RIGHT_" + (i + 1), 0);
                    }
                    trials[myApp.getNumTrials()] = pref.getFloat(TAPS_R_AVG, 0.0F);
                    data = pref.getFloat(TAPS_L_AVG, 0.0F);
                    break;
                case LH_SPIRAL:
                    data = pref.getFloat(SPIRAL_L, 0.0F);
                    break;
                case RH_SPIRAL:
                    data = pref.getFloat(SPIRAL_R, 0.0F);
                    break;
                case LH_LEVEL:
                    data = pref.getFloat(LEVEL_L, 0.0F);
                    break;
                case RH_LEVEL:
                    data = pref.getFloat(LEVEL_R, 0.0F);
                    break;
                case LH_POP:
                    data = pref.getFloat(REACTION_L, 0.0F);
                    break;
                case RH_POP:
                    data = pref.getFloat(REACTION_R, 0.0F);
                    break;
                case LH_CURL:
                    data = pref.getFloat(CURL_L, 0.0F);
                    break;
                case RH_CURL:
                    data = pref.getFloat(CURL_R, 0.0F);
                    break;
                case HEAD_SWAY:
                    data = pref.getFloat(SWAY, 0.0F);
                    break;
                default:
                    Log.d("SHEETS", "Unknown type");
            }

            String userId = "t02p" + String.valueOf(patientId);
            Log.d("SHEETS", userId);

            sheet.writeData(type, userId, data);
            if (trials == null)
                trials = new float[]{data};
            sheet.writeTrials(type, userId, trials);
        }
    }

    @Override
    public int getRequestCode(Sheets.Action action) {
        switch (action) {
            case REQUEST_PERMISSIONS:
                return 1000;
            case REQUEST_ACCOUNT_NAME:
                return 1001;
            case REQUEST_PLAY_SERVICES:
                return 1002;
            case REQUEST_AUTHORIZATION:
                return 1003;
        }
        return 0;
    }

    @Override
    public void notifyFinished(Exception e) {
        if (e != null) {
            Log.d("SHEETS", e.toString());
        } else {
            Log.d("SHEETS", "notifyFinished exception was null");
        }
    }

    @Override
    public void onRequestPermissionsResult (int requestCode, String permissions[], int[] grantResults) {
        sheet.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        sheet.onActivityResult(requestCode, resultCode, data);
    }

}
