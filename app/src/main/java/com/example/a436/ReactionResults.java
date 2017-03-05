package com.example.a436;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import java.util.ArrayList;

public class ReactionResults extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reaction_results);

        TextView mText = (TextView) findViewById(R.id.textview);

        ArrayList<Long> s = (ArrayList<Long>) getIntent().getSerializableExtra("TIMES");
        String results = "";
        for(int i = 0; i < s.size(); i++) {
            results = results+ "Results " + i + ": " + s.get(i) + "\n";
        }
        mText.setText(results);
    }
}
