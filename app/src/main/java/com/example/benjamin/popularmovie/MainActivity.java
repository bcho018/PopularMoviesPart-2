package com.example.benjamin.popularmovie;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.example.benjamin.popularmovie.data.MovieContract;
import com.example.benjamin.popularmovie.data.MovieDBHelper;
import com.example.benjamin.popularmovie.data.TestUtil;
import com.example.benjamin.popularmovie.utilities.NetworkUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;


public class MainActivity extends AppCompatActivity implements AsyncResponse, GreenAdapter.ListItemClickListener,
        LoaderManager.LoaderCallbacks<Cursor> {

    //1d44bd6687107c4f8911b50d1bd5b29d
    private static final int NUM_LIST_ITEMS = 20;
    public String apiKey = "1d44bd6687107c4f8911b50d1bd5b29d";
    public String title;
    public String releaseDate;
    public String posterPath;
    public String id;
    public String voteAverage;
    public String overview;
    public String EXTRA_apiKey;
    private String movieDBResults;
    private RecyclerView mRecyclerViewImage;
    private String[] moviePathList = new String[50];
    private int totalMovies = 20;
    private GreenAdapter mAdapter;
    private String urlString;
    private Toast mToast;
    public Cursor movieCursor;
    String sortType;
    private SQLiteDatabase mDb;
    //posterPath2, title2, releaseDate2, voteAverage2, overview2;
    private String[] id2 = new String[200];
    private String[] posterPath2 = new String[200];
    private String[] title2 = new String[200];
    private String[] releaseDate2 = new String[200];
    private String[] voteAverage2 = new String[200];
    private String[] overview2 = new String[200];
    private int totalFavorite;
    boolean favoriteMovie;


    // Constants for logging and referring to a unique loader
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int MOVIE_LOADER_ID = 10;


    // some transient state for the activity instance
    private String SORT_TYPE;
    private String SAVED_LAYOUT_MANAGER;
    private Parcelable layoutManagerSavedState;

    JSONObject jsonObject = new JSONObject();
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        ImageView t = (ImageView) findViewById(R.id.tv_image);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        //Step 1: Set up the recyclerview first
        //Step 1.5: Check for internet connectivity
        //Step 2: Connect to movieDB and retrieve the data
        //Step 3: Populate recyclerview with data

        //Step 1
        mRecyclerViewImage = (RecyclerView) findViewById(R.id.rv_numbers);
        GridLayoutManager layoutManager = new GridLayoutManager(MainActivity.this, 2);
        mRecyclerViewImage.setLayoutManager(layoutManager);
        mRecyclerViewImage.setHasFixedSize(true);
        mAdapter = new GreenAdapter(this);
        mRecyclerViewImage.setAdapter(mAdapter);

        //To pass the apiKey via intent to the movie detail activity
        EXTRA_apiKey = apiKey;

        //Step 1.5
        boolean connected = NetworkUtils.isNetworkAvailable(MainActivity.this);

        //The if statement grabs the value that was saved from onSaveInstanceState to restore previous settings
        if (savedInstanceState != null) {
            sortType = savedInstanceState.getString("SORT_TYPE");
            layoutManagerSavedState = savedInstanceState.getParcelable(SAVED_LAYOUT_MANAGER);
        }


        if (connected == true) {
            //Step 2
            //default sort
            if (sortType == null) {
                sortType = "topRated";
                displayMovie();
            } else {
                displayMovie();
            }

        } else if(connected == false) {

            //Implement a toast
            if (mToast != null) {
                mToast.cancel();
            }
            String toastMessage = "The device is not connected to the internet";
            mToast = Toast.makeText(this, toastMessage, Toast.LENGTH_LONG);

            mToast.show();
        }



        //Initialize loader to load all the favorite movies into a list so that the detail page can be altered accordingly
        getSupportLoaderManager().restartLoader(MOVIE_LOADER_ID, null, this);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {

        super.onRestoreInstanceState(savedInstanceState);



    }
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {

        savedInstanceState.putString("SORT_TYPE", sortType);

        savedInstanceState.putParcelable(SAVED_LAYOUT_MANAGER, mRecyclerViewImage.getLayoutManager().onSaveInstanceState());

        // call superclass to save any view hierarchy
        super.onSaveInstanceState(savedInstanceState);

    }



    final private void testDB(View v) {

        // Create a DB helper (this will create the DB if run for the first time)
        MovieDBHelper dbHelper = new MovieDBHelper(this);

        mDb = dbHelper.getWritableDatabase();
        TestUtil.insertFakeData(mDb);
        Cursor cursor = getAllMovies();

        int i = 0;
        String array[] = new String[cursor.getCount()];
        String test;

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            array[i] = cursor.getString(2); // This retrieves the movie title
            i++;
            cursor.moveToNext();
        }

    }


    private Cursor getAllMovies() {
        // COMPLETED (6) Inside, call query on mDb passing in the table name and projection String [] order by COLUMN_TIMESTAMP
        return mDb.query(
                MovieContract.MovieEntry.TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                MovieContract.MovieEntry.COLUMN_MOVIE_ID
        );
    }
    private void displayMovie() {

        //String testString="https://api.themoviedb.org/3/movie/550?api_key=1d44bd6687107c4f8911b50d1bd5b29d";
        String topRated = "https://api.themoviedb.org/3/movie/top_rated?api_key=" + apiKey + "&language=en-US&page=1\n";
        String mostPopular = "https://api.themoviedb.org/3/movie/popular?api_key=" + apiKey + "&language=en-US&page=1";

        if(sortType == "mostPopular") {
            urlString=mostPopular;
        } else if (sortType=="topRated") {
            urlString=topRated;
        } else if (sortType =="favoriteMovies") {

            //Steps to load the favoriteMovies stored in the database
            //Step 1: Query the content of the table: "movieList"
            //Step 2: Send the list of movies to the recycler view which will call picaso (Similar to processFinish method)

            //Step 1:
            getSupportLoaderManager().restartLoader(MOVIE_LOADER_ID, null, this);

            return;
        }

        try {
            MovieDBQueryTask asyncTask = new MovieDBQueryTask();
            asyncTask.delegate = this;
            URL url = new URL(urlString);
            URL movieDBQuery = url;
            asyncTask.execute(movieDBQuery);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.mostPopularMovies:
                sortType = "mostPopular";
                displayMovie();
                item.setChecked(true);
                return true;
            case R.id.mostTopRatedMovies:
                sortType = "topRated";
                displayMovie();
                item.setChecked(true);
                return true;
            case R.id.favoriteMovies:
                sortType = "favoriteMovies";
                displayMovie();
                item.setChecked(true);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }
    @Override
    public void onListItemClick(int clickedItemIndex) {

        //If the favorite movies is selected than read the data from the list instead

        ToggleButton toggle = (ToggleButton) findViewById(R.id.toggleButton);

        //If favorite movies are not selected
        if (sortType != "favoriteMovies") {
            //Extract the required movie info from the JSON movie data
            try {
                jsonObject = new JSONObject(movieDBResults);
                JSONArray movies = jsonObject.getJSONArray("results");
                JSONObject movie = movies.getJSONObject(clickedItemIndex);

                id = movie.getString("id");
                posterPath = movie.getString("poster_path");
                title = movie.getString("title");
                releaseDate = movie.getString("release_date");
                voteAverage = movie.getString("vote_average");
                overview = movie.getString("overview");

            } catch (JSONException e) {
                e.printStackTrace();
            }

        } else {
            //Retrieve the values from the lists

            id = id2[clickedItemIndex];
            posterPath = posterPath2[clickedItemIndex];
            title = title2[clickedItemIndex];
            releaseDate = releaseDate2[clickedItemIndex];
            voteAverage = voteAverage2[clickedItemIndex];
            overview = overview2[clickedItemIndex];

        }

        //Check if the movie is the user's favorite
        favoriteMovie=false;

        for(int i=0; i<=totalFavorite; i++){
            if (title.equals(title2[i])) {
                favoriteMovie=true;
            }
        }

        //Open the movie detail page when called
        Intent intent = new Intent(MainActivity.this, MovieDetails.class);

        intent.putExtra("EXTRA_id", id);
        intent.putExtra("EXTRA_posterPath", posterPath);
        intent.putExtra("EXTRA_title",title);
        intent.putExtra("EXTRA_releaseDate",releaseDate);
        intent.putExtra("EXTRA_voteAverage",voteAverage);
        intent.putExtra("EXTRA_overview",overview);
        intent.putExtra("EXTRA_apiKey",apiKey);
        intent.putExtra("EXTRA_favoriteMovie",favoriteMovie);


        startActivity(intent);

    }

    //When AsyncTask finish its work the thread starts here. String output is passed from asyncTask
    @Override
    public void processFinish(String output){
        movieDBResults = output;

        if (movieDBResults == null) {
            //Implement a toast
            if (mToast != null) {
                mToast.cancel();
            }
            String toastMessage = "The device is not connected to the internet";
            mToast = Toast.makeText(this, toastMessage, Toast.LENGTH_LONG);

            mToast.show();

        } else {
            //Step 3
            //Read Json string and store movie path into moviePathList array

            try {
                jsonObject = new JSONObject(movieDBResults);
                JSONArray movies = jsonObject.getJSONArray("results");

                for (int i = 0; i < totalMovies; i++) {
                    JSONObject movie = movies.getJSONObject(i);
                    posterPath = movie.getString("poster_path");
                    moviePathList[i] = posterPath;
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
            //Make sure the RecyclerView is visible
            mRecyclerViewImage.setVisibility(View.VISIBLE);
            //Send moviePathList to the RecycleView
            mAdapter.setWeatherData(moviePathList);
        }

        //Return to the original scrollview
        if (layoutManagerSavedState != null) {
            mRecyclerViewImage.getLayoutManager().onRestoreInstanceState(layoutManagerSavedState);
        }

    }



    private class MovieDBQueryTask extends AsyncTask<URL, Void, String> {
        public AsyncResponse delegate = null;

        @Override
        protected String doInBackground(URL... params) {
            String poster_path="";
            URL searchUrl = params[0];
            String movieDBSearchResults = null;
            try {
                movieDBSearchResults = NetworkUtils.getResponseFromHttpUrl(searchUrl);
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (movieDBSearchResults != null && !movieDBSearchResults.equals("")) {
                movieDBResults = movieDBSearchResults;

            }
            return movieDBSearchResults;
        }

        @Override
        protected void onPostExecute(String movieDBSearchResults) {
            delegate.processFinish(movieDBSearchResults);
        }
    }

    //###################################################################
    //This method should query the data and then display the result in the recycler view




    @Override
    public void onResume() {
        super.onResume();
        // re-queries for all tasks
        //getSupportLoaderManager().restartLoader(MOVIE_LOADER_ID, null, this);
    }

    /**
     * Instantiates and returns a new AsyncTaskLoader with the given ID.
     * This loader will return task data as a Cursor or null if an error occurs.
     *
     * Implements the required callbacks to take care of loading data at all stages of loading.
     */
    @Override
    public Loader<Cursor> onCreateLoader(int id, final Bundle loaderArgs) {

        return new AsyncTaskLoader<Cursor>(this) {

            // Initialize a Cursor, this will hold all the movie data
            Cursor mTaskData = null;

            // onStartLoading() is called when a loader first starts loading data
            @Override
            protected void onStartLoading() {
                forceLoad();
                if (mTaskData != null) {
                    // Delivers any previously loaded data immediately
                    deliverResult(mTaskData);
                } else {
                    // Force a new load
                    forceLoad();
                }
            }

            // loadInBackground() performs asynchronous loading of data
            @Override
            public Cursor loadInBackground() {
                // Query all the movie in the database

                try {
                    return getContentResolver().query(MovieContract.MovieEntry.CONTENT_URI,
                            null,
                            null,
                            null,
                            MovieContract.MovieEntry.COLUMN_MOVIE_ID);

                } catch (Exception e) {
                    Log.e(TAG, "Failed to asynchronously load data.");
                    e.printStackTrace();
                    return null;
                }


            }

            // deliverResult sends the result of the load, a Cursor, to the registered listener
            public void deliverResult(Cursor data) {
                mTaskData = data;
                super.deliverResult(data);
            }
        };

    }

    /**
     * Called when a previously created loader has finished its load.
     *
     * @param loader The Loader that has finished.
     * @param data The data generated by the Loader.
     */
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        //Pass the posterPath data from the cursor into the Recycler View adapter
        //Step 1: extract a list of movie poster URL from the cursor
        //Step 2: Sent the list to the recycler view
        movieCursor = data;

        //Load the movie
        //Clear the array
        for (int k=0; k<200; k++) {
            id2[k] = null;
            posterPath2[k] = null;
            title2[k] = null;
            releaseDate2[k] = null;
            voteAverage2[k] = null;
            overview2[k] = null;
        }
        int i =0;
        //cursor is not empty
        if ((movieCursor != null)) {

            int count = movieCursor.getCount();
            if (movieCursor.moveToFirst()) {
                do {

                    if (data.getString(data.getColumnIndex("posterPath")) != null) {
                        id2[i] = data.getString(data.getColumnIndex("id"));
                        posterPath2[i] = data.getString(data.getColumnIndex("posterPath"));
                        title2[i] = data.getString(data.getColumnIndex("movieTitle"));
                        releaseDate2[i] = data.getString(data.getColumnIndex("releaseDate"));
                        voteAverage2[i] = data.getString(data.getColumnIndex("voteAverage"));
                        overview2[i] = data.getString(data.getColumnIndex("overview"));

                        i = i + 1;
                    }
                } while (data.moveToNext());
            }
            totalFavorite = count;
        }
        //##########################################################

        // Update the data that the adapter uses to create ViewHolders


        if (sortType == "favoriteMovies") {


            //Make sure the RecyclerView is visible
            mRecyclerViewImage.setVisibility(View.VISIBLE);
            //Send moviePathList to the RecycleView
            mAdapter.setWeatherData(posterPath2);

        }

        if (data != null) {data.close();}

        if (movieCursor != null) {movieCursor.close();}

    }


    /**
     * Called when a previously created loader is being reset, and thus
     * making its data unavailable.
     * onLoaderReset removes any references this activity had to the loader's data.
     *
     * @param loader The Loader that is being reset.
     */
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }


    //###############################################################################################

}

