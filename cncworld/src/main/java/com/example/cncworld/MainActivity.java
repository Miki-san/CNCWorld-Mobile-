package com.example.cncworld;

import android.app.AlertDialog;
import android.content.Intent;
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

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;


public class MainActivity extends AppCompatActivity {
    private Spinner spinner;
    private TextView multiselect, statusText, textField;
    private Button selectButton;
    private JSONArray dataFromJSON;
    private String[] tables, fields;
    private boolean[] selectedFields;
    private ArrayList<Integer> fieldsList;
    private String requestSQL, requestFields, requestTable, selectedTable;


    public void createTablesList(JSONArray jsonArray) {
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

    public void connectionToDB(String connectionURL, String method, String requestData) {
        DBConnection dbConnection = new DBConnection(method, requestData);
        dbConnection.execute(connectionURL);
        while (!dbConnection.isFlag()) {
        }
        dataFromJSON = dbConnection.getDataFromJSON();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.start_layout);
        statusText = findViewById(R.id.statusText);
        statusText.setText(R.string.textview1);
        String tablesURL = getString(R.string.tablesURL),
                fieldsURL = getString(R.string.fieldsURL),
                selectURL = getString(R.string.selectURL);

        connectionToDB(tablesURL, "GET", null);
        setContentView(R.layout.object_select_layout);
        spinner = findViewById(R.id.spinner);
        multiselect = findViewById(R.id.multiselect);
        textField = findViewById(R.id.textField);
        selectButton = findViewById(R.id.selectButton);

        createTablesList(dataFromJSON);
        Arrays.sort(tables);
        createDropDownList(tables);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedTable = tables[position];
                Toast.makeText(MainActivity.this, "Ваш выбор: " + selectedTable, Toast.LENGTH_LONG).show();
                connectionToDB(fieldsURL + selectedTable, "GET", null);
                createFieldList(dataFromJSON);
                Arrays.sort(fields);
                selectedFields = new boolean[fields.length];
                fieldsList = new ArrayList<>();
                requestFields = "";
                createMultiselectDropDownList();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        selectButton.setOnClickListener(v -> {
            requestSQL = textField.getText().toString();
            requestTable = "{ \"fields\" : [ " + requestFields + " ], \"filter\" : \"" + requestSQL + "\" }";
            connectionToDB(selectURL + selectedTable, "POST", requestTable);
            Intent intent = new Intent(MainActivity.this, TableActivity.class);
            intent.putExtra("dataFromJSON", dataFromJSON.toString());
            intent.putExtra("tableName", selectedTable);
            startActivity(intent);
        });

    }
}
