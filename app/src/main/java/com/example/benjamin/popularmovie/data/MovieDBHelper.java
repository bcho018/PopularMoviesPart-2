package com.example.benjamin.popularmovie.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.example.benjamin.popularmovie.data.MovieContract.*;


//Purpose of the helper class is to initiate and modify the database
public class MovieDBHelper extends SQLiteOpenHelper {

    //Variable that holds the database name
    private static final String DATABASE_NAME = "movieList.db";

    //Variable that holds the Database version
    private static final int DATABASE_VERSION = 2;

    public MovieDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        final String SQL_CREATE_MOVIELIST_TABLE = "CREATE TABLE " + MovieEntry.TABLE_NAME + " (" +
                MovieEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                MovieEntry.COLUMN_MOVIE_ID + " TEXT NOT NULL, " +
                MovieEntry.COLUMN_MOVIE_TITLE + " TEXT NOT NULL," +

                MovieEntry.COLUMN_RELEASE_DATE + " TEXT NOT NULL," +
                MovieEntry.COLUMN_VOTE_AVERAGE + " TEXT NOT NULL," +
                MovieEntry.COLUMN_OVERVIEW + " TEXT NOT NULL," +
                MovieEntry.COLUMN_POSTER_PATH + " TEXT NOT NULL" +

                "); ";

        sqLiteDatabase.execSQL(SQL_CREATE_MOVIELIST_TABLE);

    }


    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

        // If you need to add a column
        if (i1 > i) {
            sqLiteDatabase.execSQL("ALTER TABLE " + MovieEntry.TABLE_NAME + " ADD COLUMN " + MovieEntry.COLUMN_RELEASE_DATE + " TEXT");
            sqLiteDatabase.execSQL("ALTER TABLE " + MovieEntry.TABLE_NAME + " ADD COLUMN " + MovieEntry.COLUMN_VOTE_AVERAGE + " TEXT");
            sqLiteDatabase.execSQL("ALTER TABLE " + MovieEntry.TABLE_NAME + " ADD COLUMN " + MovieEntry.COLUMN_OVERVIEW + " TEXT");
            sqLiteDatabase.execSQL("ALTER TABLE " + MovieEntry.TABLE_NAME + " ADD COLUMN " + MovieEntry.COLUMN_POSTER_PATH + " TEXT");
        }
    }


}
