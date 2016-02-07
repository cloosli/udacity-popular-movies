package com.loosli.christian.popularmovieapp.android.app;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.loosli.christian.popularmovieapp.android.app.adapter.MovieAdapter;
import com.loosli.christian.popularmovieapp.android.app.entity.Movie;

public class MoviesActivity extends AppCompatActivity implements MovieAdapter.OnMovieClickListener {
    private final String LOG_TAG = MoviesActivity.class.getSimpleName();
    private static final String DETAILFRAGMENT_TAG = "DFTAG";
    private static final String KEY_SELECTED_MOVIE = "selected_movie";

    private boolean mTwoPane;

    // only used in two-pane layout
    private MovieDetailsFragment mDetailsFragment = null;
    private Movie mMovie;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(getTitle());

        if (findViewById(R.id.movie_detail_container) != null) {
            // The detail container view will be present only in the large-screen layouts
            // (res/layout-sw600dp). If this view is present, then the activity should be
            // in two-pane mode.
            mTwoPane = true;
            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.
            if (savedInstanceState != null) {
                Movie movie = savedInstanceState.getParcelable(KEY_SELECTED_MOVIE);
                showMovieDetails(movie);
            }
        } else {
            mTwoPane = false;
            getSupportActionBar().setElevation(0f);
        }

//        MoviesFragment moviesFragment = ((MoviesFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_movies));
//        moviesFragment.setUseTodayLayout(!mTwoPane);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(KEY_SELECTED_MOVIE, mMovie);
//        outState.putString(KEY_SORT_ORDER, mSort.name());
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(LOG_TAG, "onResume()");
//        String location = Utility.getPreferredLocation(this);
        // update the location in our second pane using the fragment manager
//        if (location != null && !location.equals(mLocation)) {
//            ForecastFragment ff = (ForecastFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_forecast);
//            if (null != ff) {
//                ff.onLocationChanged();
//            }
//            DetailFragment df = (DetailFragment) getSupportFragmentManager().findFragmentByTag(DETAILFRAGMENT_TAG);
//            if (null != df) {
//                df.onLocationChanged(location);
//            }
//            mLocation = location;
//        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onMovieClicked(@NonNull Movie movie, View view, int position) {
        MoviesFragment moviesFragment = ((MoviesFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_movies));
        moviesFragment.setPosition(position);
        if (mTwoPane) {
            showMovieDetails(movie);
        } else {
            Intent intent = new Intent(this, MovieDetailsActivity.class);
            intent.putExtra(BundleKeys.MOVIE, movie);
            startActivity(intent);
        }
    }

    public void showMovieDetails(Movie movie) {
        if (!mTwoPane || movie == null) {
            return;
        }
        mMovie = movie;
        if (mDetailsFragment == null) {
            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.
            mDetailsFragment = MovieDetailsFragment.newInstance(movie);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.movie_detail_container, mDetailsFragment, DETAILFRAGMENT_TAG)
                    .commit();
        } else {
            mDetailsFragment.setMovie(movie);
        }
    }
}
