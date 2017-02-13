package com.example.a436;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class SpiralResults extends AppCompatActivity {

    public static final int IMAGE_GALLERY_REQUEST = 20;
    private ImageView resultDisplay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spiral_results);

        //get a reference to an image view that holds an image user will see
        resultDisplay = (ImageView) findViewById(R.id.resultDisplay);


        Uri imageUri = Uri.parse(getIntent().getStringExtra("URL"));

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
        startActivityForResult(photoPickerIntent,  IMAGE_GALLERY_REQUEST);

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK){
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
}
