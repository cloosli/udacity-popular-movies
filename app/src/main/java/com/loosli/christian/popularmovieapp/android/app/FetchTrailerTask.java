package com.loosli.christian.popularmovieapp.android.app;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.loosli.christian.popularmovieapp.android.app.util.Util;

import java.io.IOException;
import java.net.URL;

/**
 * Created by ChristianL on 28.01.16.
 */
public class FetchTrailerTask extends AsyncTask<String, Void, Void> {
    private final String LOG_TAG = FetchTrailerTask.class.getSimpleName();

    private Context mContext;

    FetchTrailerTask(Context context) {
        mContext = context;
    }

    @Override
    protected Void doInBackground(String... params) {

        if (params.length < 1) {
            return null;
        }

        try {
            final String APPID_PARAM = "api_key";
            Uri builtUri = Util.THEMOVIEDB_BASE_URI.buildUpon()
                    .appendPath("movie")
                    .appendPath(params[0]) // movie id
                    .appendPath("videos")
                    .appendQueryParameter(APPID_PARAM, BuildConfig.THE_MOVIE_DB_API_KEY)
                    .build();

            URL url = new URL(builtUri.toString());
            Log.v(LOG_TAG, url.toString());

        } catch (IOException e) {
            Log.e(LOG_TAG, "Error ", e);
            // If the code didn't successfully get the movie data, there's no point in attemping to parse it.
            return null;
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
    }
}
