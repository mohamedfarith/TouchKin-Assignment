package com.example.farith.touchkinassignment;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

//@link https://developer.android.com/guide/components/services


public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private Button btnPlay;
    private Button btnPause;
    private Button btnCancel;
    private Button btnRetry;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btnPlay = findViewById(R.id.btn_play);
        btnPause = findViewById(R.id.btn_pause);
        btnCancel = findViewById(R.id.btn_cancel);
        btnRetry = findViewById(R.id.btn_retry);
        btnPlay.setOnClickListener(this);
        btnPause.setOnClickListener(this);
        btnCancel.setOnClickListener(this);
        btnRetry.setOnClickListener(this);
    }

    //check for internet connection
    private boolean networkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    //checks for whether the service for the particular name is running or not
    private boolean isMyServiceRunning(Class serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_play:
                if (networkAvailable()) {
                    //starting the service with check if the service is still alive
                    if (!isMyServiceRunning(ForegroundService.class)) {
                        // executes when service  is not running
                        Intent intent = new Intent(MainActivity.this, ForegroundService.class);
                        intent.setAction("START_FOREGROUND_SERVICE");
                        startService(intent);
                    } else {
                        //executes when service is running i.e resuming the music
                        Intent intent = new Intent(MainActivity.this, ForegroundService.class);
                        intent.setAction("PLAY_INTENT");
                        startService(intent);
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "Connect to internet and try again", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.btn_pause:
                if (isMyServiceRunning(ForegroundService.class)) {
                    Intent intent = new Intent(MainActivity.this, ForegroundService.class);
                    intent.setAction("PAUSE_INTENT");
                    startService(intent);
                }

                break;
            case R.id.btn_cancel:
                if (isMyServiceRunning(ForegroundService.class)) {
                    Intent intent = new Intent(MainActivity.this, ForegroundService.class);
                    intent.setAction("CANCEL_INTENT");
                    startService(intent);
                }
                break;
            case R.id.btn_retry:
                if (isMyServiceRunning(ForegroundService.class)) {
                    Intent intent = new Intent(MainActivity.this, ForegroundService.class);
                    intent.setAction("RETRY_INTENT");
                    startService(intent);
                }
                break;
        }
    }
}
