package com.example.a436;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;

import edu.umd.cmsc436.sheets.Sheets;

import static com.example.a436.MyApp.*;

public class SpiralResults extends SheetsActivity {

    public static final int IMAGE_GALLERY_REQUEST = 20;
    private ImageView resultDisplay;

    String hand;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spiral_results);
        hand = getIntent().getExtras().getString("hand");

        if(hand.equals("left")) {
            Button btn = (Button)findViewById(R.id.toRight);
            btn.setVisibility(View.VISIBLE);
            Button btn2 = (Button)findViewById(R.id.toHome);
            btn2.setVisibility(View.INVISIBLE);
        } else {
            Button btn = (Button)findViewById(R.id.toRight);
            btn.setVisibility(View.INVISIBLE);
            Button btn2 = (Button)findViewById(R.id.toHome);
            btn2.setVisibility(View.VISIBLE);
        }

        //get a reference to an image view that holds an image user will see
        resultDisplay = (ImageView) findViewById(R.id.resultDisplay);
        Double d = (new Double(getIntent().getStringExtra("score")));

        if (!((MyApp) getApplication()).getTestMode()) {
            SharedPreferences pref = getApplicationContext().getSharedPreferences(MyApp.PREF_NAME,
                    Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = pref.edit();
            if(hand.equals("left")) {
                editor.putFloat(SPIRAL_L, (float) d.floatValue());
                editor.commit();
                super.sendToSheets(Sheets.TestType.LH_SPIRAL);

            } else {
                editor.putFloat(SPIRAL_R, (float) d.floatValue());
                editor.commit();
                super.sendToSheets(Sheets.TestType.RH_SPIRAL);
            }
        }

        TextView text = (TextView) findViewById(R.id.textView9);
        text.setText("Score: " + d.toString());
        Uri imageUri = Uri.parse(getIntent().getStringExtra("URL"));

        Log.d("Score", d.toString());

        //declare stream to read image data from SD card
        InputStream inputStream;

        //we are getting input stream based on URI of image
        try {
            inputStream = getContentResolver().openInputStream(imageUri);

            //get bitmap from stream
            Bitmap image = BitmapFactory.decodeStream(inputStream);

            //show image to user
            resultDisplay.setImageBitmap(image);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Toast.makeText(this, "Unable to open image", Toast.LENGTH_LONG).show();
        }


    }

    @Override
    public void onBackPressed() {
        // Do nothing, so back button doesn't work
    }

    public void getPastResults(View view) {
        //invoke immage gallery using implicit intent
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);

        //where to find data
        File pictureDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        String pictureDirectoryPath = pictureDirectory.getPath();

        //get URI representation
        Uri data = Uri.parse(pictureDirectoryPath);

        //set data and type
        photoPickerIntent.setDataAndType(data, "image/png");

        //we invoke activity and get somethign back from it
        startActivityForResult(photoPickerIntent, IMAGE_GALLERY_REQUEST);

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            //if we are here, everything processed successfully
            if (requestCode == IMAGE_GALLERY_REQUEST) {
                //if we are here, we heard back from image gallery

                //address of image on sd card
                Uri imageUri = data.getData();

                //declare stream to read image data from SD card
                InputStream inputStream;

                //we are getting input stream based on URI of image
                try {
                    inputStream = getContentResolver().openInputStream(imageUri);

                    //get bitmap from stream
                    Bitmap image = BitmapFactory.decodeStream(inputStream);

                    //show image to user
                    resultDisplay.setImageBitmap(image);

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Unable to open image", Toast.LENGTH_LONG).show();
                }


            }
        }
    }

    public void toHome(View v) {
        Intent intent = new Intent(SpiralResults.this, MainActivity.class);
        startActivity(intent);
    }

    public void toRightTest(View v) {
        Intent intent = new Intent(SpiralResults.this, DrawingActivity.class);
        intent.putExtra("hand", "right");
        startActivity(intent);
    }

}
