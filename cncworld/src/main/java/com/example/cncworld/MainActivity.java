package com.example.cncworld;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.simple.parser.JSONParser;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;


public class MainActivity extends AppCompatActivity {
    class GetUrlContentTask extends AsyncTask<String, Integer, ArrayList<String>> {
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
                        Log.d("a___a", "aaa");
                        ArrayList<String> list = new ArrayList<>();
                        for (int i = 0; i < jsonArray.length() ; i++) {
                            list.add(jsonArray.getJSONObject(i).get("table_name").toString());
                            Log.d("List__r", jsonArray.getJSONObject(i).get("table_name").toString());
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

        @RequiresApi(api = Build.VERSION_CODES.N)
        protected void onPostExecute(ArrayList<String> result) {
            // this is executed on the main thread after the process is over
            // update your UI here
            Log.d("Result", result.toString());
            createDropDownList(result.toArray(new String[0]));
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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.object_select_layout);


        String sUrl = "https://cnc-world-api.herokuapp.com/api/tables/";
        new GetUrlContentTask().execute(sUrl);
    }
}