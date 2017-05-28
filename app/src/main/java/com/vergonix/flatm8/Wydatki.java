package com.vergonix.flatm8;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

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

public class Wydatki extends AppCompatActivity {
    TextView okresView;
    Calendar kalendarz;
    int month, year;
    String okres;
    String[] miesiace = {"styczeń", "luty", "marzec", "kwiecień", "maj", "czerwiec", "lipiec", "sierpień",
            "wrzesień", "październik", "listopad", "grudzień"};
    String date;
    private static final String TAG_GROUPID = "groupId";
    private static final String TAG_DATE = "date";
    String url = "http://v-ie.uek.krakow.pl/~s187772/psm/zakupyMiesiaca.php";
    private ListView list ;
    private ArrayAdapter<String> adapter ;
    private ArrayList<String> shoppings = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wydatki);

        okresView = (TextView) findViewById(R.id.okres);
        kalendarz = Calendar.getInstance();
        month = kalendarz.get(Calendar.MONTH);
        year = kalendarz.get(Calendar.YEAR);

        okres = miesiace[month] + " " + year;
        okresView.setText(okres);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
        date = sdf.format(kalendarz.getTime());

        list = (ListView) findViewById(R.id.monthCostList);

        new MonthCostList().execute();
    }

    class MonthCostList extends AsyncTask<Void, Void, Boolean> {
        String response = "";
        Boolean status = false;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            SharedPreferences pref = getSharedPreferences("GROUP", Context.MODE_PRIVATE);
            String groupId = pref.getString(MainActivity.GROUP, "brak");

            HashMap<String, String> dane = new HashMap<String, String>();
            dane.put(TAG_GROUPID, groupId);
            dane.put(TAG_DATE, date);

            response = postData(url, dane);

            if (!response.equalsIgnoreCase("")) {
                try {
                    JSONObject jRoot = new JSONObject(response);
                    JSONArray u = jRoot.getJSONArray("shoppings");

                    if (u.length() == 0) {
                        shoppings.add("Brak zarejestrowanych zakupów w tym miesiącu");
                    }

                    for (int i=0; i<u.length(); i++) {
                        JSONObject element = u.getJSONObject(i);
                        String user = element.getString("user");
                        String item = element.getString("item");
                        String cost = element.getString("cost");
                        String row = user + "\n" + item + "\n" + cost;
                        shoppings.add(row);
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
                if (shoppings.get(0).equalsIgnoreCase("Brak zarejestrowanych zakupów w tym miesiącu")) {
                    adapter = new ArrayAdapter<String>(Wydatki.this, R.layout.list_cost2, R.id.row, shoppings);
                    list.setAdapter(adapter);
                } else {
                    adapter = new ArrayAdapter<String>(Wydatki.this, R.layout.list_cost1, R.id.shopping, shoppings);
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