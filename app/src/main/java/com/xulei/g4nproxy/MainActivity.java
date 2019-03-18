package com.xulei.g4nproxy;

import android.content.Intent;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView textView = findViewById(R.id.text);
        String clientKey = Settings.System.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        textView.setText("device: " + clientKey);

        Intent intent = new Intent(this, HttpProxyService.class);
        startService(intent);
    }
}
