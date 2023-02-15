package com.mk.m_folder.data.service;

import android.app.Service;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import com.mk.m_folder.data.DBHelper;
import com.mk.m_folder.data.entity.Deletion;
import com.mk.m_folder.data.entity.Track;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BaseService extends Service {

    private final IBinder mBinder = new LocalBinder();

    DBHelper dbHelper;

    private static final String BASE_NAME = "my_music.db";

    private static final String MUSIC_PATH_COLUMN = "music_path";

    private static final String TRACK_TABLE = "track";
    private static final String DELETION_TABLE = "deletion";
    private static final String SETTING_TABLE = "setting";

    private static final String TRACK_ID_COLUMN = "id";
    private static final String TRACK_NAME_COLUMN = "track_name";
    private static final String ARTIST_NAME_COLUMN = "artist_name";
    private static final String ALBUM_NAME_COLUMN = "album_name";

    private static final String FILE_PATH_COLUMN = "file_path";
    private static final String FILE_NAME_COLUMN = "file_name";

    List<String> settings = Arrays.asList("/storage/5E08-92B8/Music2");

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

    // Track
    public Track getTrackByFilePath(String filePath) {
        Log.d(TAG, "start getTrackByFilePath " + filePath);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        Cursor trackCursor = db.rawQuery("SELECT * FROM " + TRACK_TABLE + " WHERE file_path = " + filePath, null);

        List<Track> tracks = getTracksByCursor(trackCursor);
        if(tracks.size() > 0) return tracks.get(0);
        return null;
    }

    public void insertTrack(String trackName, String artistName, String albumName, String filePath) {
        Log.d(TAG, "start insertTrack..");
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues cv = new ContentValues();

        cv.put(TRACK_NAME_COLUMN, trackName);
        cv.put(ARTIST_NAME_COLUMN, artistName);
        cv.put(ALBUM_NAME_COLUMN, albumName);
        cv.put(FILE_PATH_COLUMN, filePath);

        long rowID = db.insert(TRACK_TABLE, null, cv);

        Log.d(TAG, "Track row inserted, ID = " + rowID);

        dbHelper.close();
    }

    private int[] getColumnIndexes(Cursor trackCursor) {
        return new int[]{
                trackCursor.getColumnIndex(TRACK_ID_COLUMN),
                trackCursor.getColumnIndex(TRACK_NAME_COLUMN),
                trackCursor.getColumnIndex(ARTIST_NAME_COLUMN),
                trackCursor.getColumnIndex(ALBUM_NAME_COLUMN),
                trackCursor.getColumnIndex(FILE_PATH_COLUMN)};
    }

    private Track getTrackFromCursor(Cursor trackCursor) {
        int[] columnIndexes = getColumnIndexes(trackCursor);

        return new Track(
                trackCursor.getString(columnIndexes[1]),
                trackCursor.getString(columnIndexes[2]),
                trackCursor.getString(columnIndexes[3]),
                trackCursor.getString(columnIndexes[4])
        );
    }

    private List<Track> getTracksByCursor(Cursor trackCursor) {
        List<Track> tracks = new ArrayList<>();

        if (trackCursor.moveToFirst()) {
            do {
                tracks.add(getTrackFromCursor(trackCursor));
            } while (trackCursor.moveToNext());

        } else Log.d(TAG, "0 track rows");

        return tracks;
    }

    public List<Track> getTracks() {
        Log.d(TAG, "start getTracks");
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        Cursor trackCursor = db.query(TRACK_TABLE, null, null, null, null, null, null);

        List<Track> tracks = getTracksByCursor(trackCursor);

        trackCursor.close();

        dbHelper.close();

        return tracks;
    }

    public int clearTracks() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        int delCount = db.delete(TRACK_TABLE, "1", null);
        Log.d(TAG, "deleted tracks rows count = " + delCount);

        dbHelper.close();

        return delCount;
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

    public int clearDeletions() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        int delCount = db.delete(DELETION_TABLE, "1", null);
        Log.d(TAG, "deleted deletions rows count = " + delCount);

        dbHelper.close();

        return delCount;
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
            saveSettings(this.settings.get(0));
            Log.d(TAG, "Settings: 0 rows");
            return this.settings;
        }
        settingCursor.close();

        dbHelper.close();
        return Arrays.asList(settings);
    }

    // export
    public void exportDatabase() {
        try {
            File sd = Environment.getExternalStorageDirectory();
            if (sd.canWrite()) {
                File currentDB = new File("/data/data/" + getPackageName() +"/databases/", BASE_NAME);

                // todo min level
                String dateDbName = LocalDate.now() + "_" + BASE_NAME;
                File backupDB = new File(sd.toString() + "/Download/", dateDbName);

                if (currentDB.exists()) {
                    FileChannel src = new FileInputStream(currentDB).getChannel();
                    FileChannel dst = new FileOutputStream(backupDB).getChannel();
                    dst.transferFrom(src, 0, src.size());
                    src.close();
                    dst.close();
                    Log.d(TAG, "database was exported successfully");
                }
            } else {
                Log.d(TAG, "нет доступа к памяти телефона");
            }
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
    }
}
