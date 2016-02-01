package com.loosli.christian.popularmovieapp.android.app.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.test.AndroidTestCase;

import java.util.Map;
import java.util.Set;

/**
 * Created by ChristianL on 01.02.16.
 */
public class TestUtilities extends AndroidTestCase {
    static final long TEST_DATE = 1419033600L;  // December 20th, 2014

    public static ContentValues createMovieValues() {
        ContentValues movieValues = new ContentValues();
        movieValues.put(MovieContract.MovieEntry._ID, "123");
        movieValues.put(MovieContract.MovieEntry.COLUMN_TITLE, "Inside Out");
        movieValues.put(MovieContract.MovieEntry.COLUMN_OVERVIEW, "Growing up can be a bumpy road, and it's no exception for Riley,...");
        movieValues.put(MovieContract.MovieEntry.COLUMN_POSTERPATH, "/aAmfIX3TT40zUHGcCKrlOZRKC7u.jpg");
        movieValues.put(MovieContract.MovieEntry.COLUMN_BACKDROPPATH, "/szytSpLAyBh3ULei3x663mAv5ZT.jpg");
        movieValues.put(MovieContract.MovieEntry.COLUMN_RATING, 8.04);
        movieValues.put(MovieContract.MovieEntry.COLUMN_RELEASEDATE, TEST_DATE);

        return movieValues;
    }

    public static void validateCurrentRecord(String error, Cursor valueCursor, ContentValues expectedValues) {
        Set<Map.Entry<String, Object>> valueSet = expectedValues.valueSet();
        for (Map.Entry<String, Object> entry : valueSet) {
            String columnName = entry.getKey();
            int idx = valueCursor.getColumnIndex(columnName);
            assertFalse("Column '" + columnName + "' not found. " + error, idx == -1);
            String expectedValue = entry.getValue().toString();
            assertEquals("Value '" + entry.getValue().toString() +
                    "' did not match the expected value '" +
                    expectedValue + "'. " + error, expectedValue, valueCursor.getString(idx));
        }
    }
}
