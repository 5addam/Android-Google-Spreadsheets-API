package com.example.quickstart;

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

import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;

import com.google.api.services.sheets.v4.model.*;

import android.Manifest;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import pl.droidsonroids.gif.GifImageView;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends Activity
        implements EasyPermissions.PermissionCallbacks {
    GoogleAccountCredential mCredential;
    GifImageView gifImageView;
    private TextView mOutputText;
    String accountName;
    String mainSpreadsheetId;
    String ID;

    public static final int TIME_OUT = 4000;
    static final int REQUEST_ACCOUNT_PICKER = 1000;
    static final int REQUEST_AUTHORIZATION = 1001;
    static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;
    static final int REQUEST_PERMISSION_GET_ACCOUNTS = 1003;
    private static final String PREF_ACCOUNT_NAME = "accountName";
    private static final String[] SCOPES = {SheetsScopes.SPREADSHEETS, SheetsScopes.DRIVE};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ID = "";
        mainSpreadsheetId = "14sKsfEbMt5ofCLrC6D8-VqxuiHUMFaqx7yqTmvLxBRc";
        mOutputText = (TextView) findViewById(R.id.txt_output);
        gifImageView = (GifImageView) findViewById(R.id.img_gif);
        mOutputText.setText("");
        gifImageView.setVisibility(View.INVISIBLE);

        mCredential = GoogleAccountCredential.usingOAuth2(
                getApplicationContext(), Arrays.asList(SCOPES))
                .setBackOff(new ExponentialBackOff());

        getResultsFromApi();
    }

    private void getResultsFromApi() {
        if (!isGooglePlayServicesAvailable()) {
            acquireGooglePlayServices();
        } else if (mCredential.getSelectedAccountName() == null) {
            chooseAccount();
        } else if (!isDeviceOnline()) {
            mOutputText.setText("No network connection available.");
        } else {
            mOutputText.setText("You are Logged in");

            SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
            String id = sharedPref.getString("SheetID", "");

            mOutputText.setText(id.toString());
            if (mOutputText.getText() == null || mOutputText.getText() == "") {
                new CreateSheetRequest(mCredential).execute();
//                new CopySheetRequest(mCredential).execute();

            } else {
                gifImageView.setVisibility(View.VISIBLE);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Intent intent = new Intent(MainActivity.this, DataActivity.class);
                        accountName = getPreferences(Context.MODE_PRIVATE)
                                .getString(PREF_ACCOUNT_NAME, null);
                        ID = mOutputText.getText().toString();
                        String values[] = {accountName, ID};
                        intent.putExtra("values", values);
                        startActivity(intent);
                        finish();

                    }
                }, TIME_OUT);

            }
        }
    }


    @AfterPermissionGranted(REQUEST_PERMISSION_GET_ACCOUNTS)
    private void chooseAccount() {
        if (EasyPermissions.hasPermissions(
                this, Manifest.permission.GET_ACCOUNTS)) {
            accountName = getPreferences(Context.MODE_PRIVATE)
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


    @Override
    protected void onActivityResult(
            int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
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

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(
                requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> list) {
        // Do nothing.
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> list) {
        // Do nothing.
    }

    private boolean isDeviceOnline() {
        ConnectivityManager connMgr =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    private boolean isGooglePlayServicesAvailable() {
        GoogleApiAvailability apiAvailability =
                GoogleApiAvailability.getInstance();
        final int connectionStatusCode =
                apiAvailability.isGooglePlayServicesAvailable(this);
        return connectionStatusCode == ConnectionResult.SUCCESS;
    }

    private void acquireGooglePlayServices() {
        GoogleApiAvailability apiAvailability =
                GoogleApiAvailability.getInstance();
        final int connectionStatusCode =
                apiAvailability.isGooglePlayServicesAvailable(this);
        if (apiAvailability.isUserResolvableError(connectionStatusCode)) {
            showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode);
        }
    }


    void showGooglePlayServicesAvailabilityErrorDialog(
            final int connectionStatusCode) {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        Dialog dialog = apiAvailability.getErrorDialog(
                MainActivity.this,
                connectionStatusCode,
                REQUEST_GOOGLE_PLAY_SERVICES);
        dialog.show();
    }

    public Activity getActivity() {
        return MainActivity.this;
    }


    private class CreateSheetRequest extends AsyncTask<Void, Void, Spreadsheet> {
        private com.google.api.services.sheets.v4.Sheets mService = null;
        private Exception mError = null;


        CreateSheetRequest(GoogleAccountCredential credential) {
            HttpTransport transport = AndroidHttp.newCompatibleTransport();
            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
            mService = new Sheets.Builder(
                    transport, jsonFactory, credential)
                    .setApplicationName("Google Sheets API Android Quickstart")
                    .build();
        }

        @Override
        protected Spreadsheet doInBackground(Void... voids) {
            try {
                return createsheet();
            } catch (Exception e) {
                mError = e;
                cancel(true);
                return null;
            }

        }

        private Spreadsheet createsheet() throws Exception {

            Spreadsheet requestbody = new Spreadsheet();
            SpreadsheetProperties properties = new SpreadsheetProperties();
            properties.setTitle("UserSpreadSheet");
            requestbody.setProperties(properties);
            Sheets.Spreadsheets.Create create = mService.spreadsheets().create(requestbody);
            return create.execute();
        }

        @Override
        protected void onPreExecute() {
             //mOutputText.setText("");

        }

        @Override
        protected void onPostExecute(Spreadsheet output) {
            if (output.toString() == null || output.toString().isEmpty()) {
                mOutputText.setText("No results returned.");
            } else {
                mOutputText.setText(output.getSpreadsheetId());
                ID = mOutputText.getText().toString();
                SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString("SheetID", ID);
                editor.commit();
            }
            new CopySheetRequest(mCredential).execute();
        }

        @Override
        protected void onCancelled() {
            if (mError != null) {
                if (mError instanceof GooglePlayServicesAvailabilityIOException) {
                    showGooglePlayServicesAvailabilityErrorDialog(
                            ((GooglePlayServicesAvailabilityIOException) mError)
                                    .getConnectionStatusCode());
                } else if (mError instanceof UserRecoverableAuthIOException) {
                    startActivityForResult(
                            ((UserRecoverableAuthIOException) mError).getIntent(),
                            MainActivity.REQUEST_AUTHORIZATION);
                } else {
                    mOutputText.setText("The following error occurred:\n"
                            + mError.getMessage());
                }
            } else {
                mOutputText.setText("Request cancelled.");
            }
        }
    }


    private class CopySheetRequest extends AsyncTask<Void, Void, SheetProperties> {
        private com.google.api.services.sheets.v4.Sheets mService = null;
        private Exception mError = null;

        CopySheetRequest(GoogleAccountCredential credential) {
            HttpTransport transport = AndroidHttp.newCompatibleTransport();
            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
            mService = new Sheets.Builder(
                    transport, jsonFactory, credential)
                    .setApplicationName("Google Sheets API Android Quickstart")
                    .build();
        }

        @Override
        protected SheetProperties doInBackground(Void... voids) {
            try {
                return copyesheet();
            } catch (Exception e) {
                mError = e;
                cancel(true);
                return null;
            }

        }

        private SheetProperties copyesheet() throws Exception {
            // The ID of the sheet to copy.
            int sheet1ID = 0;
            int sheet2ID = 981594370;

            // The ID of the spreadsheet to copy the sheet to.
            String destinationSpreadsheetId = mOutputText.getText().toString();

            CopySheetToAnotherSpreadsheetRequest requestBody = new CopySheetToAnotherSpreadsheetRequest();
            requestBody.setDestinationSpreadsheetId(destinationSpreadsheetId);

            Sheets.Spreadsheets.SheetsOperations.CopyTo request =
                    mService.spreadsheets().sheets().copyTo(mainSpreadsheetId, sheet1ID, requestBody);
            request.execute();

            CopySheetToAnotherSpreadsheetRequest secondrequestBody = new CopySheetToAnotherSpreadsheetRequest();
            requestBody.setDestinationSpreadsheetId(destinationSpreadsheetId);

            Sheets.Spreadsheets.SheetsOperations.CopyTo secondrequest =
                    mService.spreadsheets().sheets().copyTo(mainSpreadsheetId, sheet2ID, requestBody);

            return secondrequest.execute();
        }

        @Override
        protected void onPreExecute() {
          //  mOutputText.setText("");

        }

        @Override
        protected void onPostExecute(SheetProperties output) {
            if (output.toString() == null || output.toString().isEmpty()) {
                mOutputText.setText("No results returned.");
            } else {

                gifImageView.setVisibility(View.VISIBLE);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Intent intent = new Intent(MainActivity.this, DataActivity.class);
                        accountName = getPreferences(Context.MODE_PRIVATE)
                                .getString(PREF_ACCOUNT_NAME, null);
                        ID = mOutputText.getText().toString();
                        String values[] = {accountName, ID};
                        intent.putExtra("values", values);
                        startActivity(intent);
                        finish();
                    }
                }, TIME_OUT);
            }
        }

        @Override
        protected void onCancelled() {
            if (mError != null) {
                if (mError instanceof GooglePlayServicesAvailabilityIOException) {
                    showGooglePlayServicesAvailabilityErrorDialog(
                            ((GooglePlayServicesAvailabilityIOException) mError)
                                    .getConnectionStatusCode());
                } else if (mError instanceof UserRecoverableAuthIOException) {
                    startActivityForResult(
                            ((UserRecoverableAuthIOException) mError).getIntent(),
                            MainActivity.REQUEST_AUTHORIZATION);
                } else {
                    mOutputText.setText("The following error occurred:\n"
                            + mError.getMessage());
                }
            } else {
                mOutputText.setText("Request cancelled.");
            }
        }
    }

}