package com.example.a436;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class LevelInstr extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_level_instr);
    }

    public void nextButton(View v)
    {
        Intent intent = new Intent(LevelInstr.this, Level.class);
        intent.putExtra("hand", "left");
        startActivity(intent);
    }
}

