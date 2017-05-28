package com.vergonix.flatm8;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.lzyzsd.circleprogress.ArcProgress;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import javax.net.ssl.HttpsURLConnection;

public class Main extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private static final String TAG_GROUPID = "groupId";
    private static final String TAG_DATE = "date";
    String url = "http://v-ie.uek.krakow.pl/~s187772/psm/ostatnieZakupy.php";
    private ListView list ;
    private ArrayAdapter<String> adapter ;
    public ArrayList<String> shoppings = new ArrayList<String>();
    private static final String TAG_GROUP_ID = "g_id";
    private int progress;
    private String bottomText;
    NumberFormat formatter = new DecimalFormat("#0");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Main.this, noweAktywnosci.class);
                startActivity(intent);
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        list = (ListView) findViewById(R.id.shopList);

        new reminderNotifications().execute();
        new retrivedBudget().execute();
    }

    @Override
    protected void onResume() {
        super.onResume();
        new retrivedBudget().execute();
        new LastCostList().execute();
    }

    private void progressController() {
        final ArcProgress arcProgress = (ArcProgress) findViewById(R.id.arc_progress);
        if(progress <= 100) {
            arcProgress.setProgress(progress);
        } else {
            arcProgress.setProgress(100);
        }

        arcProgress.setBottomText(bottomText);
        arcProgress.setArcAngle(270);
    }

    //----------------------------------------------------------------------------------------------------

    private class retrivedBudget extends AsyncTask<Void, Void, Boolean> {
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

            response = postData("http://v-ie.uek.krakow.pl/~s187772/psm/sumaWydatkow.php", dane);

            if (!response.equalsIgnoreCase("")) {
                try {
                    JSONObject jRoot = new JSONObject(response);
                    JSONArray u = jRoot.getJSONArray("expenses");

                    if (u.length() == 0) {
                        progress = 0;
                    } else {
                        JSONObject element = u.getJSONObject(0);
                        String budget = element.getString("budget");
                        //int matesExpenses = Integer.parseInt(element.getString("costSum"));
                        //int matesBudget = Integer.parseInt(element.getString("budget"));
                        Double expenses = Double.parseDouble(element.getString("costSum"));

                        if (budget.equalsIgnoreCase("brak")) {
                            progress = 0;
                        } else {
                            progress = (int) ((expenses*100)/Double.parseDouble(budget));
                            bottomText = formatter.format(expenses) + "/" + formatter.format(Double.parseDouble(element.getString("budget"))) + " zł";
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
            if(result) {
                progressController();
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

    //-----------------------------------------------------------------------------------------------------------------

    private class reminderNotifications extends AsyncTask<Void, Void, Boolean> {
        private String toastMessage;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Boolean result) {
            Toast toast = Toast.makeText(Main.this, toastMessage, Toast.LENGTH_LONG);
            toast.show();
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            boolean status = false;
            String response = "";

            SharedPreferences pref = getSharedPreferences("GROUP", Context.MODE_PRIVATE);
            String groupId = pref.getString(MainActivity.GROUP, "brak");

            HashMap<String, String> dane = new HashMap<String, String>();
            dane.put(TAG_GROUP_ID, groupId);

            response = postData("http://v-ie.uek.krakow.pl/~s187805/SM/testToast.php", dane);

            if (!response.equalsIgnoreCase("")) {
                try {
                    JSONObject jRoot = new JSONObject(response);
                    JSONArray u = jRoot.getJSONArray("reminders");

                    if (u.length() == 0) {
                        toastMessage = "Brak nadchodzących terminów zapłaty w najblższym tygodniu";
                    } else {
                        toastMessage = "Przypominamy o zbliżających się terminach zapłaty, sprawdź przypomnienia aby uzyskać więcej informacji";
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

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_group) {
            Intent intent1 = new Intent(this, Grupa.class);
            startActivity(intent1);
        } else if (id == R.id.nav_costs) {
            Intent intent1 = new Intent(this, Wydatki.class);
            startActivity(intent1);
        } else if (id == R.id.nav_notifications) {
            Intent intent1 = new Intent(this, Przypomnienia.class);
            startActivity(intent1);
        } else if (id == R.id.nav_budget) {
            Intent intent1 = new Intent(this, Budzet.class);
            startActivity(intent1);
        } else if (id == R.id.nav_bugs) {
            Intent intent1 = new Intent(this, Bugi.class);
            startActivity(intent1);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    class LastCostList extends AsyncTask<Void, Void, Boolean> {
        String response = "";
        Boolean status = false;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            shoppings.clear();
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            SharedPreferences pref = getSharedPreferences("GROUP", Context.MODE_PRIVATE);
            String groupId = pref.getString(MainActivity.GROUP, "brak");

            Calendar calendar = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
            String date = sdf.format(calendar.getTime());

            HashMap<String, String> dane = new HashMap<String, String>();
            dane.put(TAG_GROUPID, groupId);
            dane.put(TAG_DATE, date);

            response = postData(url, dane);

            if (!response.equalsIgnoreCase("")) {
                try {
                    JSONObject jRoot = new JSONObject(response);
                    JSONArray u = jRoot.getJSONArray("shoppings");

                    if (u.length() == 0) {
                        shoppings.add("Nie dodałeś jeszcze żadnych zakupów");
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
                if (shoppings.get(0).equalsIgnoreCase("Nie dodałeś jeszcze żadnych zakupów")) {
                    adapter = new ArrayAdapter<String>(Main.this, R.layout.list_cost2, R.id.row, shoppings);
                    list.setAdapter(adapter);
                } else {
                    adapter = new ArrayAdapter<String>(Main.this, R.layout.list_cost1, R.id.shopping, shoppings);
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