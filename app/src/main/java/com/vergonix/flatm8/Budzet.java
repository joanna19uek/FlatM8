package com.vergonix.flatm8;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
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
import java.util.HashMap;

import javax.net.ssl.HttpsURLConnection;

public class Budzet extends AppCompatActivity {

    private static final String TAG_GROUP_ID = "g_id";
    private static final String TAG_BUDGET = "budget";
    private String newBudget, savings, budgetValue;

    String urlDodajbudzet = "http://v-ie.uek.krakow.pl/~s187772/psm/ustawBudzet.php",
            urlSumaWydatkow = "http://v-ie.uek.krakow.pl/~s187772/psm/sumaWydatkow.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.budzet);

        newBudget = ((EditText) findViewById(R.id.newBudget)).getText().toString();
        budgetValue = ((TextView) findViewById(R.id.budgetValue)).getText().toString();
        savings = ((TextView) findViewById(R.id.savings)).getText().toString();
    }

    public void setBudget(View view) {
        new Budget().execute();
    }

    private class Budget extends AsyncTask<Void, Void, Boolean> {

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
            dane.put(TAG_GROUP_ID, groupId);
            dane.put(TAG_BUDGET, newBudget);

            response = postData(urlDodajbudzet, dane);

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
                ((TextView) findViewById(R.id.budgetValue)).setText(budgetValue);
                ((TextView) findViewById(R.id.savings)).setText(savings);
                ((EditText) findViewById(R.id.newBudget)).setText("");
                new Budzet.savedBudget().execute();
            } else {
                Toast toast = Toast.makeText(Budzet.this, "Nie udało się zmienić wartości budżetu. Spróbuj jeszcze raz.", Toast.LENGTH_SHORT);
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

    private class savedBudget extends AsyncTask<Void, Void, Boolean> {
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
            dane.put(TAG_GROUP_ID, groupId);

            response = postData(urlSumaWydatkow, dane);

            if (!response.equalsIgnoreCase("")) {
                try {
                    JSONObject jRoot = new JSONObject(response);
                    JSONArray u = jRoot.getJSONArray("expenses");

                    if (u.length() == 0) {
                        budgetValue += " Budżet nie został ustawiony";
                    } else {
                        JSONObject element = u.getJSONObject(0);
                        String budget = element.getString("budget");
                        Double expenses = Double.parseDouble(element.getString("costSum"));

                        budgetValue += budget;
                        savings += " " + (Double.parseDouble(budget) - expenses);
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
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
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
