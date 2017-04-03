package com.example.a436;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import static com.example.a436.MyApp.*;

public class ReactionResults extends Activity {

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

        editor.putLong(REACTION_L, avgl);
        editor.putLong(REACTION_R, avgr);
        editor.commit();
    }


    public void toHome(View v)
    {
        Intent intent = new Intent(ReactionResults.this, MainActivity.class);
        startActivity(intent);
    }
}
