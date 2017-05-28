package com.vergonix.flatm8;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import javax.net.ssl.HttpsURLConnection;

public class noweAktywnosci extends AppCompatActivity {
    private static final String TAG_ID = "id";
    private Spinner mateList;
    private String url = "http://v-ie.uek.krakow.pl/~s187772/psm/listaLokatorow.php";
    private String urlShop = "http://v-ie.uek.krakow.pl/~s187772/psm/dodajZakup.php";
    private ArrayAdapter<String> adapter ;
    private ArrayList<String> mates = new ArrayList<String>();
    private EditText dateEdit, item, cost;
    private Calendar myCalendar;
    private static final String TAG_GROUPID = "g_id";
    private static final String TAG_USER = "user";
    private static final String TAG_DATE = "date";
    private static final String TAG_ITEM = "item";
    private static final String TAG_COST = "cost";
    private String group, mateValue, dateValue, itemValue, costValue;
    private SimpleDateFormat sdf;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.nowe_aktywnosci);

        mateList = (Spinner) findViewById(R.id.flatMate);
        new MateList().execute();

        dateEdit = (EditText) findViewById(R.id.dateEdit);
        myCalendar = Calendar.getInstance();
        sdf = new SimpleDateFormat("yyyy-MM-dd");

        dateEdit.setText(sdf.format(myCalendar.getTime()));

        dateEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerDialog(noweAktywnosci.this, date, myCalendar.get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });
    }

    DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
            myCalendar.set(Calendar.YEAR, year);
            myCalendar.set(Calendar.MONTH, month);
            myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            updateLabel();
        }
    };

    public void updateLabel() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        dateEdit.setText(sdf.format(myCalendar.getTime()));
    }

    public void dodajZakup(View view) {
        item = (EditText) findViewById(R.id.shopItem);
        cost = (EditText) findViewById(R.id.cena);

        new Shopping().execute();
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

    private class Shopping extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            mateValue = mateList.getSelectedItem().toString();
            dateValue = dateEdit.getText().toString();
            itemValue = item.getText().toString();
            costValue = cost.getText().toString();

            SharedPreferences pref = getSharedPreferences("GROUP", Context.MODE_PRIVATE);
            group = pref.getString(MainActivity.GROUP, "brak");
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            String response = "";
            Boolean status = false;

            HashMap<String, String> dane = new HashMap<String, String>();
            dane.put(TAG_GROUPID, group);
            dane.put(TAG_USER, mateValue);
            dane.put(TAG_DATE, dateValue);
            dane.put(TAG_ITEM, itemValue);
            dane.put(TAG_COST, costValue);

            response = postData(urlShop, dane);

            if (!response.equalsIgnoreCase("")) {
                status = true;
            } else {
                status = false;
            }

            return status;
        }

        @Override
        protected void onPostExecute(Boolean response) {
            if (response) {
                Toast toast = Toast.makeText(noweAktywnosci.this, "Dodano zakup.", Toast.LENGTH_SHORT);
                toast.show();
                dateEdit.setText(sdf.format(myCalendar.getTime()));
                item.setText("");
                cost.setText("");
            } else {
                Toast toast = Toast.makeText(noweAktywnosci.this, "Błąd przy dodawaniu. Spróbuj jeszcze raz.", Toast.LENGTH_SHORT);
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