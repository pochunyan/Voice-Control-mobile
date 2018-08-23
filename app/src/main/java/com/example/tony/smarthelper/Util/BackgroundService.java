package com.example.tony.smarthelper.Util;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

public class BackgroundService extends Service {
    private final IBinder mBinder = new LocalBinder();

    public class LocalBinder extends Binder {
        BackgroundService getService() {
            return BackgroundService.this;
        }
    }


    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }
}
