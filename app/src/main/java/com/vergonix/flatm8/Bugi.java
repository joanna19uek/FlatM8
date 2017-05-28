package com.vergonix.flatm8;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class Bugi extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bugi);
    }

    public void sendData(View view) {
        String bugDesc = ((EditText) findViewById(R.id.errorDesc)).getText().toString();

        String[] TO = {"pietralikpawel@gmail.com", "asia_p99@tlen.pl"};
        String CC = "Zgłoszenie błedu w aplikacji FlatM8";
        Intent emailIntent = new Intent(Intent.ACTION_SEND);

        emailIntent.setData(Uri.parse("mailto:"));
        emailIntent.setType("text/plain");
        emailIntent.putExtra(Intent.EXTRA_EMAIL, TO);
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, CC);
        emailIntent.putExtra(Intent.EXTRA_TEXT, bugDesc);

        try {
            startActivity(Intent.createChooser(emailIntent, "Dziekujemy za pomoc w rozwoju aplikacji!"));
            finish();
        }
        catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(Bugi.this, "Nie ma żadnego programu do wysyłania maili", Toast.LENGTH_SHORT).show();
        }
    }
}
