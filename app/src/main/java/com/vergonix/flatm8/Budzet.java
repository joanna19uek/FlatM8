package com.vergonix.flatm8;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
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
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

import javax.net.ssl.HttpsURLConnection;

public class Budzet extends AppCompatActivity {

    private static final String TAG_GROUP_ID = "g_id";
    private static final String TAG_BUDGET = "budget";
    private static final String TAG_DATE = "date";
    private String newBudget, savings, budgetValue;
    private TextView budgetView, savingsView;

    String urlDodajbudzet = "http://v-ie.uek.krakow.pl/~s187772/psm/ustawBudzet.php",
            urlSumaWydatkow = "http://v-ie.uek.krakow.pl/~s187772/psm/sumaWydatkow.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.budzet);

        budgetView = (TextView) findViewById(R.id.budgetValue);
        savingsView = (TextView) findViewById(R.id.savings);

        new SavedBudget().execute();
    }

    public void setBudget(View view) {
        newBudget = ((EditText) findViewById(R.id.newBudget)).getText().toString();
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
                new SavedBudget().execute();
                Toast toast = Toast.makeText(Budzet.this, "Zapisano", Toast.LENGTH_SHORT);
                toast.show();
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

    private class SavedBudget extends AsyncTask<Void, Void, Boolean> {
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

            Calendar cal = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
            String date = sdf.format(cal.getTime());

            HashMap<String, String> dane = new HashMap<String, String>();
            dane.put(TAG_GROUP_ID, groupId);
            dane.put(TAG_DATE, date);

            response = postData(urlSumaWydatkow, dane);

            if (!response.equalsIgnoreCase("")) {
                try {
                    JSONObject jRoot = new JSONObject(response);
                    JSONArray u = jRoot.getJSONArray("expenses");

                    if (u.length() == 0) {
                        budgetValue = "Budżet nie został jeszcze ustawiony";
                        savings = "0 zł";
                    } else {
                        JSONObject element = u.getJSONObject(0);
                        String budget = element.getString("budget");
                        Double expenses = Double.parseDouble(element.getString("costSum"));

                        if (budget.equalsIgnoreCase("brak")) {
                            budgetValue = "Budżet nie został jeszcze ustawiony";
                        } else {
                            budgetValue = budget + " zł";
                        }

                        if (expenses != 0.00) {
                            savings = String.valueOf(Double.parseDouble(budget) - expenses) + " zł";
                        }

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
                budgetView.setText(budgetValue);
                savingsView.setText(savings);
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
