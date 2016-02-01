package com.loosli.christian.popularmovieapp.android.app.data;

import android.net.Uri;
import android.test.AndroidTestCase;

/**
 * Created by ChristianL on 01.02.16.
 */
public class TestMovieContract extends AndroidTestCase {

    // intentionally includes a slash to make sure Uri is getting quoted correctly
    private static final long TEST_MOVIE_ID = 123L;

    /*
        Students: Uncomment this out to test your weather location function.
     */
    public void testBuildWeatherLocation() {
        Uri locationUri = MovieContract.MovieEntry.buildMovieUri(TEST_MOVIE_ID);
        assertNotNull("Error: Null Uri returned.  You must fill-in buildWeatherLocation in MovieContract.", locationUri);
        assertEquals("Error: Movie id not properly appended to the end of the Uri",
                Long.toString(TEST_MOVIE_ID), locationUri.getLastPathSegment());
        assertEquals("Error: Weather location Uri doesn't match our expected result",
                locationUri.toString(),
                "content://com.loosli.christian.popularmovieapp.android.app/movie/123");
    }
}
