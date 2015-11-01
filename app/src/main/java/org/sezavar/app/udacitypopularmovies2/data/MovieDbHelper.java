package org.sezavar.app.udacitypopularmovies2.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import org.sezavar.app.udacitypopularmovies2.data.MovieContract.*;

/**
 * Created by amir on 10/16/15.
 */
public class MovieDbHelper extends SQLiteOpenHelper {
    static final String DATA_BASE_NAME = "movie.db";
    private static final int DATABASE_VERSION = 1;

    public MovieDbHelper(Context context) {
        super(context, DATA_BASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        final String SQL_CREATE_MOVIE_TABLE = "CREATE TABLE " + MovieEntry.TABLE_NAME + " (" +
                MovieEntry._ID + " INTEGER PRIMARY KEY, " +
                MovieEntry.COLUMN_MDB_ID + " INTEGER UNIQUE NOT NULL, " +
                MovieEntry.COLUMN_TITLE + " TEXT NOT NULL, " +
                MovieEntry.COLUMN_POSTER_PATH + " TEXT, " +
                MovieEntry.COLUMN_OVERVIEW + " TEXT, " +
                MovieEntry.COLUMN_USER_RATING + " REAL NOT NULL, " +
                MovieEntry.COLUMN_RELEASE_DATE + " INTEGER NOT NULL, " +
                MovieEntry.COLUMN_POPULARITY + " REAL NOT NULL," +
                MovieEntry.COLUMN_FAVORITE + " NUMERIC NOT NULL," +
                " UNIQUE ("+MovieEntry.COLUMN_MDB_ID+") ON CONFLICT REPLACE"+
                ");";
        db.execSQL(SQL_CREATE_MOVIE_TABLE);

        final String SQL_CREATE_TRAILER_TABLE = "CREATE TABLE " + TrailerEntry.TABLE_NAME + " (" +
                TrailerEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                TrailerEntry.COLUMN_MOVIE_ID + " INTEGER NOT NULL, " +
                TrailerEntry.COLUMN_TRAILER_PATH + " TEXT NOT NULL, " +
                " FOREIGN KEY (" + TrailerEntry.COLUMN_MOVIE_ID + ") REFERENCES " +
                MovieEntry.TABLE_NAME + " (" + MovieEntry._ID + "), " +
                " UNIQUE (" + TrailerEntry.COLUMN_MOVIE_ID + ", " +
                TrailerEntry.COLUMN_TRAILER_PATH + ") ON CONFLICT REPLACE);";
        db.execSQL(SQL_CREATE_TRAILER_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + MovieEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + TrailerEntry.TABLE_NAME);
        onCreate(db);
    }
}
