package com.example.a436;

import android.graphics.BitmapFactory;
import android.graphics.PorterDuff;
import android.media.Image;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.content.Context;
import android.util.AttributeSet;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.view.MotionEvent;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.BitmapDrawable;
import android.widget.ImageView;

import java.util.ArrayList;

/**
 * Created by brmun on 2/11/2017.
 */

public class DrawingView extends View {
    private Path drawPath;
    private Paint drawPaint, canvasPaint;
    private int paintColor = 0xFF3CBA2B;
    private Canvas drawCanvas;
    private Bitmap canvasBitmap;

    private ArrayList<Float> xCoords = new ArrayList<Float>();
    private ArrayList<Float> yCoords = new ArrayList<Float>();
    private ArrayList<Float> pressures = new ArrayList<Float>();

    private DrawingActivity activity = (DrawingActivity) getContext();

    public DrawingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setupDrawing();
    }

    private void setupDrawing() {
        drawPath = new Path();
        drawPaint = new Paint();

        drawPaint.setColor(paintColor);

        drawPaint.setAntiAlias(true);
        drawPaint.setStrokeWidth(20);
        drawPaint.setStyle(Paint.Style.STROKE);
        drawPaint.setStrokeJoin(Paint.Join.ROUND);
        drawPaint.setStrokeCap(Paint.Cap.ROUND);

        canvasPaint = new Paint(Paint.DITHER_FLAG);
    }

    public void startOver() {
        drawCanvas.drawColor(0, PorterDuff.Mode.CLEAR);
        invalidate();
        destroyDrawingCache();
        xCoords = new ArrayList<Float>();
        yCoords = new ArrayList<Float>();
        pressures = new ArrayList<Float>();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldW, int oldH) {
        w = ((ImageView) activity.findViewById(R.id.imageView)).getWidth();
        h = ((ImageView) activity.findViewById(R.id.imageView)).getHeight();
        Log.d("DrawingView", "width: " + w + " height: " + h);
        super.onSizeChanged(w, h, oldW, oldH);
        canvasBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        drawCanvas = new Canvas(canvasBitmap);

    }

    @Override
    protected void onDraw(Canvas canvas) {
//        Drawable spiralDrawable = ((ImageView) activity.findViewById(R.id.imageView)).getDrawable();
//        Bitmap bitmap = ((BitmapDrawable) spiralDrawable).getBitmap();
//        bitmap = Bitmap.createScaledBitmap(bitmap, getWidth(),getHeight(), false);
//        canvas.drawBitmap(bitmap, 0, 0, null);
        canvas.drawBitmap(canvasBitmap, 0, 0, canvasPaint);
        canvas.drawPath(drawPath, drawPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // here you can record the X, Y, and Pressure
        float touchX = event.getX();
        float touchY = event.getY();
        float pressure = event.getPressure();

        xCoords.add(touchX);
        yCoords.add(touchY);
        pressures.add(pressure);

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                drawPath.moveTo(touchX, touchY);
                break;
            case MotionEvent.ACTION_MOVE:
                drawPath.lineTo(touchX, touchY);
                break;
            case MotionEvent.ACTION_UP:
                drawCanvas.drawPath(drawPath, drawPaint);
                drawPath.reset();
                break;
            default:
                return false;
        }
        invalidate();
        return true;
    }

    public double score(ImageView spiralImage){

        Drawable spiralDrawable = spiralImage.getDrawable();
        Bitmap bitmap = ((BitmapDrawable) spiralDrawable).getBitmap();
        bitmap = Bitmap.createScaledBitmap(bitmap, getWidth(),getHeight(), false);


        int score = 0;

        for(int i = 0; i < xCoords.size(); i++){

            Log.d("Coordinates", Math.round(xCoords.get(i)) + " " +Math.round(yCoords.get(i)));
            try{
                int colorValue = bitmap.getPixel(Math.round(xCoords.get(i)), Math.round(yCoords.get(i)));
                String s =  Integer.toHexString(colorValue);
                if(colorValue != 0){
                    score++;
                    Log.d("Non-White",s);
                } else {
                    Log.d("White",s);
                }
            } catch (Exception IllegalStateException){

            }

        }

        if (xCoords.size() == 0) return 0;
        else return (double)score/((double)xCoords.size());

    }

}
