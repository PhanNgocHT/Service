package com.phongbm.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

public class GpsService extends Service {
    private static final String TAG = "GpsService";

    public class GpsBinder extends Binder {
        public GpsService getGpsService() {
            return GpsService.this;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new GpsBinder();
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    public void getMyLocation() {
        try {
            Criteria criteria = new Criteria();
            criteria.setPowerRequirement(Criteria.ACCURACY_MEDIUM);

            LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            String provider = locationManager.getBestProvider(criteria, true);
            Location location = locationManager.getLastKnownLocation(provider);

            double latitude = location.getLatitude();
            double longitude = location.getLongitude();

            Log.d(TAG, "getMyLocation: " + latitude);
            Log.d(TAG, "getMyLocation: " + longitude);

            Uri uri = Uri.parse("geo:" + latitude + "," + longitude);
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setPackage("com.google.android.apps.maps");
            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivity(intent);
            }
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    public void downloadPicture(final String link) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // Download
                    URL url = new URL(link);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    InputStream input = connection.getInputStream();
                    Bitmap bitmap = BitmapFactory.decodeStream(input);

                    // Save
                    String path = Environment.getExternalStorageDirectory().getAbsolutePath() +
                            "/" + Environment.DIRECTORY_DOWNLOADS +
                            System.currentTimeMillis() + ".jpg";
                    File file = new File(path);
                    FileOutputStream output = new FileOutputStream(file);
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, output);

                    // Notify
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(path));
                    intent.setType("images/*");
                    PendingIntent pendingIntent = PendingIntent.getActivity(
                            GpsService.this, 0, intent, 0
                    );

                    Notification notification = new NotificationCompat.Builder(GpsService.this)
                            .setAutoCancel(true)
                            .setSmallIcon(R.mipmap.ic_launcher)
                            .setContentTitle("Download")
                            .setContentText("Successful")
                            .setContentIntent(pendingIntent)
                            .setDefaults(Notification.DEFAULT_ALL)
                            .build();

                    NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                    manager.notify(100, notification);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

}