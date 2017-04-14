package com.example.a436;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import android.widget.TextView;

public class VelocityTestInstructions extends AppCompatActivity {

    TextView textView;
    final static String INSTRUCTION =
            "Hold the phone up straight outwards. When you are ready to begin the test press Start" +
            " button at the top of the screeen. When you are finish, press the stop button that will" +
            " replace the start button once you have started to test.";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_velocity_test_instructions);
        textView = (TextView) findViewById(R.id.text);
        textView.setText(INSTRUCTION);
    }

    public void toVelocityTest(View v){
        Intent i = new Intent(VelocityTestInstructions.this, VelocityTest.class);
        startActivity(i);
    }
}
