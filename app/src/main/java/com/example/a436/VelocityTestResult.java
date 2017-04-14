package com.example.a436;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.view.View;
import android.widget.TextView;

public class VelocityTestResult extends AppCompatActivity {

    private float velocity;
    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_velocity_test_result);


        velocity = getIntent().getFloatExtra("velocity", -1);



        textView = (TextView) findViewById(R.id.resultText);
        textView.setTextSize(50);
        textView.setText("Result: " + velocity + "m/s");
    }

    public void toMainMenu(View v){
        startActivity(new Intent(VelocityTestResult.this, MainActivity.class));
    }


}
