package com.example.quickstart;

import com.google.android.gms.common.GoogleApiAvailability;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.Data;
import com.google.api.client.util.ExponentialBackOff;

import android.graphics.*;
import android.graphics.Color;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;

import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.*;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.method.ScrollingMovementMethod;
import android.view.DragEvent;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DataActivity extends AppCompatActivity {
    static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;
    private static final String[] SCOPES = {SheetsScopes.SPREADSHEETS};
    public TextView mOutputText;
    public EditText e_name;
    public List<String> spiner_values;
    GoogleAccountCredential mCredential;
    ProgressDialog mProgress;
    RelativeLayout relativeLayout1;
    CardView viewAdd;
    String accountName;
    RecyclerView recyclerView;
    Button btn_add2sheet;
    Spinner spinner;
    int position;
    EditText date;
    EditText price;
    RelativeLayout relativeLayout;
    Button cancel;
    int categorySheetStatus;
    CardView setting_layout;
    Button settingOK;
    Button settingCancel;
    String spreadsheetId;
    EditText edt_SheetID;
    ImageView icon;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.actions, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @SuppressLint("ClickableViewAccessibility")
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data);

        Toolbar toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        categorySheetStatus = 0;

        mCredential = GoogleAccountCredential.usingOAuth2(
                getApplicationContext(), Arrays.asList(SCOPES))
                .setBackOff(new ExponentialBackOff());

        Intent intent = getIntent();
        String values[] = intent.getStringArrayExtra("values");
        accountName = values[0];
        spreadsheetId = values[1];
        mCredential.setSelectedAccountName(accountName);

        relativeLayout = (RelativeLayout) findViewById(R.id.view_rel);
        mOutputText = (TextView) findViewById(R.id.txt_output);
        relativeLayout1 = (RelativeLayout) findViewById(R.id.view_child);
        viewAdd = (CardView) findViewById(R.id.view_addRow);
        btn_add2sheet = (Button) findViewById(R.id.btn_addToSheet);
        date = (EditText) findViewById(R.id.edt_date);
        price = (EditText) findViewById(R.id.edt_price);
        e_name = (EditText) findViewById(R.id.edt_name);
        spinner = (Spinner) findViewById(R.id.dropbox);
        cancel = (Button) findViewById(R.id.btn_cancel);
        recyclerView = (RecyclerView) findViewById(R.id.view_recycler);
        setting_layout = (CardView) findViewById(R.id.view_setting);
        settingOK = (Button) findViewById(R.id.btn_submitSheetID);
        settingCancel = (Button) findViewById(R.id.btn_cancelSheetID);
        edt_SheetID = (EditText) findViewById(R.id.edt_sheetID);
        icon = (ImageView) findViewById(R.id.img_name);

        relativeLayout1.setVisibility(View.VISIBLE);
        viewAdd.setVisibility(View.INVISIBLE);
        setting_layout.setVisibility(View.INVISIBLE);
        edt_SheetID.setText(spreadsheetId);

        mOutputText.setVerticalScrollBarEnabled(true);
        mOutputText.setMovementMethod(new ScrollingMovementMethod());
        mProgress = new ProgressDialog(this);
        mProgress.setMessage("Calling Google Sheets API ...");
        setContentView(relativeLayout);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
               if(spinner.getSelectedItem().toString().equals("Food")){
                   icon.setImageResource(R.drawable.food);
               }
                if(spinner.getSelectedItem().toString().equals("Groceries")){
                    icon.setImageResource(R.drawable.groceries);
                }
                if(spinner.getSelectedItem().toString().equals("Clothes")){
                    icon.setImageResource(R.drawable.clothes);
                }
                if(spinner.getSelectedItem().toString().equals("Dairy Products")){
                    icon.setImageResource(R.drawable.dairyproducts);
                }
                if(spinner.getSelectedItem().toString().equals("Accessories")){
                    icon.setImageResource(R.drawable.accessories);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        btn_add2sheet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (date.getText().toString() == "" ||
                        spinner.getSelectedItem().toString() == "Categories"
                        || e_name.getText().toString() == ""
                        || price.getText().toString() == "") {
                    Toast.makeText(DataActivity.this, "Please fill all fields.", Toast.LENGTH_SHORT).show();
                } else {
                    mProgress.setMessage("Calling Google Sheets API ...");
                    AddResultsToApi();
                    viewAdd.setVisibility(View.INVISIBLE);
                }
            }
        });
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                price.setText("");
                e_name.setText("");
                date.setText("");
                spinner.setSelection(0);
                viewAdd.setVisibility(View.INVISIBLE);
            }
        });
        settingCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setting_layout.setVisibility(View.INVISIBLE);
            }
        });

        settingOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                spreadsheetId = edt_SheetID.getText().toString();
                getResultsFromApi();
            }
        });

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btn_add2sheet.setText("Add");
                date.setText(new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date()), TextView.BufferType.EDITABLE);
                viewAdd.setVisibility(View.VISIBLE);
            }
        });
        getResultsFromApi();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                setting_layout.setVisibility(View.VISIBLE);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void getResultsFromApi() {
        new DataRetrivalTask(mCredential).execute();
    }
    private void AddResultsToApi() {
        new UpdateSheetRequest(mCredential).execute();
    }

    void showGooglePlayServicesAvailabilityErrorDialog(
            final int connectionStatusCode) {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        Dialog dialog = apiAvailability.getErrorDialog(
                DataActivity.this,
                connectionStatusCode,
                REQUEST_GOOGLE_PLAY_SERVICES);
        dialog.show();
    }

    public void Update(int pos, String edt_date, String edt_category, String edt_name, String edt_price) {
        position = pos + 1;
        date.setText(edt_date);
        e_name.setText(edt_name);
        price.setText(edt_price);

        spinner.setSelection(getIndex(spinner, edt_category));


        // getResultsApi();
        btn_add2sheet.setText("Update");
        viewAdd.setVisibility(View.VISIBLE);


    }

    private int getIndex(Spinner spinner, String myString) {
        for (int i = 0; i < spinner.getCount(); i++) {
            if (spinner.getItemAtPosition(i).toString().equalsIgnoreCase(myString)) {
                return i;
            }
        }
        return 0;
    }

    private class DataRetrivalTask extends AsyncTask<Void, Void, List<String>> {
        public List<String> categories;
        public List<String> name;
        public List<String> price;
        private com.google.api.services.sheets.v4.Sheets mService = null;
        private Exception mLastError = null;

        DataRetrivalTask(GoogleAccountCredential credential) {
            HttpTransport transport = AndroidHttp.newCompatibleTransport();
            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
            mService = new com.google.api.services.sheets.v4.Sheets.Builder(
                    transport, jsonFactory, credential)
                    .setApplicationName("Google Sheets API Android Quickstart")
                    .build();
        }

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

        private List<String> getDataFromApi() throws IOException {
            String dataRange = "Copy of Data!A2:D";
            String categoriesRange = "Copy of Categories!A2:B";
            spiner_values = new ArrayList<String>();
            List<String> date = new ArrayList<String>();
            categories = new ArrayList<String>();
            name = new ArrayList<String>();
            price = new ArrayList<String>();

            ValueRange dataResponse = this.mService.spreadsheets().values()
                    .get(spreadsheetId, dataRange)
                    .execute();

            List<List<Object>> dataValues = dataResponse.getValues();
            if (dataValues != null) {
                date.add("Date");
                categories.add("Category");
                name.add("Name");
                price.add("Price");
                for (List row : dataValues) {
                    date.add(row.get(0).toString());
                    categories.add(row.get(1).toString());
                    name.add(row.get(2).toString());
                    price.add(row.get(3).toString());
                }
            }

            if (categorySheetStatus == 0) {
                ValueRange categoriesResponse = this.mService.spreadsheets().values()
                        .get(spreadsheetId, categoriesRange)
                        .execute();

                List<List<Object>> categoriesValues = categoriesResponse.getValues();
                if (categoriesValues != null) {
                    spiner_values.add("Categories");
                    for (List row : categoriesValues) {
                        spiner_values.add(row.get(0).toString());
                    }
                }
            }

            return date;
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
                List<String> groupDate = new ArrayList<>();
                groupDate.add("Date");

                // getting all the unique dates
                int z = 0;
                for (int i = output.size() - 1; i >= 1; i--) {
                    if (!output.get(i).equals(groupDate.get(z))) {
                        groupDate.add(output.get(i));
                        z++;
                    }
                }
                int index = 1;
                int totalPrice = 0;
                List<String> total = new ArrayList<>();
                total.add("Total");
                for (int m = 1; m < groupDate.size(); m++) {
                    for (int p = 1; p < output.size(); p++) {
                        if (index <= groupDate.size() - 1) {
                            if (output.get(p).equals(groupDate.get(index))) {
                                totalPrice = totalPrice + Integer.parseInt(price.get(p));
                            }
                        }
                    }
                    if (index <= groupDate.size()) {
                        total.add("Rs " + totalPrice);
                        index++;
                        totalPrice = 0;
                    }
                }

                int x = 1;
                ArrayList<Model> list = new ArrayList<>();
                for (int y = 1; y < groupDate.size(); y++) {
                    String day = null;
                    try {
                        day = getDay(groupDate.get(y).toString());
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                    String splitDate[] = groupDate.get(y).split("/");
                    String date = splitDate[1] + "." + splitDate[2];

                    list.add(new Model(0, splitDate[0], date, day, total.get(y), 0));
                    for (int q = 1; q < output.size(); q++) {
                        if (x <= groupDate.size() - 1) {
                            if (output.get(q).equals(groupDate.get(x))) {
                                list.add(new Model(1, output.get(q), categories.get(q), name.get(q), price.get(q), q));
                            }

                        }
                    }
                    if (x <= groupDate.size()) {
                        x++;
                    }
                }
                recyclerView.setLayoutManager(new LinearLayoutManager(DataActivity.this));
                recyclerView.setAdapter(new MultiViewAdapter(list, DataActivity.this));

                if (categorySheetStatus == 0) {
                    ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(DataActivity.this, android.R.layout.simple_spinner_item, spiner_values);
                    dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinner.setAdapter(dataAdapter);
                    categorySheetStatus = 1;
                }
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
                            MainActivity.REQUEST_AUTHORIZATION);
                } else {
                    mOutputText.setText("The following error occurred:\n"
                            + mLastError.getMessage());
                }
            } else {
                mOutputText.setText("Request cancelled.");
            }
        }
    }

    private class UpdateSheetRequest extends AsyncTask<Void, Void, String> {
        List<List<Object>> values;
        private com.google.api.services.sheets.v4.Sheets mService = null;
        private Exception mLastError = null;

        UpdateSheetRequest(GoogleAccountCredential credential) {
            HttpTransport transport = AndroidHttp.newCompatibleTransport();
            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
            mService = new com.google.api.services.sheets.v4.Sheets.Builder(
                    transport, jsonFactory, credential)
                    .setApplicationName("Google Sheets API Android Quickstart")
                    .build();
        }

        @Override
        protected String doInBackground(Void... params) {
            try {
                return updateSheet();
            } catch (Exception e) {
                mLastError = e;
                cancel(true);
                return null;
            }
        }

        private String updateSheet() throws IOException {
            String rangee;
            String result = "";

            if (btn_add2sheet.getText().toString() == "Add") {
                rangee = "Copy of Data!A2:D";
                ValueRange valueRange = new ValueRange();
                valueRange.setMajorDimension("ROWS");
                valueRange.setRange(rangee);
                valueRange.setValues(values);

                AppendValuesResponse response = this.mService.spreadsheets().values()
                        .append(spreadsheetId, rangee, valueRange)
                        .setValueInputOption("RAW")
                        .execute();
                result = response.toString();
            }

            if (btn_add2sheet.getText().toString() == "Update") {
                rangee = "Copy of Data!A" + position + ":D";
                ValueRange valueRange = new ValueRange();
                valueRange.setMajorDimension("ROWS");
                valueRange.setRange(rangee);
                valueRange.setValues(values);
                UpdateValuesResponse responsee = this.mService.spreadsheets().values()
                        .update(spreadsheetId, rangee, valueRange)
                        .setValueInputOption("RAW")
                        .execute();
                result = responsee.toString();
            }

            return result;
        }


        @Override
        protected void onPreExecute() {
            mOutputText.setText("");
            values = new ArrayList<>();

            List<Object> data = new ArrayList<>();
            data.add(date.getText().toString());
            data.add(spinner.getSelectedItem().toString());
            data.add(e_name.getText().toString());
            data.add(price.getText().toString());

            values.add(data);
            mProgress.show();
        }

        @SuppressLint("SetTextI18n")
        @Override
        protected void onPostExecute(String output) {
            mProgress.hide();
            if (output == null || output.isEmpty()) {
                mOutputText.setText("No results returned.");
            } else {
                mProgress.setMessage("Updating Google Sheets API ...");
                Toast.makeText(DataActivity.this, "Record Updated", Toast.LENGTH_SHORT).show();
                getResultsFromApi();
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
                            MainActivity.REQUEST_AUTHORIZATION);
                } else {
                    mOutputText.setText("The following error occurred:\n"
                            + mLastError.getMessage());
                }
            } else {
                mOutputText.setText("Request cancelled.");
            }
        }
    }

    public String getDay(String date) throws ParseException {
        SimpleDateFormat format1 = new SimpleDateFormat("dd/MM/yyyy");
        Date dt1 = format1.parse(date);
        DateFormat format2 = new SimpleDateFormat("EEEE");
        return format2.format(dt1);

    }
}