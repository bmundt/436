package com.example.a436;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class SwayInstructions extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sway_instructions);
    }

    public void startTest(View v) {
        startActivity(new Intent(SwayInstructions.this, SwayActivity.class));
    }
}
