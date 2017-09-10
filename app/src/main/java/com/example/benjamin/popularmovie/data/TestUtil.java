package com.example.benjamin.popularmovie.data;

import android.content.ContentValues;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

public class TestUtil {


    public static void insertFakeData(SQLiteDatabase db){
        if(db == null){
            return;
        }
        //create a list of fake movie id and key
        List<ContentValues> list = new ArrayList<ContentValues>();

        ContentValues cv = new ContentValues();
        cv.put(MovieContract.MovieEntry.COLUMN_MOVIE_ID, "ABC123");
        cv.put(MovieContract.MovieEntry.COLUMN_MOVIE_TITLE, "TitleABC123");
        list.add(cv);

        cv = new ContentValues();

        cv.put(MovieContract.MovieEntry.COLUMN_MOVIE_ID, "32523523");
        cv.put(MovieContract.MovieEntry.COLUMN_MOVIE_TITLE, "Very good and nice");
        list.add(cv);

        cv = new ContentValues();
        cv.put(MovieContract.MovieEntry.COLUMN_MOVIE_ID, "32523523");
        cv.put(MovieContract.MovieEntry.COLUMN_MOVIE_TITLE, "Very good and nice");
        list.add(cv);

        try {
            db.beginTransaction();
            //Clear the db table
            db.delete(MovieContract.MovieEntry.TABLE_NAME,null,null);
            //Go through the list and a dd item 1 by 1 with a loop
            for(ContentValues c:list) {
                db.insert(MovieContract.MovieEntry.TABLE_NAME,null,c);
            }
            db.setTransactionSuccessful();

        }
        catch (SQLException e) {
            //Too bad hahaha ={
        }


    }


}
