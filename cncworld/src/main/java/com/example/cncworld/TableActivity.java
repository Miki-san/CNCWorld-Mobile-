package com.example.cncworld;

import android.os.Bundle;
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

import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Arrays;

public class TableActivity extends AppCompatActivity {
    private Spinner spinner;
    private TextView textView;
    private EditText editText;
    private Button addButton, updateButton, deleteButton, copyButton;
    private String[] ids;
    private String selectedId;
    private JSONArray jsonArray;

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




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.table_layout);

        String insertURL = getString(R.string.insertURL),
                updateURL = getString(R.string.updateURL),
                deleteURL = getString(R.string.deleteURL),
                requestData = (String)getIntent().getSerializableExtra("String");
        try {
             jsonArray = new JSONArray(requestData);
        } catch (JSONException e) {
            Log.d("ERROR", e.toString());
        }
        spinner = findViewById(R.id.spinner2);
        textView = findViewById(R.id.textView);
        editText = findViewById(R.id.textField2);
        addButton = findViewById(R.id.button);
        updateButton = findViewById(R.id.button2);
        deleteButton = findViewById(R.id.button3);
        copyButton = findViewById(R.id.button4);

        createTableList(jsonArray);
        Arrays.sort(ids);
        createDropDownList(ids);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(TableActivity.this, "Ваш выбор: " + ids[position], Toast.LENGTH_LONG).show();
                selectedId = ids[position];
                for (int i = 0; i < jsonArray.length(); i++) {
                    try {
                        if(jsonArray.getJSONObject(i).get("id").toString().equals(selectedId)){
                            textView.setText(new GsonBuilder().setPrettyPrinting().create().toJson(new JsonParser().parse(jsonArray.getJSONObject(i).toString())));
                        }
                    } catch (JSONException e) {
                        Log.d("ERROR", e.toString());
                    }
                }

                /*getDataConnection(fieldsURL + tables[position]);
                createFieldList(dataFromJSON);
                Arrays.sort(fields);
                selectedFields = new boolean[fields.length];
                fieldsList = new ArrayList<>();
                createMultiselectDropDownList();*/
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });



    }
}