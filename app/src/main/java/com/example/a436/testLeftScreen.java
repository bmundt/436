package com.example.a436;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class testLeftScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_left_screen);
    }

    public void startTest(View v) {
        Intent intent = new Intent(testLeftScreen.this, TapTestActivity.class);
        intent.putExtra("hand", "left");
        startActivity(intent);
    }
}
