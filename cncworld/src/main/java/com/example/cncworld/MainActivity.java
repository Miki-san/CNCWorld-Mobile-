package com.example.cncworld;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;


public class MainActivity extends AppCompatActivity {
    private Spinner spinner;
    private TextView multiselect, statusText, textField;
    private Button selectButton;
    private volatile boolean getDataFlag = false, URLConnectionFlag = false;
    private JSONArray dataFromJSON;
    private String[] tables, fields;
    private boolean[] selectedFields;
    private ArrayList<Integer> fieldsList;
    private String requestSQL, requestFields, requestTable, selectedTable;


    class ConnectByURL extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... urls) {
            while (!URLConnectionFlag) {
                try {
                    URL url = new URL(urls[0]);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    switch (HTTPConnection(connection, "GET")) {
                        case 200:
                        case 201:
                            URLConnectionFlag = true;
                            return connection.getURL().toString();
                    }
                } catch (Exception exception) {
                    Log.d("ERROR", exception.toString());
                }
                Log.d("Connection Status", "Connection failed. Trying again");
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
        }

        @Override
        protected void onPostExecute(String result) {
            Log.d("Connection Status", "Connected to database");
        }
    }

    class GetDataByURL extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... url) {
            try {
                URL urls = new URL(url[0]);
                HttpURLConnection connection = (HttpURLConnection) urls.openConnection();
                switch (HTTPConnection(connection, "GET")) {
                    case 200:
                    case 201:
                        getDataFlag = getData(connection);
                        return urls.toString();
                }
                return null;
            } catch (Exception exception) {
                Log.d("ERROR", exception.toString());
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(String... progress) {
        }

        @Override
        protected void onPostExecute(String result) {
            Log.d("Data Status", "Data retrieved successfully. Data address:" + result);
        }
    }

    class PostDataByURL extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... url) {
            try {
                URL urls = new URL(url[0]);
                HttpURLConnection connection = (HttpURLConnection) urls.openConnection();
                switch (HTTPConnection(connection, "GET")) {
                    case 200:
                    case 201:
                        getDataFlag = getData(connection);
                        return urls.toString();
                }
                return null;
            } catch (Exception exception) {
                Log.d("ERROR", exception.toString());
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(String... progress) {
        }

        @Override
        protected void onPostExecute(String result) {
            Log.d("Data Status", "Data retrieved successfully. Data address:" + result);
        }
    }

    public int HTTPConnection(HttpURLConnection connection, String method) {
        try {
            connection.setRequestMethod(method);
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            connection.connect();
            int status = connection.getResponseCode();
            Log.d("Status code", String.valueOf(status));
            return status;
        } catch (Exception exception) {
            Log.d("ERROR", exception.toString());
        }
        return 0;
    }

    public Boolean getData(HttpURLConnection connection) {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line).append("\n");
            }
            br.close();
            String jsonString = sb.toString();
            dataFromJSON = new JSONArray(jsonString);
            return true;
        } catch (Exception exception) {
            Log.d("ERROR", exception.toString());
        }
        Log.d("Data Status", "Data retrieved unsuccessfully. Data address:" + connection.getURL().toString());
        return false;
    }

    public void createTableList(JSONArray jsonArray) {
        try {
            ArrayList<String> list = new ArrayList<>();
            for (int i = 0; i < jsonArray.length(); i++) {
                list.add(jsonArray.getJSONObject(i).get("table_name").toString());
            }
            tables = list.toArray(new String[0]);
        } catch (Exception exception) {
            Log.d("ERROR", exception.toString());
        }
    }

    public void createDropDownList(String[] data) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, data);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    public void createFieldList(JSONArray jsonArray){
        try {
            ArrayList<String> list = new ArrayList<>();
            for (int i = 0; i < jsonArray.length(); i++) {
                list.add(jsonArray.getJSONObject(i).get("column_name").toString());
            }
            fields = list.toArray(new String[0]);
        } catch (Exception exception) {
            Log.d("ERROR", exception.toString());
        }
    }

    public void testConnection(String connectionURL){
        new ConnectByURL().execute(connectionURL);
        while (!URLConnectionFlag) {
        }
        URLConnectionFlag = false;
    }

    public void getDataConnection(String dataURL){
        new GetDataByURL().execute(dataURL);
        while (!getDataFlag) {
        }
        getDataFlag = false;
    }

    public void postDataConnection(String dataURL, String data){
        new PostDataByURL().execute(dataURL);
        while (!getDataFlag) {
        }
        getDataFlag = false;
    }

    public void createMultiselectDropDownList(){
        multiselect.setOnClickListener(view1 -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("Select Fields");
            builder.setCancelable(false);

            builder.setMultiChoiceItems(fields, selectedFields, (dialogInterface, i, b) -> {
                if (b) {
                    fieldsList.add(i);
                } else {
                    for (int j = 0; j < fieldsList.size(); j++) {
                        if(fieldsList.get(j) == i){
                            fieldsList.remove(j);
                            break;
                        }
                    }
                }
                Collections.sort(fieldsList);
            });

            builder.setPositiveButton("OK", (dialogInterface, i) -> {
                StringBuilder stringBuilder = new StringBuilder(), requestBuilder = new StringBuilder();
                for (int j = 0; j < fieldsList.size(); j++) {
                    stringBuilder.append(fields[fieldsList.get(j)]);
                    requestBuilder.append("\"").append(fields[fieldsList.get(j)]).append("\"");
                    if (j != fieldsList.size() - 1) {
                        stringBuilder.append(", ");
                        requestBuilder.append(", ");
                    }
                }
                multiselect.setText(stringBuilder.toString());
                requestFields = requestBuilder.toString();
            });

            builder.setNegativeButton("Cancel", (dialogInterface, i) -> dialogInterface.dismiss());

            builder.setNeutralButton("Clear All", (dialogInterface, i) -> {
                Arrays.fill(selectedFields, false);
                fieldsList.clear();
                multiselect.setText("");
                requestFields = "";
            });
            builder.show();
        });
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.start_layout);
        statusText = findViewById(R.id.statusText);
        statusText.setText(R.string.textview1);
        String tablesURL = getString(R.string.tablesURL),
                connectionURL = getString(R.string.connectionURL),
                fieldsURL = getString(R.string.fieldsURL),
                selectURL = getString(R.string.selectURL),
                insertURL = getString(R.string.insertURL),
                updateURL = getString(R.string.updateURL),
                deleteURL = getString(R.string.deleteURL);

        testConnection(connectionURL);
        getDataConnection(tablesURL);
        setContentView(R.layout.object_select_layout);
        spinner = findViewById(R.id.spinner);
        multiselect = findViewById(R.id.multiselect);
        textField = findViewById(R.id.textField);
        selectButton = findViewById(R.id.selectButton);

        createTableList(dataFromJSON);
        Arrays.sort(tables);
        createDropDownList(tables);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(MainActivity.this, "Ваш выбор: " + tables[position], Toast.LENGTH_LONG).show();
                selectedTable = tables[position];
                getDataConnection(fieldsURL+tables[position]);
                createFieldList(dataFromJSON);
                Arrays.sort(fields);
                selectedFields = new boolean[fields.length];
                fieldsList = new ArrayList<>();
                createMultiselectDropDownList();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });

        selectButton.setOnClickListener(v -> {
            requestSQL = textField.getText().toString();
            requestTable = "{ \"fields\" : [ " + requestFields + " ], \"filter\" : \"" + requestSQL + "\" }";
            Log.d("Data Status", "Request Address successfully created. Request Address:" + selectURL + selectedTable);
            postDataConnection(selectURL+selectedTable, requestTable);
            Log.d("Data Status", dataFromJSON.toString());
        });

    }
}