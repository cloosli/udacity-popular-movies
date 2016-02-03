package com.loosli.christian.popularmovieapp.android.app.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.loosli.christian.popularmovieapp.android.app.data.MovieContract.MovieEntry;

/**
 * Created by ChristianL on 01.02.16.
 */
public class MovieDbHelper extends SQLiteOpenHelper {

    // If you change the database schema, you must increment the database version.
    private static final int DATABASE_VERSION = 2;

    static final String DATABASE_NAME = "movie.db";

    public MovieDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        final String SQL_CREATE_WEATHER_TABLE = "CREATE TABLE " + MovieEntry.TABLE_NAME + " (" +
                MovieEntry._ID + " INTEGER PRIMARY KEY, " +
                MovieEntry.COLUMN_TITLE + " TEXT NOT NULL, " +
                MovieEntry.COLUMN_OVERVIEW + " TEXT NOT NULL, " +
                MovieEntry.COLUMN_POSTERPATH + " TEXT NOT NULL, " +
                MovieEntry.COLUMN_BACKDROPPATH + " TEXT NOT NULL, " +
                MovieEntry.COLUMN_RATING + " REAL NOT NULL, " +
                MovieEntry.COLUMN_RELEASEDATE + " INTEGER NOT NULL " +
                ");";

        db.execSQL(SQL_CREATE_WEATHER_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + MovieEntry.TABLE_NAME);
        onCreate(db);
    }

}
