package com.example.farith.touchkinassignment;

import android.app.ActivityManager;
import android.app.Application;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.util.List;


/*
for adding notification and action to notification
@link https://developer.android.com/training/notify-user/build-notification
*/


public class ForegroundService extends Service {
    private static final String TAG = "ForegroundService";
    String NOTIFICATION_CHANNEL_ID = "com.example.farith.touchkinassignment";
    int FOREGROUND_SERVICE_ID = 1122334455;
    NotificationCompat.Builder notification;
    MediaPlayer mediaPlayer;
    int length;
    Boolean isPaused = false;

    @Override
    public void onCreate() {
        super.onCreate();
        //Defining intent so that on clicking the  notification the activity is opened
        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, 0);
        NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(NOTIFICATION_SERVICE);
        // Oreo version has certain limits to display notification, notification channel have to be defined
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, "NOTIFICATION", NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(notificationChannel);
        }
        notification = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
        notification.setSmallIcon(R.drawable.play_music);
        Bitmap bitmap = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.play_music);
        notification.setLargeIcon(bitmap);
        notification.setTicker("MUSIC");
        notification.setContentTitle("PLAYING MUSIC");
        notification.setPriority(NotificationCompat.PRIORITY_DEFAULT);
        notification.setContentIntent(pendingIntent);
        //defining pending intents
        //for pause function
        pauseFunction();
        //for play function
        playFunction();
        //for canceling the foreground service
        cancelFunction();
        //for retry option
        retryFunction();
    }

    private void cancelFunction() {
        Intent cancelIntent = new Intent(this, ForegroundService.class);
        cancelIntent.setAction("CANCEL_INTENT");
        cancelIntent.setClass(this, ForegroundService.class);
        PendingIntent cancelPendingIntent = PendingIntent.getService(ForegroundService.this, 0, cancelIntent, 0);
        notification.addAction(R.drawable.ic_cancel_black_24dp, "", cancelPendingIntent);
    }

    private void pauseFunction() {
        Intent pauseIntent = new Intent(this, ForegroundService.class);
        pauseIntent.setAction("PAUSE_INTENT");
        pauseIntent.setClass(this, ForegroundService.class);
        PendingIntent pausePendingIntent = PendingIntent.getService(ForegroundService.this, 0, pauseIntent, 0);
        notification.addAction(R.drawable.ic_pause_black_24dp, "", pausePendingIntent);
    }

    private void playFunction() {
        Intent playIntent = new Intent(this, ForegroundService.class);
        playIntent.setAction("PLAY_INTENT");
        playIntent.setClass(this, ForegroundService.class);
        PendingIntent playPendingIntent = PendingIntent.getService(ForegroundService.this, 0, playIntent, 0);
        notification.addAction(R.drawable.ic_play_arrow_black_24dp, "", playPendingIntent);

    }

    private void retryFunction() {
        Intent retryIntent = new Intent(this, ForegroundService.class);
        retryIntent.setAction("RETRY_INTENT");
        PendingIntent retryPendingIntent = PendingIntent.getService(ForegroundService.this, 0, retryIntent, 0);
        notification.addAction(R.drawable.ic_replay_black_24dp, "", retryPendingIntent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        startForeground(FOREGROUND_SERVICE_ID, notification.build());
        if (intent.getAction().equals("START_FOREGROUND_SERVICE")) {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            try {
                mediaPlayer.setDataSource("http://content.touchkin.com/audio/Mindful6-7-7s.mp3");
                mediaPlayer.prepare();
            } catch (IOException e) {
                Toast.makeText(getApplicationContext(), "There is problem in the file path", Toast.LENGTH_SHORT).show();
            }
            mediaPlayer.start();
        }
        if (intent.getAction().equals("PLAY_INTENT")) {
            if (isPaused) {
                mediaPlayer.seekTo(length);
                mediaPlayer.start();
                isPaused = false;
            }
            Log.d(TAG, "onStartCommand: PLAY BUTTON IS CLICKED");
        }
        if (intent.getAction().equals("PAUSE_INTENT")) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
                length = mediaPlayer.getCurrentPosition();
                isPaused = true;
            }
        }
        if (intent.getAction().equals("CANCEL_INTENT")) {
            Log.d(TAG, "onStartCommand: PAUSE BUTTON IS CLICKED");
            //removing the service from the foreground
            stopForeground(true);
            mediaPlayer.stop();
            stopSelf();
        }
        if (intent.getAction().equals("RETRY_INTENT")) {
            Log.d(TAG, "onStartCommand: RETRY_BUTTON IS CLICKED");
            mediaPlayer.setOnInfoListener(new MediaPlayer.OnInfoListener() {
                @Override
                public boolean onInfo(MediaPlayer mp, int what, int extra) {
                    switch (what) {
                        case MediaPlayer.MEDIA_INFO_BUFFERING_START:
                            if (networkAvailable()) {
                                Log.d(TAG, "onInfo: media is buffering");
                                try {
                                    mediaPlayer = new MediaPlayer();
                                    mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                                    mediaPlayer.setDataSource("http://content.touchkin.com/audio/Mindful6-7-7s.mp3");
                                    mediaPlayer.prepare();
                                } catch (IOException e) {
                                    Toast.makeText(getApplicationContext(), "There is problem in the file path", Toast.LENGTH_SHORT).show();
                                }
                                mediaPlayer.start();
                            } else {
                                Toast.makeText(getApplicationContext(), "Connect to internet and try again", Toast.LENGTH_SHORT).show();

                            }
                    }
                    return true;
                }
            });

        }


        return START_STICKY;
    }

    //check for internet connection
    private boolean networkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}
