package com.example.a436;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.content.Intent;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        /*
            Insturctions methods would go here, and then make an
            intent to call TapTestActivity with correct hand
            for now I'll just have it send one intent with left hand
        */
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
        Intent intent = new Intent(MainActivity.this, Level.class);
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
    }
}
