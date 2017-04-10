package com.example.a436;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import edu.umd.cmsc436.sheets.Sheets;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    protected void sendToSheets(Sheets.TestType type) {
        float data = 0;
        String spreadsheetId = "1ASIF7kZHFFaUNiBndhPKTGYaQgTEbqPNfYO5DVb1Y9Y";
        SharedPreferences pref = getApplicationContext().getSharedPreferences(MyApp.PREF_NAME,
                Context.MODE_PRIVATE);
        int patientId = pref.getInt("patientID", -1);
        if (patientId == -1) {
            Log.d("SHEETS", "No patientID");
        } else {
            switch(type) {
                case LH_TAP:
//                for (int i = 0; i <= myApp.getNumTrials(); i++) {
//                    data.add(pref.getInt(TAPS + "_LEFT_" + i, 0));
//                }
                    data = pref.getFloat(TAPS_L_AVG, 0.0F);
//                data.add(avgLTaps);
                    break;
                case RH_TAP:
//                for (int i = 0; i <= myApp.getNumTrials(); i++) {
//                    data.add(pref.getInt(TAPS + "_RIGHT_" + i, 0));
//                }
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
                    data = pref.getLong(CURL_L, 0L);
                    break;
                case RH_CURL:
                    data = pref.getLong(CURL_R, 0L);
                    break;
                default:
                    Log.d("SHEETS", "Unknown type");
            }

            String userId = "t02p" + String.valueOf(patientId);
            Sheets sheet = new Sheets(this, getString(R.string.app_name), spreadsheetId);
            sheet.writeData(type, userId, data);
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

    }

}
