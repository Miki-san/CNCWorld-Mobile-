package com.example.cncworld;

import android.os.AsyncTask;
import android.util.Log;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;

import java.io.BufferedInputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class DBConnection extends AsyncTask<String, String, String> {
    private String method, requestData, responseData;
    private URL url;
    private int statusCode;
    private volatile boolean flag;
    private JSONArray dataFromJSON = null;

    public JSONArray getDataFromJSON() {
        return dataFromJSON;
    }

    String unescape(String s) {
        int i = 0, len = s.length();
        char c;
        StringBuffer sb = new StringBuffer(len);
        while (i < len) {
            c = s.charAt(i++);
            if (c == '\\') {
                if (i < len) {
                    c = s.charAt(i++);
                    if (c == 'u') {
                        // TODO: check that 4 more chars exist and are all hex digits
                        c = (char) Integer.parseInt(s.substring(i, i + 4), 16);
                        i += 4;
                    } // add other cases here as desired...
                }
            } // fall through: \ escapes itself, quotes any character but u
            sb.append(c);
        }
        return sb.toString();
    }

    public Boolean getData(HttpURLConnection connection) {
        try {
            BufferedInputStream in = new BufferedInputStream(connection.getInputStream());
            this.dataFromJSON = new JSONArray(IOUtils.toString(in, StandardCharsets.UTF_8));
            return true;
        } catch (Exception exception) {
            Log.d("getData ERROR", exception.toString());
        }
        Log.d("getData status", "Data retrieved unsuccessfully. Data address: " + connection.getURL().toString());
        return false;
    }

    public Boolean postData(HttpURLConnection connection) {
        try {
            byte[] postDataBytes = this.requestData.getBytes(StandardCharsets.UTF_8);
            connection.getOutputStream().write(postDataBytes);
            this.responseData = IOUtils.toString(connection.getInputStream(), StandardCharsets.UTF_8);
            this.responseData = unescape(this.responseData);
            if (this.responseData.charAt(0) != '[') {
                this.responseData = "[" + this.responseData + "]";
            }
            this.dataFromJSON = new JSONArray(this.responseData);
            return true;
        } catch (Exception exception) {
            Log.d("postData ERROR", exception.toString());
        }
        Log.d("postData status", "Data retrieved unsuccessfully. Data address: " + connection.getURL().toString());
        return false;
    }


    public Boolean deleteData(HttpURLConnection connection) {
        try {
            byte[] postDataBytes = this.requestData.getBytes(StandardCharsets.UTF_8);
            connection.getOutputStream().write(postDataBytes);
            this.statusCode = connection.getResponseCode();
            return true;
        } catch (Exception exception) {
            Log.d("deleteData ERROR", exception.toString());
        }
        Log.d("deleteData status", "Data retrieved unsuccessfully. Data address: " + connection.getURL().toString());
        return false;
    }

    public void HTTPConnection(HttpURLConnection connection, String method) {
        try {
            connection.setRequestMethod(method);
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);
            switch (method) {
                case "GET":
                    connection.connect();
                    this.statusCode = connection.getResponseCode();
                    switch (this.statusCode) {
                        case 200:
                        case 201:
                            this.flag = getData(connection);
                    }
                    break;
                case "POST":
                case "PATCH":
                    connection.setInstanceFollowRedirects(false);
                    connection.setUseCaches(false);
                    connection.setRequestProperty("Content-Type", "application/json; utf-8");
                    connection.setRequestProperty("Accept", "application/json; utf-8");
                    connection.setDoOutput(true);
                    this.flag = postData(connection);
                    break;
                case "DELETE":
                    connection.setRequestProperty("Content-Type", "application/json; utf-8");
                    connection.setDoOutput(true);
                    connection.connect();
                    this.flag = deleteData(connection);
                    break;
            }
        } catch (Exception exception) {
            Log.d("HTTPConnection ERROR", exception.toString());
        }
    }

    public DBConnection(String method, String requestData) {
        this.method = method;
        this.requestData = requestData;
        this.statusCode = 0;
        this.flag = false;
    }

    public boolean isFlag() {
        return flag;
    }

    @Override
    protected String doInBackground(String... strings) {
        try {
            this.url = new URL(strings[0]);
            HttpURLConnection connection = (HttpURLConnection) this.url.openConnection();
            HTTPConnection(connection, this.method);
            return this.url.toString();
        } catch (Exception e) {
            Log.d(this.method + "ERROR", e.toString());
        }
        return null;
    }

    @Override
    protected void onProgressUpdate(String... values) {
    }

    @Override
    protected void onPostExecute(String s) {
        Log.d(this.method + " status", this.method + " Completed! " + this.method + " URL: " + s + " Request data: " + this.requestData);
    }
}
