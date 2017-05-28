package com.vergonix.flatm8;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

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

public class Grupa extends AppCompatActivity {
    private ListView list ;
    private ArrayAdapter<String> adapter ;
    private ArrayList<String> mates = new ArrayList<String>();
    private static final String TAG_ID = "id";
    private static final String TAG_GROUP = "group";
    private static final String TAG_NAME = "name";
    private String empty;

    String urlGrupa = "http://v-ie.uek.krakow.pl/~s187772/psm/grupy.php";
    String urlDodajDoGrupy = "http://v-ie.uek.krakow.pl/~s187772/psm/dodajDoGrupy.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.grupa);

        list = (ListView) findViewById(R.id.list);
        SharedPreferences pref = getSharedPreferences("GROUP", Context.MODE_PRIVATE);
        String groupName = pref.getString(MainActivity.GROUPN, "brak");
        TextView group = (TextView) findViewById(R.id.grupa);
        group.setText(groupName);
        new MateList().execute();
    }

    public void dodaj(View view) {
        empty = ((EditText) findViewById(R.id.user)).getText().toString();
        if(empty.equals("")) {
            Toast toast = Toast.makeText(Grupa.this, "Nie podano żadnej nazwy użytkownika", Toast.LENGTH_SHORT);
            toast.show();
        } else {
            mates.clear();
            new Mate().execute();
        }
    }

    private class MateList extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            boolean status = false;
            String response = "";

            SharedPreferences pref = getSharedPreferences("GROUP", Context.MODE_PRIVATE);
            String groupId = pref.getString(MainActivity.GROUP, "brak");

            HashMap<String, String> dane = new HashMap<String, String>();
            dane.put(TAG_ID, groupId);

            response = postData(urlGrupa, dane);

            if (!response.equalsIgnoreCase("")) {
                try {
                    JSONObject jRoot = new JSONObject(response);
                    JSONArray u = jRoot.getJSONArray("users");

                    if (u.length() == 0) {
                        mates.add("Nie dodałeś jeszcze żadnych współlokatorów");
                    }

                    for (int i=0; i<u.length(); i++) {
                        JSONObject element = u.getJSONObject(i);
                        String name = element.getString("name");
                        mates.add(name);
                    }

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
            if (result) {
                if (mates.get(0).equalsIgnoreCase("Nie dodałeś jeszcze żadnych współlokatorów")) {
                    adapter = new ArrayAdapter<String>(Grupa.this, R.layout.list_item2, R.id.row2, mates);
                    list.setAdapter(adapter);
                } else {
                    adapter = new ArrayAdapter<String>(Grupa.this, R.layout.list_item, R.id.row1, mates);
                    list.setAdapter(adapter);
                }
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

    // ----------------------------------------------------------------------------------------

    private class Mate extends AsyncTask<Void, Void, Boolean> {
        String newUser;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            newUser = ((EditText) findViewById(R.id.user)).getText().toString();
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            boolean status = false;
            String response = "";

            SharedPreferences pref = getSharedPreferences("GROUP", Context.MODE_PRIVATE);
            String groupId = pref.getString(MainActivity.GROUP, "brak");

            HashMap<String, String> dane = new HashMap<String, String>();
            dane.put(TAG_NAME, newUser);
            dane.put(TAG_GROUP, groupId);

            response = postData(urlDodajDoGrupy, dane);

            if (!response.equalsIgnoreCase("")) {
                status = true;
            } else {
                status = false;
            }

            return status;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (result) {
                ((EditText) findViewById(R.id.user)).setText("");
                new MateList().execute();
            } else {
                Toast toast = Toast.makeText(Grupa.this, "Nie udało się dodać współlokatora. Spróbuj jeszcze raz.", Toast.LENGTH_SHORT);
                toast.show();
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
