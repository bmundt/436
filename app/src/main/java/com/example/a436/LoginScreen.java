package com.example.a436;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class LoginScreen extends Activity {

    Button login;
    EditText ed1;
    String patientID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_screen);

        login = (Button)findViewById(R.id.button3);
        ed1 = (EditText)findViewById(R.id.editText);
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                patientID = ed1.getText().toString();
                SharedPreferences pref = getPreferences(Context.MODE_PRIVATE);
                // set the values for the different trials
                SharedPreferences.Editor editor = pref.edit();
                editor.putInt("patientID", Integer.parseInt(patientID));
                editor.commit();
                Toast.makeText(getApplicationContext(),
                        "Signing In...",Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(LoginScreen.this, MainActivity.class);
                startActivity(intent);
            }
        });

    }
}
