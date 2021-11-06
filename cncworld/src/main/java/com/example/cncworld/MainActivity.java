package com.example.cncworld;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
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


public class MainActivity extends AppCompatActivity {
    private Spinner spinner;
    private TextView multiselect,  statusText, textField;
    private Button selectButton;

    class GetDataByURL extends AsyncTask<String, Integer, ArrayList<String>> {
        protected ArrayList<String> doInBackground(String... urls) {
            try {
                URL url = new URL(urls[0]);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                switch (HTTPConnection(connection)) {
                    case 200:
                    case 201:
                        return getTableList(connection);
                }
                return null;
            } catch (Exception exception){
                Log.d("ERROR", exception.toString());
            }
            return null;
        }

        protected void onProgressUpdate(Integer... progress) {
        }

        @SuppressLint("SetTextI18n")
        @RequiresApi(api = Build.VERSION_CODES.N)
        protected void onPostExecute(ArrayList<String> result) {
            Handler handler = new Handler(), handler1 = new Handler();
            handler.postDelayed(() -> {
                statusText.setText(R.string.textview2);
                handler1.postDelayed(() -> {
                    setContentView(R.layout.object_select_layout);
                    spinner = findViewById(R.id.spinner);
                    multiselect = findViewById(R.id.multiselect);
                    textField = findViewById(R.id.textField);
                    selectButton = findViewById(R.id.selectButton);
                    createDropDownList(result.toArray(new String[0]));
                    selectButton.setOnClickListener(v -> {
                        if(textField.getText().toString().trim().equals("")){
                            Toast.makeText(MainActivity.this, R.string.no_text,Toast.LENGTH_LONG).show();
                        }
                    });
                }, 1000);
            }, 1000);


        }
    }

    public int HTTPConnection(HttpURLConnection connection){
        try {
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            connection.connect();
            int status = connection.getResponseCode();
            Log.d("Status", String.valueOf(status));
            return status;
        }catch (Exception exception){
            Log.d("ERROR", exception.toString());
        }
        return 0;
    }

    public ArrayList<String> getTableList(HttpURLConnection connection){
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line).append("\n");
            }
            br.close();
            String jsonString = sb.toString();
            JSONArray jsonArray = new JSONArray(jsonString);
            ArrayList<String> list = new ArrayList<>();
            for (int i = 0; i < jsonArray.length() ; i++) {
                list.add(jsonArray.getJSONObject(i).get("table_name").toString());
            }
            return list;
        }catch (Exception exception){
            Log.d("ERROR", exception.toString());
        }
        return null;
    }

    public void createDropDownList(String[] data){
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, data);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }


    @SuppressLint("SetTextI18n")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.start_layout);
        statusText=findViewById(R.id.statusText);
        statusText.setText(R.string.textview1);
        String sUrl = getString(R.string.dbURL);
        Log.d("URL", sUrl);
        new GetDataByURL().execute(sUrl);
    }
}