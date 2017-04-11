package com.example.a436;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PointF;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;

import edu.umd.cmsc436.sheets.Sheets;

import static com.example.a436.MyApp.*;

public class SwayResults extends SheetsActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sway_results);

        TextView textView = (TextView) findViewById(R.id.textView);

        ArrayList<PointF> myList = (ArrayList<PointF>) getIntent().getSerializableExtra("points");


        float avg = (float) calculateAverage(myList);
        textView.setText("The average distance was: " + avg);

        SharedPreferences pref = getApplicationContext().getSharedPreferences(MyApp.PREF_NAME,
                Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = pref.edit();

        editor.putFloat(SWAY, avg);
        editor.commit();
        super.sendToSheets(Sheets.TestType.HEAD_SWAY);

    }

    private double calculateAverage(ArrayList<PointF> l) {
        double size = l.size();
        double ans = 0.0;
        for(PointF p: l) {
            double xsq = Math.pow(p.x, 2.0);
            double ysq = Math.pow(p.y, 2.0);
            double sum = xsq+ysq;
            ans += Math.sqrt(sum);
        }

        return ans/size;
    }

    public void toHome(View v) {
        Intent intent = new Intent(SwayResults.this, MainActivity.class);
        startActivity(intent);
    }
}
