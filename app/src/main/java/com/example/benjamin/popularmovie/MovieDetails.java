package com.example.benjamin.popularmovie;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.example.benjamin.popularmovie.data.MovieContract;
import com.example.benjamin.popularmovie.utilities.NetworkUtils;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;



public class MovieDetails extends AppCompatActivity implements AsyncResponse1 {
    public String key, reviewsList;
    public String trailerUrl, test;
    public String[] urlList = new String[2];
    public String[] content = new String[1000];
    public String[] author = new String[1000];
    public TextView reviewTextView;
    public int totalMovieReviews;
    public String reviewsContent;
    private String movieDBResults, movieDbReviewResults, trailerUrlString, reviewsUrlString;
    private String id, title, posterPath,releaseDate,voteAverage,overview;
    private boolean favoriteMovie;
    private static final int MOVIE_LOADER_ID = 0;
    public MainActivity mainActivity;
    public String posterPath2;

    JSONObject jsonObject = new JSONObject();
    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_details);

        MainActivity mainActivity = new MainActivity();

        String b = mainActivity.posterPath;

        // Get the Intent that started this activity and extract the string
        Bundle resultIntent = getIntent().getExtras();

        posterPath = resultIntent.getString("EXTRA_posterPath");
        title = resultIntent.getString("EXTRA_title");
        releaseDate = resultIntent.getString("EXTRA_releaseDate");
        voteAverage = resultIntent.getString("EXTRA_voteAverage");
        overview = resultIntent.getString("EXTRA_overview");
        String apiKey = resultIntent.getString("EXTRA_apiKey");
        id = resultIntent.getString("EXTRA_id");

        favoriteMovie = resultIntent.getBoolean("EXTRA_favoriteMovie");

        //From the id of the movie retrieve the youtube trailer link with Async task

        String apiKey1 = mainActivity.apiKey;
        trailerUrlString =  "http://api.themoviedb.org/3/movie/" + id + "/videos?api_key=" + apiKey1;
        reviewsUrlString = "http://api.themoviedb.org/3/movie/" + id + "/reviews?api_key=" + apiKey1;

        urlList[0]=trailerUrlString;
        urlList[1]=reviewsUrlString;
        try {
            //Retrieve trailer key for youtube
            QueryVideo asyncTask = new QueryVideo();
            asyncTask.delegate = this;
            URL url = new URL(trailerUrlString);
            URL url1 = new URL(reviewsUrlString);
            URL movieDBQuery1 = url;
            URL movieDBQuery2 = url1;

            URL[] movieDBQuery = new URL[2];
            movieDBQuery[0] = movieDBQuery1;
            movieDBQuery[1] = movieDBQuery2;
            asyncTask.execute(movieDBQuery);

        } catch (IOException e) {
            e.printStackTrace();
        }

        //Assign the TextView their variable names
        TextView titleTextView = (TextView) findViewById(R.id.title);
        TextView releaseDateTextView = (TextView) findViewById(R.id.releaseDate);
        TextView voteAverageTextView = (TextView) findViewById(R.id.voteAverage);
        TextView overviewTextView = (TextView) findViewById(R.id.overview);
        reviewTextView = (TextView) findViewById(R.id.reviews);

        //Assign value to the TextView(s) & ImageView
        posterPath2 = "https://image.tmdb.org/t/p/w300/" + posterPath;
        ImageView moviePosterImageView = (ImageView) findViewById(R.id.movie_poster);
        Context context = MovieDetails.this;
        Picasso.with(context)
                .load(posterPath2)
                .into(moviePosterImageView);

        titleTextView.setText("Title : " + title);
        releaseDateTextView.setText("Release Date : " + releaseDate);
        voteAverageTextView.setText("Average Vote : " + voteAverage);
        overviewTextView.setText("Overview : " + overview);

        //Check if the movie is a favourite movie and then alter the toggle button accordingly
        ToggleButton favoriteToggle = (ToggleButton) findViewById(R.id.toggleButton);


        if (favoriteMovie == true) {
            favoriteToggle.setChecked(true);
        } else {
            favoriteToggle.setChecked(false);
        }

    }

    public void favouriteToggle(View v) {

        MainActivity mainActivity = new MainActivity();

        //false: movie stored as favourite
        //true: movie stored as not favourite
        ToggleButton toggleButton = (ToggleButton) findViewById(R.id.toggleButton);
        Boolean ToggleButtonState = toggleButton.isChecked();


        if (ToggleButtonState){
            //Store the movie as favorite
            // Create new empty ContentValues object
            ContentValues contentValues = new ContentValues();
            //Store data into the contentValues object
            contentValues.put(MovieContract.MovieEntry.COLUMN_MOVIE_ID, id);
            contentValues.put(MovieContract.MovieEntry.COLUMN_MOVIE_TITLE, title);
            contentValues.put(MovieContract.MovieEntry.COLUMN_RELEASE_DATE, releaseDate);
            contentValues.put(MovieContract.MovieEntry.COLUMN_VOTE_AVERAGE, voteAverage);
            contentValues.put(MovieContract.MovieEntry.COLUMN_OVERVIEW, overview);
            contentValues.put(MovieContract.MovieEntry.COLUMN_POSTER_PATH, posterPath);



            //SO FAR YOU ARE TRYNG TO ADD SOME MORE COLUMNS INTO YOU EXISTING DATABASE

            // Insert the content values via a ContentResolver
            Uri uri = getContentResolver().insert(MovieContract.MovieEntry.CONTENT_URI, contentValues);

            if(uri != null) {
                Toast.makeText(getBaseContext(), uri.toString(), Toast.LENGTH_LONG).show();
            }

        } else {
            //Remove the movie as favorite from the DB

            //Build appropriate uri
            Uri uri = MovieContract.MovieEntry.CONTENT_URI;
            uri = uri.buildUpon().appendPath(title).build();
            getContentResolver().delete(uri,null,null);

        }

    }

    public void launchTrailer(View view) {
        //Step 1: Read key from JSON string
        //Step 2: Append it to youtube address and display it in XML movie details page

        //Step 1:
        try {
            jsonObject = new JSONObject(movieDBResults);
            JSONArray movies = jsonObject.getJSONArray("results");
            JSONObject movie = movies.getJSONObject(0);

            key = movie.getString("key");

        } catch (JSONException e) {
            e.printStackTrace();
        }

        trailerUrl = "https://www.youtube.com/watch?v=" + key;

        //Use an intent to open the browser with the URL
        Intent intent= new Intent(Intent.ACTION_VIEW,Uri.parse(trailerUrl));
        startActivity(intent);
    }

    public void processFinish1(String output){
        //Populate the reviews section in this method
        String as = movieDbReviewResults;

        //Read JSON string
        try {
            jsonObject = new JSONObject(movieDbReviewResults);
            JSONArray movieReviews = jsonObject.getJSONArray("results");
            totalMovieReviews = jsonObject.getInt("total_results");

            for (int i = 0; i < totalMovieReviews; i++) {
                JSONObject movie = movieReviews.getJSONObject(i);
                content[i] = movie.getString("content");
                author[i] = movie.getString("author");
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        //If there are any reviews than populate the review section
        if (content[0] != null && author[0] != null){

            for (int i = 0; i < totalMovieReviews; i++) {
                reviewsContent = reviewsContent + author[i];
                //Line break
                reviewsContent = reviewsContent + "\n";
                reviewsContent = reviewsContent + content[i];
                //Line break
                reviewsContent = reviewsContent + "\n \n \n";
            }
            reviewTextView.setText(reviewsContent);
        }
    }
    private class QueryVideo extends AsyncTask<URL, Void, String> {
        public AsyncResponse1 delegate = null;

        @Override
        protected String doInBackground(URL... params) {
            URL videoUrl = params[0];
            URL reviewsUrl = params[1];

            //Step 1: Retrieve movie trailer key
            //Step 2: Retrieve movie reviews
            String[] movieDBSearchResults = new String[2];

            try {
                movieDBSearchResults[0] = NetworkUtils.getResponseFromHttpUrl(videoUrl);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (movieDBSearchResults[0] != null && !movieDBSearchResults[0].equals("")) {
                movieDBResults = movieDBSearchResults[0];
            }

            //Step 2: Retrieve movie reviews
            try {
                movieDBSearchResults[1] = NetworkUtils.getResponseFromHttpUrl(reviewsUrl);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (movieDBSearchResults[1] != null && !movieDBSearchResults[1].equals("")) {
                movieDbReviewResults = movieDBSearchResults[1];
            }

            return movieDBSearchResults[1];
        }
        @Override
        protected void onPostExecute(String movieDBSearchResults) {
            delegate.processFinish1(movieDBSearchResults);
        }

    }
}