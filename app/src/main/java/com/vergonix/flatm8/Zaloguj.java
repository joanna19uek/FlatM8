package com.vergonix.flatm8;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

import javax.net.ssl.HttpsURLConnection;

public class Zaloguj extends AppCompatActivity {
    String group, password;
    private ProgressDialog pDialog;
    private static final String TAG_EMAIL = "email";
    private static final String TAG_PASSWORD = "password";
    String url = "http://v-ie.uek.krakow.pl/~s187772/psm/logowanie.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.zaloguj);
    }

    public void zaloguj(View view) {
        group = ((EditText) findViewById(R.id.nazwa)).getText().toString();
        password = ((EditText) findViewById(R.id.haslo)).getText().toString();

        new Login().execute();
    }

    private class Login extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(Zaloguj.this);
            pDialog.setMessage("Trwa logowanie..");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            boolean status = false;
            String response = "";

            HashMap<String, String> dane = new HashMap<String, String>();
            dane.put(TAG_EMAIL, group);
            dane.put(TAG_PASSWORD, password);

            response = postData(url, dane);
            if (!response.equalsIgnoreCase("")) {
                try {
                    JSONObject jRoot = new JSONObject(response);
                    JSONArray u = jRoot.getJSONArray("groups");

                    if(u.length() == 1) {
                        JSONObject element = u.getJSONObject(0);
                        String id = element.getString("id");

                        if (Integer.valueOf(id) != 0) {
                            status = true;
                            SharedPreferences pref = getSharedPreferences("USER", Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = pref.edit();
                            editor.putString(MainActivity.USER, id);
                            editor.apply();
                        }
                    } else {
                        status = false;
                    }


                } catch (JSONException e) {
                    // displayLoding(false);
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
                Intent mainIntent = new Intent(Zaloguj.this, Main.class);
                startActivity(mainIntent);
            } else {
                Toast toast = Toast.makeText(Zaloguj.this, "Błąd w logowaniu. Spróbuj jeszcze raz.", Toast.LENGTH_SHORT);
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
