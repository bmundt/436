package com.example.a436;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;

import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.ExponentialBackOff;

import com.google.api.services.sheets.v4.SheetsScopes;

import com.google.api.services.sheets.v4.model.*;

import android.Manifest;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class PostResults extends Activity
        implements EasyPermissions.PermissionCallbacks {
    GoogleAccountCredential mCredential;
    private TextView mOutputText;
    private Button mCallApiButton;
    ProgressDialog mProgress;

    static final int REQUEST_ACCOUNT_PICKER = 1000;
    static final int REQUEST_AUTHORIZATION = 1001;
    static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;
    static final int REQUEST_PERMISSION_GET_ACCOUNTS = 1003;

    private static final String BUTTON_TEXT = "Submit Results";
    private static final String PREF_ACCOUNT_NAME = "accountName";
    private static final String[] SCOPES = { SheetsScopes.SPREADSHEETS };

    private Integer patientID;
    private Integer tapsleft1;
    private Integer tapsleft2;
    private Integer tapsleft3;
    private Integer tapsleft4;
    private Integer tapsleft5;

    private Integer tapsright1;
    private Integer tapsright2;
    private Integer tapsright3;
    private Integer tapsright4;
    private Integer tapsright5;

    private Double tapsrightAvg;
    private Double tapsleftAvg;

    private Double rightSpiralResult;
    private Double leftSpiralResult;
    private Double rightLevelResult;
    private Double leftLevelResult;

    private Long leftReaction;
    private long rightReaction;

    /**
     * Create the main activity.
     * @param savedInstanceState previously saved instance data.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LinearLayout activityLayout = new LinearLayout(this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        activityLayout.setLayoutParams(lp);
        activityLayout.setOrientation(LinearLayout.VERTICAL);
        activityLayout.setPadding(16, 16, 16, 16);

        SharedPreferences pref = getApplicationContext().getSharedPreferences(MyApp.PREF_NAME,
                Context.MODE_PRIVATE);
        patientID = pref.getInt("patientID", 0);
        tapsleft1 = pref.getInt("taps left 1", 0);
        tapsleft2 = pref.getInt("taps left 2", 0);
        tapsleft3 = pref.getInt("taps left 3", 0);
        tapsleft4 = pref.getInt("taps left 4", 0);
        tapsleft5 = pref.getInt("taps left 5", 0);

        tapsright1 = pref.getInt("taps right 1", 0);
        tapsright2 = pref.getInt("taps right 2", 0);
        tapsright3 = pref.getInt("taps right 3", 0);
        tapsright4 = pref.getInt("taps right 4", 0);
        tapsright5 = pref.getInt("taps right 5", 0);

        tapsrightAvg = new Double(pref.getFloat("tapsrightAvg", 0.0F));
        tapsleftAvg = new Double(pref.getFloat("tapsleftAvg", 0.0F));

        rightSpiralResult = new Double(pref.getFloat("rightSpiral", 0.0F));
        leftSpiralResult = new Double(pref.getFloat("leftSpiral", 0.0F));
        rightLevelResult = new Double(pref.getFloat("rightlevel", 0.0F));
        leftLevelResult = new Double(pref.getFloat("leftlevel", 0.0F));

        leftReaction = new Long(pref.getLong("average reaction left", 0));
        rightReaction = new Long(pref.getLong("average reaction right", 0));

        ViewGroup.LayoutParams tlp = new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);

        mCallApiButton = new Button(this);
        mCallApiButton.setText(BUTTON_TEXT);
        mCallApiButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCallApiButton.setEnabled(false);
                mOutputText.setText("");
                getResultsFromApi();
                mCallApiButton.setEnabled(true);
            }
        });
        activityLayout.addView(mCallApiButton);

        mOutputText = new TextView(this);
        mOutputText.setLayoutParams(tlp);
        mOutputText.setPadding(16, 16, 16, 16);
        mOutputText.setVerticalScrollBarEnabled(true);
        mOutputText.setMovementMethod(new ScrollingMovementMethod());
        mOutputText.setText(
                "Click the \'" + BUTTON_TEXT +"\' button to test the API.");
        activityLayout.addView(mOutputText);

        mProgress = new ProgressDialog(this);
        mProgress.setMessage("Calling Google Sheets API ...");

        setContentView(activityLayout);

        // Initialize credentials and service object.
        mCredential = GoogleAccountCredential.usingOAuth2(
                getApplicationContext(), Arrays.asList(SCOPES))
                .setBackOff(new ExponentialBackOff());
    }



    /**
     * Attempt to call the API, after verifying that all the preconditions are
     * satisfied. The preconditions are: Google Play Services installed, an
     * account was selected and the device currently has online access. If any
     * of the preconditions are not satisfied, the app will prompt the user as
     * appropriate.
     */
    private void getResultsFromApi() {
        if (! isGooglePlayServicesAvailable()) {
            acquireGooglePlayServices();
        } else if (mCredential.getSelectedAccountName() == null) {
            chooseAccount();
        } else if (! isDeviceOnline()) {
            mOutputText.setText("No network connection available.");
        } else {
            new MakeRequestTask(mCredential).execute();
        }
    }

    /**
     * Attempts to set the account used with the API credentials. If an account
     * name was previously saved it will use that one; otherwise an account
     * picker dialog will be shown to the user. Note that the setting the
     * account to use with the credentials object requires the app to have the
     * GET_ACCOUNTS permission, which is requested here if it is not already
     * present. The AfterPermissionGranted annotation indicates that this
     * function will be rerun automatically whenever the GET_ACCOUNTS permission
     * is granted.
     */
    @AfterPermissionGranted(REQUEST_PERMISSION_GET_ACCOUNTS)
    private void chooseAccount() {
        if (EasyPermissions.hasPermissions(
                this, Manifest.permission.GET_ACCOUNTS)) {
            String accountName = getPreferences(Context.MODE_PRIVATE)
                    .getString(PREF_ACCOUNT_NAME, null);
            if (accountName != null) {
                mCredential.setSelectedAccountName(accountName);
                getResultsFromApi();
            } else {
                // Start a dialog from which the user can choose an account
                startActivityForResult(
                        mCredential.newChooseAccountIntent(),
                        REQUEST_ACCOUNT_PICKER);
            }
        } else {
            // Request the GET_ACCOUNTS permission via a user dialog
            EasyPermissions.requestPermissions(
                    this,
                    "This app needs to access your Google account (via Contacts).",
                    REQUEST_PERMISSION_GET_ACCOUNTS,
                    Manifest.permission.GET_ACCOUNTS);
        }
    }

    /**
     * Called when an activity launched here (specifically, AccountPicker
     * and authorization) exits, giving you the requestCode you started it with,
     * the resultCode it returned, and any additional data from it.
     * @param requestCode code indicating which activity result is incoming.
     * @param resultCode code indicating the result of the incoming
     *     activity result.
     * @param data Intent (containing result data) returned by incoming
     *     activity result.
     */
    @Override
    protected void onActivityResult(
            int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {
            case REQUEST_GOOGLE_PLAY_SERVICES:
                if (resultCode != RESULT_OK) {
                    mOutputText.setText(
                            "This app requires Google Play Services. Please install " +
                                    "Google Play Services on your device and relaunch this app.");
                } else {
                    getResultsFromApi();
                }
                break;
            case REQUEST_ACCOUNT_PICKER:
                if (resultCode == RESULT_OK && data != null &&
                        data.getExtras() != null) {
                    String accountName =
                            data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                    if (accountName != null) {
                        SharedPreferences settings =
                                getPreferences(Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putString(PREF_ACCOUNT_NAME, accountName);
                        editor.apply();
                        mCredential.setSelectedAccountName(accountName);
                        getResultsFromApi();
                    }
                }
                break;
            case REQUEST_AUTHORIZATION:
                if (resultCode == RESULT_OK) {
                    getResultsFromApi();
                }
                break;
        }
    }

    /**
     * Respond to requests for permissions at runtime for API 23 and above.
     * @param requestCode The request code passed in
     *     requestPermissions(android.app.Activity, String, int, String[])
     * @param permissions The requested permissions. Never null.
     * @param grantResults The grant results for the corresponding permissions
     *     which is either PERMISSION_GRANTED or PERMISSION_DENIED. Never null.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(
                requestCode, permissions, grantResults, this);
    }

    /**
     * Callback for when a permission is granted using the EasyPermissions
     * library.
     * @param requestCode The request code associated with the requested
     *         permission
     * @param list The requested permission list. Never null.
     */
    @Override
    public void onPermissionsGranted(int requestCode, List<String> list) {
        // Do nothing.
    }

    /**
     * Callback for when a permission is denied using the EasyPermissions
     * library.
     * @param requestCode The request code associated with the requested
     *         permission
     * @param list The requested permission list. Never null.
     */
    @Override
    public void onPermissionsDenied(int requestCode, List<String> list) {
        // Do nothing.
    }

    /**
     * Checks whether the device currently has a network connection.
     * @return true if the device has a network connection, false otherwise.
     */
    private boolean isDeviceOnline() {
        ConnectivityManager connMgr =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    /**
     * Check that Google Play services APK is installed and up to date.
     * @return true if Google Play Services is available and up to
     *     date on this device; false otherwise.
     */
    private boolean isGooglePlayServicesAvailable() {
        GoogleApiAvailability apiAvailability =
                GoogleApiAvailability.getInstance();
        final int connectionStatusCode =
                apiAvailability.isGooglePlayServicesAvailable(this);
        return connectionStatusCode == ConnectionResult.SUCCESS;
    }

    /**
     * Attempt to resolve a missing, out-of-date, invalid or disabled Google
     * Play Services installation via a user dialog, if possible.
     */
    private void acquireGooglePlayServices() {
        GoogleApiAvailability apiAvailability =
                GoogleApiAvailability.getInstance();
        final int connectionStatusCode =
                apiAvailability.isGooglePlayServicesAvailable(this);
        if (apiAvailability.isUserResolvableError(connectionStatusCode)) {
            showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode);
        }
    }


    /**
     * Display an error dialog showing that Google Play Services is missing
     * or out of date.
     * @param connectionStatusCode code describing the presence (or lack of)
     *     Google Play Services on this device.
     */
    void showGooglePlayServicesAvailabilityErrorDialog(
            final int connectionStatusCode) {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        Dialog dialog = apiAvailability.getErrorDialog(
                PostResults.this,
                connectionStatusCode,
                REQUEST_GOOGLE_PLAY_SERVICES);
        dialog.show();
    }

    /**
     * An asynchronous task that handles the Google Sheets API call.
     * Placing the API calls in their own task ensures the UI stays responsive.
     */
    private class MakeRequestTask extends AsyncTask<Void, Void, List<String>> {
        private com.google.api.services.sheets.v4.Sheets mService = null;
        private Exception mLastError = null;

        MakeRequestTask(GoogleAccountCredential credential) {
            HttpTransport transport = AndroidHttp.newCompatibleTransport();
            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
            mService = new com.google.api.services.sheets.v4.Sheets.Builder(
                    transport, jsonFactory, credential)
                    .setApplicationName("Google Sheets API Android Quickstart")
                    .build();
        }

        /**
         * Background task to call Google Sheets API.
         * @param params no parameters needed for this task.
         */
        @Override
        protected List<String> doInBackground(Void... params) {
            try {
                return getDataFromApi();
            } catch (Exception e) {
                mLastError = e;
                cancel(true);
                return null;
            }
        }

        /**
         * Fetch a list of names and majors of students in a sample spreadsheet:
         * https://docs.google.com/spreadsheets/d/1BxiMVs0XRA5nFMdKvBdBZjgmUUqptlbs74OgvE2upms/edit
         * @return List of names and majors
         * @throws IOException
         */
        private List<String> getDataFromApi() throws IOException {
            /*String spreadsheetId = "13fbFdZSWjjP_DakUX2liVe8pgM4PovPHA96PNd2YYMk";
            String range = "Class Data!A2:B";
            List<String> results = new ArrayList<String>();
            ValueRange response = this.mService.spreadsheets().values()
                    .get(spreadsheetId, range)
                    .execute();
            List<List<Object>> values = response.getValues();
            if (values != null) {
                results.add("Id, Taps");
                for (List row : values) {
                    results.add(row.get(0) + ", " + row.get(1));
                }
            }
            return results;*/
            String spreadsheetId = "1YvI3CjS4ZlZQDYi5PaiA7WGGcoCsZfLoSFM0IdvdbDU";

            String range1 = "Tapping Test (LH)!A1:G1";
            String range2 = "Tapping Test (RH)!A1:G1";
            String range3 = "Tapping Test (LF)!A1:G1";
            String range4 = "Tapping Test (RF)!A1:G1";
            String range5 = "Spiral Test (LH)!A1:J1";
            String range6 = "Spiral Test (RH)!A1:J1";
            String range7 = "Balloon Test (LH)!A1:F1";
            String range8 = "Balloon Test (RH)!A1:F1";
            String range9 = "Level Test (LH)!A1:G1";
            String range10 = "Level Test (RH)!A1:G1";



            List<String> results = new ArrayList<String>();
            results.add("Results added to spreadsheet");
            List<List<Object>> values = new ArrayList<List<Object>>();
            List<Object> data1 = new ArrayList<Object>();
            List<Object> data2 = new ArrayList<Object>();
            List<Object> data3 = new ArrayList<Object>();
            List<Object> data4 = new ArrayList<Object>();
            List<Object> data5 = new ArrayList<Object>();
            List<Object> data6 = new ArrayList<Object>();
            List<Object> data7 = new ArrayList<Object>();
            List<Object> data8 = new ArrayList<Object>();
            List<Object> data9 = new ArrayList<Object>();
            List<Object> data10 = new ArrayList<Object>();

            Calendar calendar = Calendar.getInstance(Locale.getDefault());
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int minute = calendar.get(Calendar.MINUTE);
            //int second = calendar.get(Calendar.SECOND);
            int date = calendar.get(Calendar.DAY_OF_MONTH);
            int month = calendar.get(Calendar.MONTH) + 1;
            int year = calendar.get(Calendar.YEAR);
            String dateStr = "" + month + "/" + date + "/" + year + " at " + hour + ":" + minute;

            SharedPreferences pref = getApplicationContext().getSharedPreferences(MyApp.PREF_NAME,
                    Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = pref.edit();

            int day = pref.getInt("day",1);

            data1.add("p" + patientID+"t02");
            data1.add(dateStr);
            data1.add(day);
            data1.add(tapsleftAvg);
            data1.add("");
            data1.add("");


            data2.add("p" + patientID+"t02");
            data2.add(dateStr);
            data2.add(day);
            data2.add(tapsrightAvg);
            data2.add("");
            data2.add("");

            data5.add("p" + patientID+"t02");
            data5.add(dateStr);
            data5.add(day);
            data5.add("");
            data5.add("");
            data5.add("");
            data5.add(leftSpiralResult);

            data6.add("p" + patientID+"t02");
            data6.add(dateStr);
            data6.add(day);
            data6.add("");
            data6.add("");
            data6.add("");
            data6.add(rightSpiralResult);

            data7.add("p" + patientID+"t02");
            data7.add(dateStr);
            data7.add(day);
            data7.add(10);
            data7.add(leftReaction);
            data7.add("");

            data8.add("p" + patientID+"t02");
            data8.add(dateStr);
            data8.add(day);
            data8.add(10);
            data8.add(rightReaction);
            data8.add("");

            data9.add("p" + patientID+"t02");
            data9.add(dateStr);
            data9.add(day);
            data9.add(leftLevelResult);
            data9.add("");
            data9.add("");

            data10.add("p" + patientID+"t02");
            data10.add(dateStr);
            data10.add(day);
            data10.add(rightLevelResult);
            data10.add("");
            data10.add("");

            editor.putInt("day", day+1);
            editor.commit();

            //There are obviously more dynamic ways to do these, but you get the picture
            ValueRange valueRange;

            values = new ArrayList<List<Object>>();
            values.add(data1);
            valueRange = new ValueRange();
            valueRange.setMajorDimension("ROWS");
            valueRange.setRange(range1);
            valueRange.setValues(values);
            this.mService.spreadsheets().values().append(spreadsheetId, range1, valueRange).setValueInputOption("RAW")
                    .execute();

            values = new ArrayList<List<Object>>();
            values.add(data2);
            valueRange = new ValueRange();
            valueRange.setMajorDimension("ROWS");
            valueRange.setRange(range2);
            valueRange.setValues(values);
            this.mService.spreadsheets().values().append(spreadsheetId, range2, valueRange).setValueInputOption("RAW")
                    .execute();

            values = new ArrayList<List<Object>>();
            values.add(data6);
            valueRange = new ValueRange();
            valueRange.setMajorDimension("ROWS");
            valueRange.setRange(range6);
            valueRange.setValues(values);
            this.mService.spreadsheets().values().append(spreadsheetId, range6, valueRange).setValueInputOption("RAW")
                    .execute();

            values = new ArrayList<List<Object>>();
            values.add(data7);
            valueRange = new ValueRange();
            valueRange.setMajorDimension("ROWS");
            valueRange.setRange(range7);
            valueRange.setValues(values);
            this.mService.spreadsheets().values().append(spreadsheetId, range7, valueRange).setValueInputOption("RAW")
                    .execute();

            values = new ArrayList<List<Object>>();
            values.add(data8);
            valueRange = new ValueRange();
            valueRange.setMajorDimension("ROWS");
            valueRange.setRange(range8);
            valueRange.setValues(values);
            this.mService.spreadsheets().values().append(spreadsheetId, range8, valueRange).setValueInputOption("RAW")
                    .execute();

            values = new ArrayList<List<Object>>();
            values.add(data10);
            valueRange = new ValueRange();
            valueRange.setMajorDimension("ROWS");
            valueRange.setRange(range10);
            valueRange.setValues(values);
            this.mService.spreadsheets().values().append(spreadsheetId, range10, valueRange).setValueInputOption("RAW")
                    .execute();
            /*this.mService.spreadsheets().values().update(spreadsheetId, range, valueRange)
                    .setValueInputOption("USER_ENTERED")
                    .execute();*/

            return results;
        }



        @Override
        protected void onPreExecute() {
            mOutputText.setText("");
            mProgress.show();
        }

        @Override
        protected void onPostExecute(List<String> output) {
            mProgress.hide();
            if (output == null || output.size() == 0) {
                mOutputText.setText("No results returned.");
            } else {
                output.add(0, "Data retrieved using the Google Sheets API:");
                mOutputText.setText(TextUtils.join("\n", output));
            }
        }

        @Override
        protected void onCancelled() {
            mProgress.hide();
            if (mLastError != null) {
                if (mLastError instanceof GooglePlayServicesAvailabilityIOException) {
                    showGooglePlayServicesAvailabilityErrorDialog(
                            ((GooglePlayServicesAvailabilityIOException) mLastError)
                                    .getConnectionStatusCode());
                } else if (mLastError instanceof UserRecoverableAuthIOException) {
                    startActivityForResult(
                            ((UserRecoverableAuthIOException) mLastError).getIntent(),
                            PostResults.REQUEST_AUTHORIZATION);
                } else {
                    mOutputText.setText("The following error occurred:\n"
                            + mLastError.getMessage());
                }
            } else {
                mOutputText.setText("Request cancelled.");
            }
        }
    }
}