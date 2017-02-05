package com.example.a436;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class InstrScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_instr_screen);
    }

    public void nextBut(View v)
    {
        Intent intent = new Intent(InstrScreen.this, testLeftScreen.class);
        startActivity(intent);
    }
}
