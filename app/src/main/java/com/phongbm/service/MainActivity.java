package com.phongbm.service;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private ServiceConnection connection;
    private GpsService gpsService;
    private boolean isServiceConnected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initializeComponents();
        connectService();
    }

    private void initializeComponents() {
        findViewById(R.id.btn_start_service).setOnClickListener(this);
        findViewById(R.id.btn_get_my_location).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_start_service:
                Intent intent = new Intent(this, MediaService.class);
                intent.setAction(Key.SERVICE_ACTION_PLAY);
                intent.putExtra(Key.SONG_URI, "http://zmp3-mp3-s1-te-zmp3-fpthn-2.zadn.vn/baafbf3d2579cc279568/1325612997669102955?key=UG-iOBUXARY5m3pPVe0E4g&expires=1500638584");
                startService(intent);

                // Stop service
                // Intent intent = new Intent(this, MediaService.class);
                // stopService(intent);
                break;

            case R.id.btn_get_my_location:
                if (isServiceConnected) {
                    // gpsService.getMyLocation();
                    gpsService.downloadPicture("https://cdn1.thehunt.com/app/public/system/zine_images/3809557/original/f6f30528102c423928eb87f8b5c4e7af.jpg");
                }
                break;

            default:
                break;
        }
    }

    private void connectService() {
        connection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                if (iBinder instanceof GpsService.GpsBinder) {
                    gpsService = ((GpsService.GpsBinder) iBinder).getGpsService();
                    isServiceConnected = true;
                }
            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {
                isServiceConnected = false;
            }
        };

        Intent intent = new Intent(this, GpsService.class);
        bindService(intent, connection, Service.BIND_AUTO_CREATE);
    }

    private void disconnectService() {
        unbindService(connection);
    }

    @Override
    protected void onDestroy() {
        disconnectService();
        super.onDestroy();
    }

}