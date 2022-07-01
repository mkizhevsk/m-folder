package com.mk.m_folder.data.service;

import android.app.Service;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import com.mk.m_folder.data.DBHelper;
import com.mk.m_folder.data.dto.DeletionDto;
import com.mk.m_folder.data.entity.Deletion;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BaseService extends Service {

    private final IBinder mBinder = new LocalBinder();

    DBHelper dbHelper;

    private static final String BASE_NAME = "my_music.db";

    private static final String DELETION_TABLE = "deletion";
    private static final String MUSIC_PATH_COLUMN = "music_path";

    private static final String SETTING_TABLE = "setting";
    private static final String TRACK_NAME_COLUMN = "track_name";
    private static final String ARTIST_NAME_COLUMN = "artist_name";
    private static final String ALBUM_NAME_COLUMN = "album_name";
    private static final String FILE_NAME_COLUMN = "file_name";


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

    // Deletion
    public void insertDeletion(String trackName, String artistName, String albumName, String fileName) {
        Log.d(TAG, "start insertDeletion..");
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues cv = new ContentValues();

        cv.put(TRACK_NAME_COLUMN, trackName);
        cv.put(ARTIST_NAME_COLUMN, artistName);
        cv.put(ALBUM_NAME_COLUMN, albumName);
        cv.put(FILE_NAME_COLUMN, fileName);

        long rowID = db.insert("deletion", null, cv);

        Log.d(TAG, "Deletion row inserted, ID = " + rowID);

        dbHelper.close();
    }

    private List<Deletion> getDeletionByCursor(Cursor deletionCursor) {
        List<Deletion> deletions = new ArrayList<>();

        if (deletionCursor.moveToFirst()) {
            int trackNameColIndex = deletionCursor.getColumnIndex(TRACK_NAME_COLUMN);
            int artistNameColIndex = deletionCursor.getColumnIndex(ARTIST_NAME_COLUMN);
            int albumNameColIndex = deletionCursor.getColumnIndex(ALBUM_NAME_COLUMN);
            int fileNameColIndex = deletionCursor.getColumnIndex(FILE_NAME_COLUMN);

            do {
                Deletion deletion = new Deletion();
                deletion.setTrackName(deletionCursor.getString(trackNameColIndex));
                deletion.setAlbumName(deletionCursor.getString(artistNameColIndex));
                deletion.setAlbumName(deletionCursor.getString(albumNameColIndex));
                deletion.setFileName(deletionCursor.getString(fileNameColIndex));

                deletions.add(deletion);
            } while (deletionCursor.moveToNext());

        } else Log.d(TAG, "0 rows");

        return deletions;
    }

    public List<Deletion> getDeletions() {
        Log.d(TAG, "start getDeletions");
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        Cursor deletionCursor = db.query(DELETION_TABLE, null, null, null, null, null, null);

        List<Deletion> deletions = getDeletionByCursor(deletionCursor);

        deletionCursor.close();

        dbHelper.close();

        return deletions;
    }

    // Setting
    public void saveSettings(String path) {
        Log.d(TAG, "saveSettings: " + path);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues cv = new ContentValues();
        cv.put(MUSIC_PATH_COLUMN, path);

        String sql = "SELECT * FROM "  + SETTING_TABLE;
        Cursor settingCursor = db.rawQuery(sql,null);
        if(settingCursor.moveToFirst()) { //existed row
            int updCount = db.update(SETTING_TABLE, cv, "id = " + 1, null);
            Log.d(TAG, "  setting updated rows count  = " + updCount);
        } else { // insert a new row
            long rowID = db.insert(SETTING_TABLE, null, cv);
            Log.d(TAG, "  setting row inserted, ID = " + rowID);
        }
        settingCursor.close();

        dbHelper.close();
    }

    public List<String> getSettings() {
        Log.d(TAG, "start getSettings..");
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        String[] settings = new String[1];
        String sql = "SELECT * FROM "  + SETTING_TABLE;
        Cursor settingCursor = db.rawQuery(sql,null);
        if (settingCursor.moveToFirst()) {
            int pathColIndex = settingCursor.getColumnIndex(MUSIC_PATH_COLUMN);
            settings[0] = settingCursor.getString(pathColIndex);
            Log.d(TAG, "Settings were red");
        } else {
            Log.d(TAG, "Settings: 0 rows");
            return null;
        }
        settingCursor.close();

        dbHelper.close();
        return Arrays.asList(settings);
    }
}
