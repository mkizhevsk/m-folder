package com.mk.m_folder.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;
import android.util.Log;

public class DBHelper extends SQLiteOpenHelper {

    private static final String BASE_NAME = "my_music.db";
    private static final String TAG = "MainActivity";

    public DBHelper(Context context) {
        super(context, BASE_NAME, null, 1);
    }

    private static final String TABLE_TRACK =
            "create table if not exists track ("
                    + "id integer primary key autoincrement, "
                    + "track_name text, "
                    + "artist_name text, "
                    + "album_name text, "
                    + "file_path text" + ");";

    private static final String TABLE_DELETION =
            "create table if not exists deletion ("
                    + "id integer primary key autoincrement, "
                    + "track_name text, "
                    + "artist_name text, "
                    + "album_name text, "
                    + "file_name text" + ");";

    private static final String TABLE_SETTING =
            "create table if not exists setting ("
                    + "id integer primary key autoincrement, "
                    + "music_path text" + ");";

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(TAG, "--- onCreate database ---");

        // создаем таблицу, если ее нет
        db.execSQL(TABLE_TRACK);
        db.execSQL(TABLE_DELETION);
        db.execSQL(TABLE_SETTING);
        Log.d(TAG, "--- onCreate database finish ---");
    }

    @Override
    public  void onOpen(SQLiteDatabase database) {
        Log.d(TAG, "--- onOpen database ---");

        super.onOpen((database));
        if(Build.VERSION.SDK_INT >= 28) {
            database.disableWriteAheadLogging();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}