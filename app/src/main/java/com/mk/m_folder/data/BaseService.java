package com.mk.m_folder.data;

import android.app.Service;
import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

public class BaseService extends Service {

    private final IBinder mBinder = new LocalBinder();

    DBHelper dbHelper;

    private static final String dbName = "my_music.db";

    private static final String TAG = "MainActivity";

    public void onCreate() {
        super.onCreate();

        dbHelper = new DBHelper(this);
        Log.d(TAG, "BaseService onCreate");
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "BaseService onStartCommand");
        return super.onStartCommand(intent, flags, startId);
    }

    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "BaseService onDestroy");
    }

    public class LocalBinder extends Binder {
        public BaseService getService() {
            return BaseService.this;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public void insertDeletion(String trackName, String artistName, String albumName, String fileName) {
        Log.d(TAG, "start insert..");
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues cv = new ContentValues();

        cv.put("track_name", trackName);
        cv.put("artist_name", artistName);
        cv.put("album_name", albumName);
        cv.put("file_name", fileName);

        long rowID = db.insert("deletion", null, cv);

        Log.d(TAG, "Deletion row inserted, ID = " + rowID);

        dbHelper.close();
    }






}
