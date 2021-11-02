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
import android.widget.Spinner;
import android.widget.TextView;

import org.json.JSONArray;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;


public class MainActivity extends AppCompatActivity {

    class GetUrlContentTask extends AsyncTask<String, Integer, ArrayList<String>> {
        TextView textView;
        public GetUrlContentTask(TextView tv) {
            textView = tv;
        }

        protected ArrayList<String> doInBackground(String... urls) {
            try {
                URL url = new URL(urls[0]);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);
                connection.connect();
                int status = connection.getResponseCode();
                Log.d("Status", String.valueOf(status));
                switch (status) {
                    case 200:
                    case 201:
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
            // this is executed on the main thread after the process is over
            // update your UI here
            Handler handler = new Handler(), handler1 = new Handler();
            handler.postDelayed(() -> {
                textView.setText(R.string.textview2);
                handler1.postDelayed(() -> {
                    setContentView(R.layout.object_select_layout);
                    createDropDownList(result.toArray(new String[0]));
                }, 1000);
            }, 1000);


        }
    }
    public void createDropDownList(String[] data){
        Spinner spinner = findViewById(R.id.spinner);
        // Создаем адаптер ArrayAdapter с помощью массива строк и стандартной разметки элемета spinner
        ArrayAdapter<String> adapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, data);
        // Определяем разметку для использования при выборе элемента
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Применяем адаптер к элементу spinner
        spinner.setAdapter(adapter);
    }


    @SuppressLint("SetTextI18n")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.start_layout);
        TextView textView = findViewById(R.id.textView);
        textView.setText(R.string.textview1);
        String sUrl = getString(R.string.dbURL);
        Log.d("URL", sUrl);
        new GetUrlContentTask(textView).execute(sUrl);


    }
}