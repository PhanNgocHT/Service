package com.phongbm.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import java.io.IOException;

// * Phạm vi sử dụng:
// 1. Internal service
// 2. External service: AIDL: Android interface definition language
// * Cách sử dụng
// 1. start service
// 2. bound service (client - server)
// 3. Đối tượng IntentService: start xong service, service chạy, chạy xong thì dừng hẳn

public class MediaService extends Service {
    private static final String TAG = "MediaService";

    private static final int NOTIFICATION_ID = 100;

    private static final int STATE_IDLE = 1;
    private static final int STATE_PLAYING = 2;
    private static final int STATE_PAUSED = 3;

    private MediaPlayer mediaPlayer;
    private int state;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate:...");
        state = STATE_IDLE;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) {
            return Service.START_STICKY;
        }

        switch (intent.getAction()) {
            case Key.SERVICE_ACTION_PLAY:
                String songUri = intent.getStringExtra(Key.SONG_URI);
                if (songUri != null) {
                    if (state != STATE_IDLE) {
                        stopSong();
                    }
                    startSong(Uri.parse(songUri));
                    startForegroundMediaService();
                }
                return START_STICKY;

            case Key.SERVICE_ACTION_NEXT:
                return START_STICKY;

            case Key.SERVICE_ACTION_PREVIOUS:
                return START_STICKY;

            case Key.SERVICE_ACTION_PAUSE:
                return START_STICKY;

            case Key.SERVICE_ACTION_RESUME:
                resumeSong();
                return START_STICKY;

            case Key.SERVICE_ACTION_STOP:
                stopSong();
                return START_STICKY;

            default:
                return START_STICKY;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy:...");
        super.onDestroy();
    }

    private void startSong(Uri uri) {
        if (state == STATE_IDLE) {
            try {
                mediaPlayer = new MediaPlayer();
                mediaPlayer.setDataSource(this, uri);
                mediaPlayer.prepareAsync();

                mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer player) {
                        mediaPlayer.start();
                        state = STATE_PLAYING;
                    }
                });

                mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mediaPlayer) {
                        stopSong();
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void pauseSong() {
    }

    private void resumeSong() {
    }

    private void stopSong() {
        if (state != STATE_IDLE) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
            state = STATE_IDLE;

            stopForeground(true);
            stopSelf();
        }
    }

    private void startForegroundMediaService() {
        Bitmap largeIcon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_media_large);

        Intent stopIntent = new Intent(this, MediaService.class);
        stopIntent.setAction(Key.SERVICE_ACTION_STOP);
        PendingIntent pendingIntentStop = PendingIntent.getService(this, 0, stopIntent, 0);

        PendingIntent pendingIntentPrevious = PendingIntent.getService(this, 0, new Intent(), 0);
        PendingIntent pendingIntentPausePlay = PendingIntent.getService(this, 0, new Intent(), 0);
        PendingIntent pendingIntentNext = PendingIntent.getService(this, 0, new Intent(), 0);

        Notification notification = new NotificationCompat.Builder(this)
                .setAutoCancel(false)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(largeIcon)
                .setContentTitle("Mãi mãi một tình yêu")
                .setContentText("Đan Trường")
                .setStyle(new NotificationCompat.MediaStyle())
                .addAction(android.R.drawable.ic_media_previous, "Previous", pendingIntentPrevious)
                .addAction(android.R.drawable.ic_media_pause, "Pause", pendingIntentPausePlay)
                .addAction(android.R.drawable.ic_media_next, "Next", pendingIntentNext)
                .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Stop", pendingIntentStop)
                .setWhen(System.currentTimeMillis())
                .setShowWhen(false)
                .setOngoing(true)
                .build();
        startForeground(NOTIFICATION_ID, notification);
    }

}