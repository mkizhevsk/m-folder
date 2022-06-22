package com.mk.m_folder.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;
import android.util.Log;

public class DBHelper extends SQLiteOpenHelper {

    private static final String TAG = "MainActivity";

    public DBHelper(Context context) {
        super(context, "my_music.db", null, 1);
    }

    private static final String TABLE_DELETION =
            "create table if not exists contact ("
                    + "id integer primary key autoincrement, "
                    + "track_name text, "
                    + "artist_name text, "
                    + "album_name text, "
                    + "file_name text" + ");";

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(TAG, "--- onCreate database ---");

        // создаем таблицу, если ее нет
        db.execSQL(TABLE_DELETION);
        Log.d(TAG, "--- onCreate database finish ---");
    }

    @Override
    public  void onOpen(SQLiteDatabase database) {
        Log.d(TAG, "--- onOpen database ---");
//        database.execSQL(TABLE_CARD);

        super.onOpen((database));
        if(Build.VERSION.SDK_INT >= 28) {
            database.disableWriteAheadLogging();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
