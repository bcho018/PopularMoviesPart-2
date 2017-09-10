package com.example.benjamin.popularmovie.data;

import android.net.Uri;
import android.provider.BaseColumns;

//Database contract
public class MovieContract {

    //Define the authority
    public static final String AUTHORITY = "com.example.benjamin.popularmovie";
    //Define the base content URI which is "content:// + <authority>"
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + AUTHORITY);
    //Define the path directory which in this case is the table name
    public static final String PATH_TASKS = "movieList";

    //MovieEntry defines the content of the movieList table
    public static final class MovieEntry implements BaseColumns {

        //TaskEntry content URI = base content URI + path
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_TASKS).build();

        //These static variables corresponds to the table name and each db columns
        public static final String TABLE_NAME = "movieList";
        public static final String COLUMN_MOVIE_ID = "id";
        public static final String COLUMN_MOVIE_TITLE = "movieTitle";
        public static final String COLUMN_RELEASE_DATE = "releaseDate";
        public static final String COLUMN_VOTE_AVERAGE = "voteAverage";
        public static final String COLUMN_OVERVIEW = "overview";
        public static final String COLUMN_POSTER_PATH = "posterPath";

    }

}
