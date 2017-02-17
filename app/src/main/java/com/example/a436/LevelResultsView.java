package com.example.a436;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;

import java.util.ArrayList;

public class LevelResultsView extends View {
    Paint paint = new Paint();
    ArrayList<Double> XList;
    ArrayList<Double> YList;

    public LevelResultsView(Context context) {
        super(context);
        paint.setColor(Color.BLACK);
        paint.setStrokeWidth(10);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeCap(Paint.Cap.ROUND);
    }

    @Override
    public void onDraw(Canvas canvas) {
        int size = XList.size();

        for(int i = 0; i < size-1; i++) {
            Double dx =  XList.get(i);
            Double dy = YList.get(i);
            Double ndx =  XList.get(i+1);
            Double ndy = YList.get(i+1);


            float currX= dx.floatValue();
            float currY= dy.floatValue();
            float nX= ndx.floatValue();
            float nY= ndy.floatValue();

            canvas.drawLine(currX, currY, nX, nY, paint);
        }
        //canvas.drawLine(0, 0, 200, 200, paint);
        //canvas.drawLine(200, 0, 0, 200, paint);
    }

    public void setLists(ArrayList<Double> x, ArrayList<Double> y) {
        XList = x;
        YList= y;
    }

}