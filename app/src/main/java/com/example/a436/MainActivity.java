package com.example.a436;

import android.content.Context;
import android.content.SharedPreferences;
import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);



        setContentView(R.layout.activity_main);
        /*
            Insturctions methods would go here, and then make an
            intent to call TapTestActivity with correct hand
            for now I'll just have it send one intent with left hand
        */

        Switch toggle = (Switch) findViewById(R.id.test_mode_toggle);
        toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    ((MyApp) getApplication()).setTestMode(true);
                } else {
                    ((MyApp) getApplication()).setTestMode(false);
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences pref = getApplicationContext().getSharedPreferences(MyApp.PREF_NAME,
                Context.MODE_PRIVATE);
//        int patientID = -1;
        int patientID = pref.getInt("patientID", -1);
        Log.d("Main", "This is the patient ID: " + patientID);
        if (patientID == -1) {
            Intent intent = new Intent(MainActivity.this, LoginScreen.class);
            startActivity(intent);
        }
    }

    public void tapTest(View v)
    {
        Intent intent = new Intent(MainActivity.this, InstrScreen.class);
        startActivity(intent);
    }

    public void spiralTest(View v)
    {
        Intent intent = new Intent(MainActivity.this, SpiralInstr.class);
        startActivity(intent);
    }

    public void levelTest(View v) {
        Intent intent = new Intent(MainActivity.this, LevelInstr.class);
        startActivity(intent);
    }

    public void reactionTest(View v) {
        Intent intent = new Intent(MainActivity.this, ReactionInstr.class);
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
    }

    public void sendResults(View v) {
        // would start the post results activity, would
        // have to check if all tests are done
        Intent intent = new Intent(MainActivity.this, PostResults.class);
        startActivity(intent);
    }

    public void curlTest(View V) {
        Intent intent = new Intent(MainActivity.this, CurlActivity.class);
        startActivity(intent);
    }

    public void login(View v) {
        Intent intent = new Intent(MainActivity.this, LoginScreen.class);
        startActivity(intent);
    }
}

