package com.loosli.christian.popularmovieapp.android.app;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Random;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {

    private MoviesAdapter mMoviesAdapter;

    public MainActivityFragment() { }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.movies_fragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final int id = item.getItemId();

        if (id == R.id.action_refresh) {
            updateMovies();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        mMoviesAdapter = new MoviesAdapter(getActivity());

        GridView gridView = (GridView) rootView.findViewById(R.id.movies_gridview);
        gridView.setAdapter(mMoviesAdapter);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String movie = mMoviesAdapter.getItem(position);
                Toast.makeText(getActivity(), movie + " " + position, Toast.LENGTH_SHORT).show();
            }
        });
        return rootView;
    }

    private void updateMovies() {
        FetchMoviesTask task = new FetchMoviesTask();
        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        //String location = prefs.getString(getString(R.string.pref_sorting_key), getString(R.string.pref_sorting_default));
        task.execute("popularity.desc", Integer.toString(new Random().nextInt(20)));
    }

    @Override
    public void onStart() {
        super.onStart();
        updateMovies();
    }

    public class FetchMoviesTask extends AsyncTask<String, Void, String[]> {
        private final String LOG_TAG = FetchMoviesTask.class.getSimpleName();

        /* The date/time conversion code is going to be moved outside the asynctask later,
        * so for convenience we're breaking it out into its own method now.
        */
        private String getReadableDateString(long time) {
            // Because the API returns a unix timestamp (measured in seconds),
            // it must be converted to milliseconds in order to be converted to valid date.
            SimpleDateFormat shortenedDateFormat = new SimpleDateFormat("EEE MMM dd");
            return shortenedDateFormat.format(time);
        }

        /**
         * Take the String representing the complete forecast in JSON Format and
         * pull out the data we need to construct the Strings needed for the wireframes.
         * <p/>
         * Fortunately parsing is easy:  constructor takes the JSON string and converts it
         * into an Object hierarchy for us.
         */
        private String[] getMoviesPosterDataFromJson(String moviesJsonStr)
                throws JSONException {

            // These are the names of the JSON objects that need to be extracted.

            final String OWM_PAGE = "page";
            final String OWM_TOTAL_PAGES="total_pages";
            final String OWM_RESULTS = "results";
            final String OWM_ORIG_TITLE = "original_title";
            final String OWM_TITLE = "title";
            final String OWM_POSTER = "poster_path";


            JSONObject moviesJson = new JSONObject(moviesJsonStr);
            JSONArray moviesArray = moviesJson.getJSONArray(OWM_RESULTS);
            final int moviesArraySize = moviesArray.length();
            final String[] resultStrs = new String[moviesArraySize];
            for (int i = 0; i < moviesArraySize; i++) {

                // Get the JSON object representing the movie
                JSONObject movie = moviesArray.getJSONObject(i);
                String posterPath = movie.getString(OWM_POSTER);
                String title = movie.getString(OWM_TITLE);
                Log.v(LOG_TAG, "Movie: " + title + " Poster: " + posterPath);

                // The date/time is returned as a long.  We need to convert that
                // into something human-readable, since most people won't read "1400356800" as
                // "this saturday".
                //long dateTime;
                // Cheating to convert this to UTC time, which is what we want anyhow
                //dateTime = dayTime.setJulianDay(julianStartDay + i);
                //day = getReadableDateString(dateTime);


                resultStrs[i] = posterPath;
            }

            return resultStrs;

        }

        @Override
        protected String[] doInBackground(String... params) {

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
                final String FORECAST_BASE_URL = "http://api.themoviedb.org/3/discover/movie?";
                final String SORT_PARAM = "sort_by";
                final String PAGE_PARAM = "page";
                final String APPID_PARAM = "api_key";

                Uri builtUri = Uri.parse(FORECAST_BASE_URL).buildUpon()
                        .appendQueryParameter(SORT_PARAM, params[0]) //popularity.desc
                        .appendQueryParameter(PAGE_PARAM, params[1]) //1
                        .appendQueryParameter(APPID_PARAM, BuildConfig.THE_MOVIE_DB_API_KEY)
                        .build();

                URL url = new URL(builtUri.toString());

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
                // If the code didn't successfully get the weather data, there's no point in attemping
                // to parse it.
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
        protected void onPostExecute(String[] s) {
            if (s != null) {
                mMoviesAdapter.addAll(Arrays.asList(s));
            }
        }
    }
}
