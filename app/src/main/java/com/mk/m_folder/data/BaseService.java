package com.mk.m_folder.data;

import android.app.Service;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import com.mk.m_folder.data.dto.DeletionDto;

import java.util.ArrayList;
import java.util.List;

public class BaseService extends Service {

    private final IBinder mBinder = new LocalBinder();

    DBHelper dbHelper;

    private static final String BASE_NAME = "my_music.db";
    private static final String DELETION_TABLE = "deletion";
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

    private List<DeletionDto> getDeletionByCursor(Cursor deletionCursor) {
        List<DeletionDto> deletions = new ArrayList<>();

        if (deletionCursor.moveToFirst()) {
            int trackNameColIndex = deletionCursor.getColumnIndex("track_name");
            int artistNameColIndex = deletionCursor.getColumnIndex("artist_name");
            int albumNameColIndex = deletionCursor.getColumnIndex("album_name");
            int fileNameColIndex = deletionCursor.getColumnIndex("file_name");

            do {
                DeletionDto deletionDto = new DeletionDto();
                deletionDto.setTrackName(deletionCursor.getString(trackNameColIndex));
                deletionDto.setAlbumName(deletionCursor.getString(artistNameColIndex));
                deletionDto.setAlbumName(deletionCursor.getString(albumNameColIndex));
                deletionDto.setFileName(deletionCursor.getString(fileNameColIndex));

                deletions.add(deletionDto);
            } while (deletionCursor.moveToNext());

        } else Log.d(TAG, "0 rows");

        return deletions;
    }

    public List<DeletionDto> getDeletions() {
        Log.d(TAG, "start getDeletions");
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        Cursor deletionCursor = db.query(DELETION_TABLE, null, null, null, null, null, null);

        List<DeletionDto> deletions = getDeletionByCursor(deletionCursor);

        deletionCursor.close();

        dbHelper.close();

        return deletions;
    }




}
