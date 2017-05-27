package com.vergonix.flatm8;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import javax.net.ssl.HttpsURLConnection;

public class noweAktywnosci extends AppCompatActivity {
    private static final String TAG_ID = "id";
    private Spinner mateList;
    private String url = "http://v-ie.uek.krakow.pl/~s187772/psm/listaLokatorow.php";
    private ArrayAdapter<String> adapter ;
    private ArrayList<String> mates = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.nowe_aktywnosci);

        mateList = (Spinner) findViewById(R.id.flatMate);
        new MateList().execute();

    }

    private class MateList extends AsyncTask<Void, Void, Boolean> {


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            String response = "";
            Boolean status = false;

            SharedPreferences pref = getSharedPreferences("GROUP", Context.MODE_PRIVATE);
            String groupId = pref.getString(MainActivity.GROUP, "brak");

            HashMap<String, String> dane = new HashMap<String, String>();
            dane.put(TAG_ID, groupId);

            response = postData(url, dane);

            if (!response.equalsIgnoreCase("")) {
                try {
                    JSONObject jRoot = new JSONObject(response);
                    JSONArray u = jRoot.getJSONArray("mates");

                    if (u.length() == 0) {
                        mates.add("Brak zarejestrowanych lokatorów");
                    }

                    for (int i=0; i<u.length(); i++) {
                        JSONObject element = u.getJSONObject(i);
                        String name = element.getString("name");
                        mates.add(name);
                    }

                    //Log.i("ODP", mates.toString());

                    status = true;
                } catch (JSONException e) {
                    e.printStackTrace();
                } finally {

                }
            } else {
                status = false;
            }

            return status;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if(result) {
                adapter = new ArrayAdapter<String>(noweAktywnosci.this, android.R.layout.simple_spinner_item, mates);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                mateList.setAdapter(adapter);
            }
        }

        public String postData(String url, HashMap<String, String> data) {
            URL requestUrl;
            String response = "";

            try {
                requestUrl = new URL(url);
                HttpURLConnection conn = (HttpURLConnection) requestUrl.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.setDoOutput(true);

                conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");

                JSONObject root = new JSONObject(data);

                String str = root.toString();
                byte[] outputBytes = str.getBytes("UTF-8");
                OutputStream os = conn.getOutputStream();
                os.write(outputBytes);

                int responseCode = conn.getResponseCode();

                if (responseCode == HttpsURLConnection.HTTP_OK) {
                    String line;
                    BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    while ((line = br.readLine()) != null) {
                        response += line;
                    }
                } else {
                    response = "";
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return response;
        }
    }
}
