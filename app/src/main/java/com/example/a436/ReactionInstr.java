package com.example.a436;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class ReactionInstr extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reaction_instr);
    }

    public void nextButton(View v)
    {
        Intent intent = new Intent(ReactionInstr.this, ReactionTest.class);
        startActivity(intent);
    }
}
