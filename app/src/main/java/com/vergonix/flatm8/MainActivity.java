package com.vergonix.flatm8;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {
    public static final String GROUP = "com.vergonix.flatm8.GROUP";
    public static final String GROUPN = "com.vergonix.flatm8.GROUPN";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
    }

    public void wybierzGrupe(View view) {
        Intent intent = new Intent(this, Zaloguj.class);
        startActivity(intent);
    }

    public void nowaGrupa(View view) {
        Intent intent2 = new Intent(this, Zarejestruj.class);
        startActivity(intent2);
    }
}
