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


public class MainActivity extends AppCompatActivity {
    private Spinner spinner;
    private TextView multiselect, statusText, textField;
    private Button selectButton;
    private boolean getDataFlag = false, URLConnectionFlag = false;
    private JSONArray dataFromJSON;
    private String[] tables, fields;
    private boolean[] selectedFields;
    private ArrayList<Integer> fieldsList;

    class ConnectByURL extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... urls) {
            while (!URLConnectionFlag) {
                try {
                    URL url = new URL(urls[0]);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    switch (HTTPConnection(connection)) {
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
                /*ArrayList<URL> urls = new ArrayList<>();
                for (String str:url) {
                    urls.add(new URL(str));
                }*/
                URL urls = new URL(url[0]);
                HttpURLConnection connection = (HttpURLConnection) urls.openConnection();
                switch (HTTPConnection(connection)) {
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

    public int HTTPConnection(HttpURLConnection connection) {
        try {
            connection.setRequestMethod("GET");
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
                    fieldsList.remove(i);
                }
            });

            builder.setPositiveButton("OK", (dialogInterface, i) -> {
                StringBuilder stringBuilder = new StringBuilder();
                for (int j = 0; j < fieldsList.size(); j++) {
                    stringBuilder.append(fields[fieldsList.get(j)]);
                    if (j != fieldsList.size() - 1) {
                        stringBuilder.append(", ");
                    }
                }
                multiselect.setText(stringBuilder.toString());
            });

            builder.setNegativeButton("Cancel", (dialogInterface, i) -> dialogInterface.dismiss());

            builder.setNeutralButton("Clear All", (dialogInterface, i) -> {
                for (int j = 0; j < selectedFields.length; j++) {
                    selectedFields[j] = false;
                    fieldsList.clear();
                    multiselect.setText("");
                }
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
        String tablesURL = getString(R.string.tablesURL), connectionURL = getString(R.string.connectionURL), fieldsURL = getString(R.string.fieldsURL);

        testConnection(connectionURL);
        new GetDataByURL().execute(tablesURL);
        while (!getDataFlag) {
        }
        getDataFlag = false;
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
                new GetDataByURL().execute(fieldsURL+tables[position]);
                while (!getDataFlag) { }
                createFieldList(dataFromJSON);
                Arrays.sort(fields);
                selectedFields = new boolean[fields.length];
                fieldsList=new ArrayList<>();
                createMultiselectDropDownList();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });


        selectButton.setOnClickListener(v -> {
            if (textField.getText().toString().trim().equals("")) {
                Toast.makeText(MainActivity.this, R.string.no_text, Toast.LENGTH_LONG).show();
            }
        });


    }
}