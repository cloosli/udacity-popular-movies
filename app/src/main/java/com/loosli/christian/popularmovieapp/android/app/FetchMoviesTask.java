package com.loosli.christian.popularmovieapp.android.app;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.widget.Toast;

import com.loosli.christian.popularmovieapp.android.app.entity.Movie;
import com.loosli.christian.popularmovieapp.android.app.util.Util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import butterknife.ButterKnife;

/**
 * Created by ChristianL on 27.01.16.
 */
public class FetchMoviesTask extends AsyncTask<String, Void, ArrayList<Movie>> {
    private final String LOG_TAG = FetchMoviesTask.class.getSimpleName();

    private Context mContext;
    private MoviesAdapter mMoviesAdapter;

    public FetchMoviesTask(Context context) {
        mContext = context;
    }

    public FetchMoviesTask(Context context, MoviesAdapter moviesAdapter) {
        mContext = context;
        mMoviesAdapter = moviesAdapter;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        ((Activity)mContext).findViewById(R.id.main_swipe_refresh_layout);
//        mProgressBar.setVisibility(View.VISIBLE);
    }

    /**
     * Take the String representing the complete movies in JSON Format and
     * pull out the data we need to construct the Strings needed for the wireframes.
     * <p>
     * Fortunately parsing is easy:  constructor takes the JSON string and converts it
     * into an Object hierarchy for us.
     * </p>
     */
    private ArrayList<Movie> getMoviesPosterDataFromJson(String moviesJsonStr)
            throws JSONException {

        // These are the names of the JSON objects that need to be extracted.
        final String MDB_RESULTS = "results";
//            final String MDB_PAGE = "page";
        final String MDB_TOTAL_PAGES = "total_pages";
        final String MDB_ID = "id";
        final String MDB_TITLE = "original_title";
        final String MDB_DESCRIPTION = "overview";
        final String MDB_POSTER_PATH = "poster_path";
        final String MDB_BACKDROP_PATH = "backdrop_path";
        final String MDB_RELEASE_DATE = "release_date";
        final String MDB_RATING = "vote_average";
        final String MDB_POPULARITY = "popularity";

        JSONObject moviesJson = new JSONObject(moviesJsonStr);
        int totalPages = moviesJson.getInt(MDB_TOTAL_PAGES);
//        mTotalPageNumber = totalPages < mTotalPageNumber ? totalPages : mTotalPageNumber;
        JSONArray moviesArray = moviesJson.getJSONArray(MDB_RESULTS);
        final int moviesArraySize = moviesArray.length();
        final ArrayList<Movie> movies = new ArrayList<>(moviesArraySize);
        for (int i = 0; i < moviesArraySize; i++) {

            // Get the JSON object representing the movie
            JSONObject movieJson = moviesArray.getJSONObject(i);
            Movie movie = new Movie();
            movie.setId(movieJson.getLong(MDB_ID));
            movie.setTitle(movieJson.getString(MDB_TITLE));
            movie.setOverview(movieJson.getString(MDB_DESCRIPTION));
            movie.setPosterPath(movieJson.getString(MDB_POSTER_PATH));
            movie.setBackdropPath(movieJson.getString(MDB_BACKDROP_PATH));
            movie.setRating((float) movieJson.getDouble(MDB_RATING));
            Date releaseDate = null;
            String date = movieJson.getString(MDB_RELEASE_DATE);
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
            try {
                releaseDate = formatter.parse(date);
            } catch (ParseException e) {
                Log.e(LOG_TAG, "Parsing failed: " + Log.getStackTraceString(e));
                releaseDate = new Date();
            }
            movie.setReleaseDate(releaseDate);

            if (BuildConfig.DEBUG) {
                Log.v(LOG_TAG, "MDB_POPULARITY=" + movieJson.getString(MDB_POPULARITY) + "\t MDB_RATING= " + movie.getRating());
            }

            movies.add(movie);
        }

        return movies;
    }

    @Override
    protected ArrayList<Movie> doInBackground(String... params) {

        //If there's no sort definition, there's nothing to loop up. Verify size of params.
        if (params.length < 2) {
            return null;
        }
        // These two need to be declared outside the try/catch
        // so that they can be closed in the finally block.
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        // Will contain the raw JSON response as a string.
        String moviesJsonStr = null;

        try {
            // Construct the URL for the TheMovieDB API query
            // http://api.themoviedb.org/3/discover/movie?
            final String SORT_PARAM = "sort_by";
            final String PAGE_PARAM = "page";
            final String MINVOTECOUNT_PARAM = "vote_count.gte";
            final String APPID_PARAM = "api_key";

            Uri builtUri = Util.THEMOVIEDB_BASE_URI.buildUpon()
                    .appendPath("discover")
                    .appendPath("movie")
                    .appendQueryParameter(SORT_PARAM, params[0]) //popularity.desc
                    .appendQueryParameter(PAGE_PARAM, params[1]) //1
                    .appendQueryParameter(MINVOTECOUNT_PARAM, "300")
                    .appendQueryParameter(APPID_PARAM, BuildConfig.THE_MOVIE_DB_API_KEY)
                    .build();

            URL url = new URL(builtUri.toString());
            Log.v(LOG_TAG, url.toString());

            // Create the request to OpenWeatherMap, and open the connection
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // Read the input stream into a String
            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                // Nothing to do.
                return null;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                // But it does make debugging a *lot* easier if you print out the completed
                // buffer for debugging.
                buffer.append(line + "\n");
            }

            if (buffer.length() == 0) {
                // Stream was empty.  No point in parsing.
                return null;
            }
            moviesJsonStr = buffer.toString();

        } catch (IOException e) {
            Log.e(LOG_TAG, "Error ", e);
            // If the code didn't successfully get the movie data, there's no point in attemping to parse it.
            return null;
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e(LOG_TAG, "Error closing stream", e);
                }
            }
        }

        try {
            return getMoviesPosterDataFromJson(moviesJsonStr);
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
        }

        return null;
    }

    @Override
    protected void onProgressUpdate(Void... values) {
        super.onProgressUpdate(values);
    }

    @Override
    protected void onPostExecute(ArrayList<Movie> movies) {
        if (movies != null && movies.isEmpty() == false) {
            mMoviesAdapter.addAll(movies);
            SwipeRefreshLayout swipeRefreshLayout = ButterKnife.findById((Activity) mContext, R.id.main_swipe_refresh_layout);
            swipeRefreshLayout.setRefreshing(false);
            Toast.makeText(mContext, "finished loading movies", Toast.LENGTH_SHORT).show();
//            mMoviesAdapter.notifyDataSetChanged();
//            Log.v(LOG_TAG, "onPostExecute() mMovieList size: " + mMovieList.size() + " mMoviesAdapter size: " + mMoviesAdapter.getCount());
        }
//        mProgressBar.setVisibility(View.GONE);
    }
}
