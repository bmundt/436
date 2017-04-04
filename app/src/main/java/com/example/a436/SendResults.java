package com.example.a436;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;

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
import static com.example.a436.MyApp.*;

public class SendResults extends AppCompatActivity implements EasyPermissions.PermissionCallbacks {

    GoogleAccountCredential mCredential;
    private TextView mOutputText;
    private Button mCallApiButton;
    ProgressDialog mProgress;

    static final int REQUEST_ACCOUNT_PICKER = 1000;
    static final int REQUEST_AUTHORIZATION = 1001;
    static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;
    static final int REQUEST_PERMISSION_GET_ACCOUNTS = 1003;

    private static final String BUTTON_TEXT = "Call Google Sheets API";
    private static final String PREF_ACCOUNT_NAME = "accountName";
    private static final String[] SCOPES = { SheetsScopes.SPREADSHEETS };

    private float updateValue;
    private String sheetID;
    private String userID;
    private MyApp myApp;
    private final static String LOG_TAG = "SEND_RESULTS";

    SharedPreferences pref;

    final public static String EXTRA_VALUE = "com.example.sheets436.VALUE";
    final public static String EXTRA_USER = "com.example.sheets436.USER";
    final public static String EXTRA_TYPE = "com.example.sheets436.TYPE";

    final private static String spreadsheetID = "13fbFdZSWjjP_DakUX2liVe8pgM4PovPHA96PNd2YYMk";

    public enum UpdateType {
        LH_TAP, RH_TAP,
        LH_SPIRAL, RH_SPIRAL,
        LH_LEVEL, RH_LEVEL,
        LH_POP, RH_POP,
        LH_CURL, RH_CURL
    }

    private UpdateType updateType;

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

        ViewGroup.LayoutParams tlp = new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);

        myApp = (MyApp) getApplication();
        pref = getApplicationContext().getSharedPreferences(MyApp.PREF_NAME,
                Context.MODE_PRIVATE);

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

        // Retrieve information from calling activity
        Intent intent = getIntent();
        updateValue = intent.getFloatExtra(EXTRA_VALUE, 0);

        userID = "p" + pref.getInt(PID_STR, -1) + "t02";
        if (userID == null || userID == "p-1t02") {
            Log.d("SEND_RESULTS", "patient id not found");
            finish();
        }
        updateType = UpdateType.values()[intent.getIntExtra(EXTRA_TYPE, 0)];
        sheetID = getSheetID(UpdateType.values()[intent.getIntExtra(EXTRA_TYPE, 0)]);
        if (sheetID == null) {
            finish();
        }

        // Initialize credentials and service object.
        mCredential = GoogleAccountCredential.usingOAuth2(
                getApplicationContext(), Arrays.asList(SCOPES))
                .setBackOff(new ExponentialBackOff());
    }

    /**
     * Gets a ID for the sheet to be updated.
     * @param type the enum representing which test type has results to write to sheets
     * @return string representing the sheet in A1 format
     */
    private String getSheetID(UpdateType type) {
        switch (type) {
            case LH_TAP:
                return "'Tapping Test (LH)'";
            case RH_TAP:
                return "'Tapping Test (RH)'";
            case LH_SPIRAL:
                return "'Spiral Test (LH)'";
            case RH_SPIRAL:
                return "'Spiral Test (RH)'";
            case LH_LEVEL:
                return "'Level Test (LH)'";
            case RH_LEVEL:
                return "'Level Test (RH)'";
            case LH_POP:
                return "'Balloon Test (LH)'";
            case RH_POP:
                return "'Balloon Test (RH)'";
            case RH_CURL:
                return "'Curling Test (RH)'";
            case LH_CURL:
                return "'Curling Test (LH)'";
            default:
                return null;
        }
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
        } else if (!isDeviceOnline()) {
            mOutputText.setText(R.string.no_net);
        } else {
            Log.d("SEND_RESULTS", mCredential.getSelectedAccountName());
            new MakeRequestTask(mCredential).execute();
            finish();
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
            Log.d("SEND_RESULTS", "NO Permissions");
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
                    mOutputText.setText(R.string.no_goog);
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
        Log.d(LOG_TAG, "permission granted");
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
        Log.d(LOG_TAG, "permission denied");
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
            Log.d(LOG_TAG, "cannot acquire google play services");
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
                SendResults.this,
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
                writeToSheet();
                return null;
            } catch (Exception e) {
                Log.d(LOG_TAG, "Exception: " + e.toString());
                cancel(true);
                return null;
            }
        }

        // @throws IOException
        private void writeToSheet() throws IOException {
            Calendar calendar = Calendar.getInstance(Locale.getDefault());
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int minute = calendar.get(Calendar.MINUTE);
            //int second = calendar.get(Calendar.SECOND);
            int date = calendar.get(Calendar.DAY_OF_MONTH);
            int month = calendar.get(Calendar.MONTH) + 1;
            int year = calendar.get(Calendar.YEAR);
            String dateStr = "" + month + "/" + date + "/" + year + " at " + hour + ":" + minute;


            SharedPreferences.Editor editor = pref.edit();

            int day = pref.getInt("day",1);

            Intent intent = getIntent();

            Log.d("SEND_RESULTS", "In Write To Sheet");


            // data to send to insert
            List<String> results = new ArrayList<String>();
            results.add("Results added to spreasheet");
            List<List<Object>> values = new ArrayList<List<Object>>();
            List<Object> data = new ArrayList<Object>();
            data.add(userID);
            data.add(dateStr);
            String range = "";
            Intent sheetsIntent = new Intent(SendResults.this, com.example.a436.Sheets.class);
            switch(updateType) {
                case LH_TAP:
                    for (int i = 0; i <= myApp.getNumTrials(); i++) {
                        data.add(pref.getInt(TAPS + "_LEFT_" + i, 0));
                    }
                    float avgLTaps = pref.getFloat(TAPS_L_AVG, 0.0F);
                    data.add(avgLTaps);
                    range = "Tapping Test (LH) !A1:F1";
                    sheetsIntent.putExtra(Sheets.EXTRA_TYPE, Sheets.UpdateType.LH_TAP.ordinal());
                    sheetsIntent.putExtra(Sheets.EXTRA_USER, userID);
                    sheetsIntent.putExtra(Sheets.EXTRA_VALUE, avgLTaps);
                    break;
                case RH_TAP:
                    for (int i = 0; i <= myApp.getNumTrials(); i++) {
                        data.add(pref.getInt(TAPS + "_RIGHT_" + i, 0));
                    }
                    range = "Tapping Test (RH) !A1:F1";
                    float avgRTaps = pref.getFloat(TAPS_L_AVG, 0.0F);
                    data.add(avgRTaps);
                    sheetsIntent.putExtra(Sheets.EXTRA_TYPE, Sheets.UpdateType.RH_TAP.ordinal());
                    sheetsIntent.putExtra(Sheets.EXTRA_USER, userID);
                    sheetsIntent.putExtra(Sheets.EXTRA_VALUE, avgRTaps);
                    break;
                case LH_SPIRAL:
                    data.add(pref.getFloat(SPIRAL_L, 0.0F));
                    range = "Spiral Test (LH)!A1:C1";
                    sheetsIntent.putExtra(Sheets.EXTRA_TYPE, Sheets.UpdateType.LH_SPIRAL.ordinal());
                    sheetsIntent.putExtra(Sheets.EXTRA_USER, userID);
                    sheetsIntent.putExtra(Sheets.EXTRA_VALUE, pref.getFloat(SPIRAL_L, 0.0F));
                    break;
                case RH_SPIRAL:
                    data.add(pref.getFloat(SPIRAL_R, 0.0F));
                    range = "Spiral Test (RH)!A1:C1";
                    sheetsIntent.putExtra(Sheets.EXTRA_TYPE, Sheets.UpdateType.RH_SPIRAL.ordinal());
                    sheetsIntent.putExtra(Sheets.EXTRA_USER, userID);
                    sheetsIntent.putExtra(Sheets.EXTRA_VALUE, pref.getFloat(SPIRAL_R, 0.0F));
                    break;
                case LH_LEVEL:
                    data.add(pref.getFloat(LEVEL_L, 0.0F));
                    range = "Level Test (LH)!A1:C1";
                    sheetsIntent.putExtra(Sheets.EXTRA_TYPE, Sheets.UpdateType.LH_LEVEL.ordinal());
                    sheetsIntent.putExtra(Sheets.EXTRA_USER, userID);
                    sheetsIntent.putExtra(Sheets.EXTRA_VALUE, pref.getFloat(LEVEL_L, 0.0F));
                    break;
                case RH_LEVEL:
                    data.add(pref.getFloat(LEVEL_R, 0.0F));
                    range = "Level Test (RH)!A1:C1";
                    sheetsIntent.putExtra(Sheets.EXTRA_TYPE, Sheets.UpdateType.RH_LEVEL.ordinal());
                    sheetsIntent.putExtra(Sheets.EXTRA_USER, userID);
                    sheetsIntent.putExtra(Sheets.EXTRA_VALUE, pref.getFloat(LEVEL_R, 0.0F));
                    break;
                case LH_POP:
                    data.add(pref.getFloat(REACTION_L, 0.0F));
                    sheetsIntent.putExtra(Sheets.EXTRA_TYPE, Sheets.UpdateType.LH_POP.ordinal());
                    sheetsIntent.putExtra(Sheets.EXTRA_USER, userID);
                    sheetsIntent.putExtra(Sheets.EXTRA_VALUE, pref.getFloat(REACTION_L, 0.0F));
                    range = "Balloon Test (LH)!A1:C1";
                    break;
                case RH_POP:
                    data.add(pref.getFloat(REACTION_R, 0.0F));
                    range = "Balloon Test (RH)!A1:C1";
                    sheetsIntent.putExtra(Sheets.EXTRA_TYPE, Sheets.UpdateType.RH_POP.ordinal());
                    sheetsIntent.putExtra(Sheets.EXTRA_USER, userID);
                    sheetsIntent.putExtra(Sheets.EXTRA_VALUE, pref.getFloat(REACTION_R, 0.0F));
                    break;
                case LH_CURL:
                    range = "Curl Test (LH)!A1:C1";
                    data.add(pref.getLong(CURL_L, 0L));
                    sheetsIntent.putExtra(Sheets.EXTRA_TYPE, Sheets.UpdateType.LH_CURL.ordinal());
                    sheetsIntent.putExtra(Sheets.EXTRA_USER, userID);
                    sheetsIntent.putExtra(Sheets.EXTRA_VALUE, pref.getFloat(CURL_L, 0.0F));
                    break;
                case RH_CURL:
                    range = "Curl Test (RH) !A1:C1";
                    data.add(pref.getLong(CURL_R, 0L));
                    sheetsIntent.putExtra(Sheets.EXTRA_TYPE, Sheets.UpdateType.RH_CURL.ordinal());
                    sheetsIntent.putExtra(Sheets.EXTRA_USER, userID);
                    sheetsIntent.putExtra(Sheets.EXTRA_VALUE, pref.getFloat(CURL_R, 0.0F));
                    break;
            }
            values.add(data);
            ValueRange valueRange = new ValueRange();
            valueRange.setMajorDimension("ROWS");
            valueRange.setRange(range);
            valueRange.setValues(values);
            Log.d("SEND_RESULTS", "about to send results to our sheet");

            AppendValuesResponse response = mService.spreadsheets().values().append(spreadsheetID, range, valueRange)
                    .setValueInputOption("RAW").execute();
            Log.d(LOG_TAG, response.toString());
            Log.d("SEND_RESULTS", "sent data to our spreadsheet");
            startActivity(sheetsIntent);
            Log.d("SEND_RESULTS", "sent data to their spreadsheet");
        }
    }


        /**
         * Writes new trial value to the userID and sheetID specified by an intent.
         * The spreadsheet is located at
         */
//        private void writeToSheet() throws IOException {
//            ValueRange response = this.mService.spreadsheets().values()
//                    .get(spreadsheetID, sheetID + "!A2:A")
//                    .execute();
//            List<List<Object>> sheet = response.getValues();
//            int rowIdx = 2;
//            if (sheet != null) {
//                for (List row : sheet) {
//                    if (row.get(0).equals(userID)) {
//                        break;
//                    }
//                    rowIdx++;
//                }
//            }
//
//            response = this.mService.spreadsheets().values()
//                    .get(spreadsheetID, sheetID + "!" + rowIdx + ":" + rowIdx)
//                    .execute();
//            sheet = response.getValues();
//            String colIdx = "A";
//            if (sheet != null) {
//                colIdx = columnToLetter(sheet.get(0).size() + 1);
//            }
//
//            String updateCell = sheetID + "!" + colIdx + rowIdx;
//            List<List<Object>> values = new ArrayList<>();
//            List<Object> row = new ArrayList<>();
//
//            if (colIdx.equals("A")) {
//                row.add(userID);
//                updateCell += ":B" + rowIdx;
//            }
//
//            row.add(updateValue);
//            values.add(row);
//
//            ValueRange valueRange = new ValueRange();
//            valueRange.setValues(values);
//
//            // Call the API
//            this.mService.spreadsheets().values()
//                    .update(spreadsheetID, updateCell, valueRange)
//                    .setValueInputOption("RAW")
//                    .execute();
//        }
//    }

    private String columnToLetter(int column) {
        int temp;
        String letter = "";
        while (column > 0)
        {
            temp = (column - 1) % 26;
            letter = ((char)(temp + 65)) + letter;
            column = (column - temp - 1) / 26;
        }
        return letter;
    }
}
