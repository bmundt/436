package com.example.a436;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;

import edu.umd.cmsc436.sheets.Sheets;

import static com.example.a436.MyApp.*;

public class ReactionResults extends SheetsActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reaction_results);

        TextView mText = (TextView) findViewById(R.id.textview);

        ArrayList<Long> s = (ArrayList<Long>) getIntent().getSerializableExtra("TIMES");
        String results = "";
        long suml = 0;
        long sumr = 0;
        for(int i = 0; i < 10; i++) {
            suml += s.get(i);
        }
        for(int i = 10; i < 20; i++) {
            sumr += s.get(i);
        }
        long avgl = suml/10;
        long avgr = sumr/10;
        results = results + "The left hand average is: " + avgl + "\n";
        results = results + "The right hand average is: " + avgr + "\n";

        mText.setText(results);

        SharedPreferences pref = getApplicationContext().getSharedPreferences(MyApp.PREF_NAME,
                Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = pref.edit();

        float avgl_seconds = avgl / 1000.0F;
        float avgr_seconds = avgr / 1000.0F;


        editor.putFloat(REACTION_L, avgl_seconds);
        editor.putFloat(REACTION_R, avgr_seconds);
        editor.commit();

        super.sendToSheets(Sheets.TestType.LH_POP);
        super.sendToSheets(Sheets.TestType.RH_POP);
    }


    public void toHome(View v)
    {
        Intent intent = new Intent(ReactionResults.this, MainActivity.class);
        startActivity(intent);
    }
}
