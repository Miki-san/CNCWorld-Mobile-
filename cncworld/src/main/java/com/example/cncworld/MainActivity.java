package com.example.cncworld;

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

import androidx.appcompat.app.AppCompatActivity;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;

import java.io.BufferedInputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;


public class MainActivity extends AppCompatActivity {
    private Spinner spinner;
    private TextView multiselect, statusText, textField;
    private Button selectButton;
    private volatile boolean getDataFlag = false, URLConnectionFlag = false, selectFlag = false;
    private JSONArray dataFromJSON;
    private String[] tables, fields;
    private boolean[] selectedFields;
    private ArrayList<Integer> fieldsList;
    private String requestSQL, requestFields, requestTable, selectedTable, requestData,
            tablesURL = getString(R.string.tablesURL),
            connectionURL = getString(R.string.connectionURL),
            fieldsURL = getString(R.string.fieldsURL),
            selectURL = getString(R.string.selectURL),
            insertURL = getString(R.string.insertURL),
            updateURL = getString(R.string.updateURL),
            deleteURL = getString(R.string.deleteURL);;
    private int tableRow, tableColumn;


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
                    Log.d("ConnectByURL ERROR", exception.toString());
                }
                Log.d("ConnectByURL Status", "Connection failed. Trying again");
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
        }

        @Override
        protected void onPostExecute(String result) {
            Log.d("ConnectByURL Status", "Connected to database");
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
                Log.d("GetDataByURL ERROR", exception.toString());
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(String... progress) {
        }

        @Override
        protected void onPostExecute(String result) {
            Log.d("GetDataByURL Status", "Data retrieved successfully. Data address: " + result);
        }
    }

    class PostDataByURL extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... url) {
            try {
                URL urls = new URL(url[0]);
                HttpURLConnection connection = (HttpURLConnection) urls.openConnection();
                HTTPConnection(connection, "POST");
                getDataFlag = postData(connection);
                return urls.toString();
            } catch (Exception exception) {
                Log.d("PostDataByURL ERROR", exception.toString());
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(String... progress) {
        }

        @Override
        protected void onPostExecute(String result) {
            Log.d("PostDataByURL Status", "Data sent successfully. Data address: " + result + " Sent data: " + requestTable + " Response data: " + requestData);
        }
    }

    public int HTTPConnection(HttpURLConnection connection, String method) {
        try {
            int status = 0;
            connection.setRequestMethod(method);
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);
            switch (method) {
                case "GET":
                    connection.connect();
                    status = connection.getResponseCode();
                    Log.d("HTTPConnection code", String.valueOf(status));
                    break;
                case "POST":
                    connection.setInstanceFollowRedirects(false);
                    connection.setUseCaches(false);
                    connection.setRequestProperty("Content-Type", "application/json; utf-8");
                    connection.setRequestProperty("Accept", "application/json; utf-8");
                    connection.setDoOutput(true);
                    break;
            }
            return status;
        } catch (Exception exception) {
            Log.d("HTTPConnection ERROR", exception.toString());
        }
        return 0;
    }

    public Boolean getData(HttpURLConnection connection) {
        try {
            BufferedInputStream in = new BufferedInputStream(connection.getInputStream());
            dataFromJSON = new JSONArray(IOUtils.toString(in, StandardCharsets.UTF_8));
            return true;
        } catch (Exception exception) {
            Log.d("getData ERROR", exception.toString());
        }
        Log.d("getData status", "Data retrieved unsuccessfully. Data address: " + connection.getURL().toString());
        return false;
    }

    String unescape(String s) {
        int i=0, len=s.length();
        char c;
        StringBuffer sb = new StringBuffer(len);
        while (i < len) {
            c = s.charAt(i++);
            if (c == '\\') {
                if (i < len) {
                    c = s.charAt(i++);
                    if (c == 'u') {
                        // TODO: check that 4 more chars exist and are all hex digits
                        c = (char) Integer.parseInt(s.substring(i, i+4), 16);
                        i += 4;
                    } // add other cases here as desired...
                }
            } // fall through: \ escapes itself, quotes any character but u
            sb.append(c);
        }
        return sb.toString();
    }

    public Boolean postData(HttpURLConnection connection) {
        try {
            byte[] postDataBytes = requestTable.getBytes(StandardCharsets.UTF_8);
            connection.getOutputStream().write(postDataBytes);
            requestData = IOUtils.toString(connection.getInputStream(), StandardCharsets.UTF_8);
            requestData = unescape(requestData);
            return true;
        } catch (Exception exception) {
            Log.d("postData ERROR", exception.toString());
        }
        Log.d("postData status", "Data retrieved unsuccessfully. Data address: " + connection.getURL().toString());
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
            Log.d("createTableList ERROR", exception.toString());
        }
    }

    public void createDropDownList(String[] data) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, data);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    public void createFieldList(JSONArray jsonArray) {
        try {
            ArrayList<String> list = new ArrayList<>();
            for (int i = 0; i < jsonArray.length(); i++) {
                list.add(jsonArray.getJSONObject(i).get("column_name").toString());
            }
            fields = list.toArray(new String[0]);
        } catch (Exception exception) {
            Log.d("createFieldList ERROR", exception.toString());
        }
    }

    public void testConnection(String connectionURL) {
        new ConnectByURL().execute(connectionURL);
        while (!URLConnectionFlag) {
        }
        URLConnectionFlag = false;
    }

    public void getDataConnection(String dataURL) {
        new GetDataByURL().execute(dataURL);
        while (!getDataFlag) {
        }
        getDataFlag = false;
    }

    public void postDataConnection(String dataURL) {
        new PostDataByURL().execute(dataURL);
        while (!getDataFlag) {
        }
        getDataFlag = false;
    }

    public void createMultiselectDropDownList() {
        multiselect.setOnClickListener(view1 -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("Select Fields");
            builder.setCancelable(false);

            builder.setMultiChoiceItems(fields, selectedFields, (dialogInterface, i, b) -> {
                if (b) {
                    fieldsList.add(i);
                } else {
                    for (int j = 0; j < fieldsList.size(); j++) {
                        if (fieldsList.get(j) == i) {
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
                tableColumn = fieldsList.size();
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

    public void tableSelect(){
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
                getDataConnection(fieldsURL + tables[position]);
                createFieldList(dataFromJSON);
                Arrays.sort(fields);
                selectedFields = new boolean[fields.length];
                fieldsList = new ArrayList<>();
                createMultiselectDropDownList();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        selectButton.setOnClickListener(v -> {
            requestSQL = textField.getText().toString();
            requestTable = "{ \"fields\" : [ " + requestFields + " ], \"filter\" : \"" + requestSQL + "\" }";
            postDataConnection(selectURL + selectedTable);
            selectFlag = true;
        });
        while (!selectFlag){}
        selectFlag = false;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.start_layout);
        statusText = findViewById(R.id.statusText);
        statusText.setText(R.string.textview1);

        testConnection(connectionURL);

        tableSelect();

        setContentView(R.layout.table_layout);




    }
}
