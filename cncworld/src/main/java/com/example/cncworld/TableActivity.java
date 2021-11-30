package com.example.cncworld;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

public class TableActivity extends AppCompatActivity {
    private Spinner spinner;
    private TextView textView;
    private EditText editText;
    private Button addButton, updateButton, deleteButton, copyButton;
    private String[] ids;
    private String selectedId, jsonCreator, actionRequest, selectedTable;
    private JSONArray dataFromJSON;
    private JSONObject selectedObj;

    public void createTableList(JSONArray jsonArray) {
        try {
            ArrayList<String> list = new ArrayList<>();
            for (int i = 0; i < jsonArray.length(); i++) {
                list.add(jsonArray.getJSONObject(i).get("id").toString());
            }
            ids = list.toArray(new String[0]);
        } catch (Exception exception) {
            Log.d("createTableList ERROR", exception.toString());
        }
    }

    public void createDropDownList(String[] data) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, data);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    public static void normalizeJSON(JSONObject obj) {
        for (Iterator<String> it = obj.keys(); it.hasNext(); ) {
            String key = it.next();
            try {
                if (obj.get(key) instanceof JSONObject) {
                    JSONObject nest = (JSONObject) obj.get(key);
                    obj.put(key, nest.get("id"));
                }
            } catch (Exception e) {
                Log.d("normalizeJSON ERROR", e.toString());
            }
        }
    }

    public void connectionToDB(String connectionURL, String method, String requestData) {
        DBConnection dbConnection = new DBConnection(method, requestData);
        dbConnection.execute(connectionURL);
        while (!dbConnection.isFlag()) {
        }
        dataFromJSON = dbConnection.getDataFromJSON();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.table_layout);
        String insertURL = getString(R.string.insertURL),
                updateURL = getString(R.string.updateURL),
                deleteURL = getString(R.string.deleteURL);


        try {
            dataFromJSON = new JSONArray((String) getIntent().getSerializableExtra("dataFromJSON"));
        } catch (JSONException e) {
            Log.d("ERROR", e.toString());
        }
        selectedTable = (String) getIntent().getSerializableExtra("tableName");


        spinner = findViewById(R.id.spinner2);
        textView = findViewById(R.id.textView);
        textView.setMovementMethod(new ScrollingMovementMethod());
        editText = findViewById(R.id.textField2);
        addButton = findViewById(R.id.button);
        updateButton = findViewById(R.id.button2);
        deleteButton = findViewById(R.id.button3);
        copyButton = findViewById(R.id.button4);

        createTableList(dataFromJSON);
        Arrays.sort(ids);
        createDropDownList(ids);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedId = ids[position];
                Toast.makeText(TableActivity.this, "Ваш выбор: " + selectedId, Toast.LENGTH_LONG).show();
                for (int i = 0; i < dataFromJSON.length(); i++) {
                    try {
                        if (dataFromJSON.getJSONObject(i).get("id").toString().equals(selectedId)) {
                            selectedObj = dataFromJSON.getJSONObject(i);
                            Gson gson = new GsonBuilder().setPrettyPrinting().create();
                            JsonElement jsonElement = new JsonParser().parse(selectedObj.toString());
                            jsonCreator = gson.toJson(jsonElement);
                            textView.setText(jsonCreator);
                        }
                    } catch (Exception e) {
                        Log.d("ERROR", e.toString());
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        copyButton.setOnClickListener(v -> {
            normalizeJSON(selectedObj);
            editText.setText(new GsonBuilder().setPrettyPrinting().create().toJson(new JsonParser().parse(selectedObj.toString())));
        });

        deleteButton.setOnClickListener(v -> {
            actionRequest = "{ \"ids\": [" + selectedId + "] }";
            connectionToDB(deleteURL + selectedTable, "DELETE", actionRequest);
            Intent intent = new Intent(TableActivity.this, MainActivity.class);
            startActivity(intent);
        });

        addButton.setOnClickListener(v -> {
            actionRequest = "{ \"data\": " + editText.getText() + "}";
            connectionToDB(insertURL + selectedTable, "POST", actionRequest);
            Intent intent = new Intent(TableActivity.this, MainActivity.class);
            startActivity(intent);
        });

        updateButton.setOnClickListener(v -> {
            actionRequest = "{ \"id\": " + selectedId + ", \"data\": " + editText.getText() + "}";
            connectionToDB(updateURL + selectedTable, "PATCH", actionRequest);
            Intent intent = new Intent(TableActivity.this, MainActivity.class);
            startActivity(intent);
        });


    }
}