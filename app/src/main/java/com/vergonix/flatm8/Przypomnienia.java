package com.vergonix.flatm8;

import android.app.DatePickerDialog;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.HashMap;

import javax.net.ssl.HttpsURLConnection;

public class Przypomnienia extends AppCompatActivity {

    private Calendar myCalendar;
    private EditText myDate;
    private ListView list ;
    private ArrayAdapter<String> adapter ;
    private ArrayList<String> reminders = new ArrayList<String>();
    private static final String TAG_GROUP_ID = "g_id";
    private static final String TAG_TITLE = "title";
    private static final String TAG_DATE = "date";
    private static final String TAG_SUM = "sum";

    String urlPrzypomnienie = "http://v-ie.uek.krakow.pl/~s187772/psm/przypomnienieOdczyt.php",
            urlDodajPrzypomnienie = "http://v-ie.uek.krakow.pl/~s187772/psm/przypomnienieZapis.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.przypomnienia);

        list = (ListView) findViewById(R.id.list);

        myDate = (EditText) findViewById(R.id.reminderDate);
        myCalendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        myDate.setText(sdf.format(myCalendar.getTime()));

        myDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerDialog(Przypomnienia.this, date, myCalendar.get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        new ReminderList().execute();
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
        myDate.setText(sdf.format(myCalendar.getTime()));
    }

    public void addReminder(View view) {
        reminders.clear();
        new Reminder().execute();
    }

    private class Reminder extends AsyncTask<Void, Void, Boolean> {

        String reminderTitle, reminderDate, reminderSum;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            reminderTitle = ((EditText) findViewById(R.id.reminderTitle)).getText().toString();
            reminderSum = ((EditText) findViewById(R.id.reminderSum)).getText().toString();
            reminderDate = ((EditText) findViewById(R.id.reminderDate)).getText().toString();
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            boolean status = false;
            String response = "";

            SharedPreferences pref = getSharedPreferences("GROUP", Context.MODE_PRIVATE);
            String groupId = pref.getString(MainActivity.GROUP, "brak");

            HashMap<String, String> dane = new HashMap<String, String>();
            dane.put(TAG_GROUP_ID, groupId);
            dane.put(TAG_TITLE, reminderTitle);
            dane.put(TAG_DATE, reminderDate);
            dane.put(TAG_SUM, reminderSum);

            response = postData(urlDodajPrzypomnienie, dane);

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
                ((EditText) findViewById(R.id.reminderTitle)).setText("");
                ((EditText) findViewById(R.id.reminderSum)).setText("");
                ((EditText) findViewById(R.id.reminderDate)).setText("");
                new ReminderList().execute();
            } else {
                Toast toast = Toast.makeText(Przypomnienia.this, "Nie udało się dodać przypomnienia. Spróbuj jeszcze raz.", Toast.LENGTH_SHORT);
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

    private class ReminderList extends AsyncTask<Void, Void, Boolean> {

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

            response = postData(urlPrzypomnienie, dane);

            if (!response.equalsIgnoreCase("")) {
                try {
                    JSONObject jRoot = new JSONObject(response);
                    JSONArray u = jRoot.getJSONArray("reminders");

                    if (u.length() == 0) {
                        reminders.add("Nie dodałeś jeszcze żadnych przypomnień");
                    }

                    for (int i=0; i<u.length(); i++) {
                        JSONObject element = u.getJSONObject(i);
                        String title = element.getString("title");
                        String date = element.getString("date");
                        String sum = element.getString("sum");
                        String row = title + "\nTermin zapłaty: " + date + "\nKwota: " + sum + " zł";
                        reminders.add(row);
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
                if (reminders.get(0).equalsIgnoreCase("Nie dodałeś jeszcze żadnych przypomnień")) {
                    adapter = new ArrayAdapter<String>(Przypomnienia.this, R.layout.list_reminder1, R.id.row1, reminders);
                    list.setAdapter(adapter);
                } else {
                    adapter = new ArrayAdapter<String>(Przypomnienia.this, R.layout.list_reminder1, R.id.row1, reminders);
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
}

