package com.example.a436;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class PracticeSpiral extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_practice_spiral);
    }

    public void toTest(View v)
    {
        Intent intent = new Intent(PracticeSpiral.this, DrawingActivity.class);
        startActivity(intent);
    }
}
