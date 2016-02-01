package com.loosli.christian.popularmovieapp.android.app.data;

import android.content.ComponentName;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;

import com.loosli.christian.popularmovieapp.android.app.data.MovieContract.MovieEntry;

/**
 * Created by ChristianL on 01.02.16.
 */
public class TestProvider extends AndroidTestCase {

    public static final String LOG_TAG = TestProvider.class.getSimpleName();

    /*
       This helper function deletes all records from both database tables using the ContentProvider.
       It also queries the ContentProvider to make sure that the database has been successfully
       deleted, so it cannot be used until the Query and Delete functions have been written
       in the ContentProvider.

       Students: Replace the calls to deleteAllRecordsFromDB with this one after you have written
       the delete functionality in the ContentProvider.
     */
    public void deleteAllRecordsFromProvider() {
        mContext.getContentResolver().delete(
                MovieEntry.CONTENT_URI,
                null,
                null
        );

        Cursor cursor = mContext.getContentResolver().query(
                MovieEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );
        assertEquals("Error: Records not deleted from Movie table during delete", 0, cursor.getCount());
        cursor.close();
    }

    public void deleteAllRecords() {
        deleteAllRecordsFromProvider();
    }

    // Since we want each test to start with a clean slate, run deleteAllRecords
    // in setUp (called by the test runner before each test).
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        deleteAllRecords();
    }

    /*
        This test checks to make sure that the content provider is registered correctly.
    */
    public void testProviderRegistry() {
        PackageManager pm = mContext.getPackageManager();

        // We define the component name based on the package name from the context and the
        // MovieProvider class.
        ComponentName componentName = new ComponentName(mContext.getPackageName(),
                MovieProvider.class.getName());
        try {
            // Fetch the provider info using the component name from the PackageManager
            // This throws an exception if the provider isn't registered.
            ProviderInfo providerInfo = pm.getProviderInfo(componentName, 0);

            // Make sure that the registered authority matches the authority from the Contract.
            assertEquals("Error: MovieProvider registered with authority: " + providerInfo.authority +
                            " instead of authority: " + MovieContract.CONTENT_AUTHORITY,
                    providerInfo.authority, MovieContract.CONTENT_AUTHORITY);
        } catch (PackageManager.NameNotFoundException e) {
            // I guess the provider isn't registered correctly.
            assertTrue("Error: MovieProvider not registered at " + mContext.getPackageName(), false);
        }
    }

    /*
        This test doesn't touch the database.  It verifies that the ContentProvider returns
        the correct type for each type of URI that it can handle.
        Students: Uncomment this test to verify that your implementation of GetType is
        functioning correctly.
     */
    public void testGetType() {
        // content://com.example.android.sunshine.app/weather/
        String type = mContext.getContentResolver().getType(MovieEntry.CONTENT_URI);
        // vnd.android.cursor.dir/com.example.android.sunshine.app/weather
        assertEquals("Error: the WeatherEntry CONTENT_URI should return WeatherEntry.CONTENT_TYPE", MovieEntry.CONTENT_TYPE, type);

        long testMovieId = 94074;
        // content://com.example.android.sunshine.app/weather/94074
        type = mContext.getContentResolver().getType(MovieEntry.buildMovieUri(testMovieId));
        // vnd.android.cursor.dir/com.example.android.sunshine.app/weather
        assertEquals("Error: the WeatherEntry CONTENT_URI with location should return WeatherEntry.CONTENT_TYPE", MovieEntry.CONTENT_ITEM_TYPE, type);
    }

    /*
        This test uses the database directly to insert and then uses the ContentProvider to
        read out the data.  Uncomment this test to see if the basic movie query functionality
        given in the ContentProvider is working correctly.
     */
    public void testBasicMovieQuery() {
        // insert our test records into the database
        MovieDbHelper dbHelper = new MovieDbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Fantastic.  Now that we have a location, add some weather!
        ContentValues moviesValues = TestUtilities.createMovieValues();

        long movieRowId = db.insert(MovieEntry.TABLE_NAME, null, moviesValues);
        assertTrue("Unable to Insert WeatherEntry into the Database", movieRowId != -1);

        db.close();

        // Test the basic content provider query
        Cursor moviesCursor = mContext.getContentResolver().query(
                MovieEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );

        // Make sure we get the correct cursor out of the database
        TestUtilities.validateCursor("testBasicMovieQuery", moviesCursor, moviesValues);
    }

    /*
    This test uses the database directly to insert and then uses the ContentProvider to
    read out the data.  Uncomment this test to see if the basic movie query functionality
    given in the ContentProvider is working correctly.
 */
    public void testMovieByIdQuery() {
        // insert our test records into the database
        MovieDbHelper dbHelper = new MovieDbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Fantastic.  Now that we have a location, add some weather!
        ContentValues moviesValues = TestUtilities.createMovieValues();

        long movieRowId = db.insert(MovieEntry.TABLE_NAME, null, moviesValues);
        assertTrue("Unable to Insert WeatherEntry into the Database", movieRowId != -1);

        db.close();

        // Test the basic content provider query
        Cursor moviesCursor = mContext.getContentResolver().query(
                MovieEntry.buildMovieUri(123),
                null,
                null,
                null,
                null
        );

        // Make sure we get the correct cursor out of the database
        TestUtilities.validateCursor("testMovieByIdQuery", moviesCursor, moviesValues);
    }
}
