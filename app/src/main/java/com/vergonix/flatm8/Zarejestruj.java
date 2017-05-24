package com.vergonix.flatm8;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

import javax.net.ssl.HttpsURLConnection;

public class Zarejestruj extends AppCompatActivity {
    private String user, pass, repass;
    private ProgressDialog pDialog;
    private static final String TAG_EMAIL = "email";
    private static final String TAG_PASSWORD = "password";
    String url = "http://v-ie.uek.krakow.pl/~s187772/psm/rejestracja.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.zarejestruj);
    }

    public void noweKonto(View view) {
        user = ((EditText) findViewById(R.id.nazwa)).getText().toString();
        pass = ((EditText) findViewById(R.id.haslo)).getText().toString();
        repass = ((EditText) findViewById(R.id.haslo2)).getText().toString();

        if (pass.equals(repass)) {
            new Register().execute();
        } else {
            Toast toast = Toast.makeText(this, "Podane hasła nie są identyczne. Spróbuj jeszcze raz.", Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    private class Register extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(Zarejestruj.this);
            pDialog.setMessage("Trwa rejestracja..");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            boolean status = false;
            String response = "";

            HashMap<String, String> dane = new HashMap<String, String>();
            dane.put(TAG_EMAIL, user);
            dane.put(TAG_PASSWORD, pass);

            response = postData(url, dane);

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
                Intent loginIntent = new Intent(Zarejestruj.this, Zaloguj.class);
                startActivity(loginIntent);
            } else {
                Toast toast = Toast.makeText(Zarejestruj.this, "Błąd w rejestracji. Spróbuj jeszcze raz.", Toast.LENGTH_SHORT);
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
                Log.i("KOD", String.valueOf(responseCode));

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
