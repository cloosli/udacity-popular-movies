package com.loosli.christian.popularmovieapp.android.app;

import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Toast;

import com.loosli.christian.popularmovieapp.android.app.adapter.MovieAdapter;
import com.loosli.christian.popularmovieapp.android.app.api.TheMovieDBService;
import com.loosli.christian.popularmovieapp.android.app.data.MovieContract;
import com.loosli.christian.popularmovieapp.android.app.entity.Movie;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.Bind;
import butterknife.BindDimen;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, SwipeRefreshLayout.OnRefreshListener {
    private final String LOG_TAG = MainActivityFragment.class.getSimpleName();
    private static final String STATE_MOVIES = "state_movies";
    private static final String STATE_SORT_CRITERIA = "state_sort_criteria";
    private static final String STATE_START_PAGE = "state_start_page";
    private static final int MOVIE_LOADER = 0;

    // For the forecast view we're showing only a small subset of the stored data.
    // Specify the columns we need.
    private static final String[] FORECAST_COLUMNS = {
            // In this case the id needs to be fully qualified with a table name, since
            // the content provider joins the location & weather tables in the background
            // (both have an _id column)
            // On the one hand, that's annoying.  On the other, you can search the weather table
            // using the location set by the user, which is only in the Location table.
            // So the convenience is worth it.
            MovieContract.MovieEntry.TABLE_NAME + "." + MovieContract.MovieEntry._ID,
            MovieContract.MovieEntry.COLUMN_TITLE,
            MovieContract.MovieEntry.COLUMN_OVERVIEW,
            MovieContract.MovieEntry.COLUMN_POSTERPATH,
            MovieContract.MovieEntry.COLUMN_BACKDROPPATH,
            MovieContract.MovieEntry.COLUMN_RATING,
            MovieContract.MovieEntry.COLUMN_RELEASEDATE,
    };

    // These indices are tied to FORECAST_COLUMNS.  If FORECAST_COLUMNS changes, these
    // must change.
    static final int COL_MOVIE_ID = 0;
    static final int COL_MOVIE_TITEL = 1;
    static final int COL_MOVIE_OVERVIEW = 2;
    static final int COL_MOVIE_POSTERPATH = 3;
    static final int COL_MOVIE_BACKDROP = 4;
    static final int COL_MOVIE_RATING = 5;
    static final int COL_MOVIE_RELEASEDATE = 6;

    @Bind(R.id.main_swipe_refresh_layout)
    SwipeRefreshLayout mSwipeRefreshLayout;

    @Bind(R.id.movies_recycler_view)
    RecyclerView mRecylerView;

    @BindDimen(R.dimen.movie_thumb_width)
    int mDesiredColumnWidth;

    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private ArrayList<Movie> mMovieList = new ArrayList<>();
    private int mTotalPages = 1000;
    private SortCriteria mSortCriteria = SortCriteria.POPULARITY;
    private int mStartPage = 0;

    public enum SortCriteria {
        POPULARITY("popularity.desc"), RATING("vote_average.desc"), FAVORITES("");
        public final String name;

        SortCriteria(String name) {
            this.name = name;
        }

        public String toString() {
            return this.name;
        }
    }

    public MainActivityFragment() {
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.movies_fragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_sort_favorites:
                item.setChecked(true);
                setSortCriteria(SortCriteria.FAVORITES);
                return true;
            case R.id.action_sort_popularity:
                item.setChecked(true);
                setSortCriteria(SortCriteria.POPULARITY);
                return true;

            case R.id.action_sort_rating:
                item.setChecked(true);
                setSortCriteria(SortCriteria.RATING);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.v(LOG_TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        if (savedInstanceState != null && savedInstanceState.containsKey(STATE_MOVIES)) {
            mMovieList = savedInstanceState.getParcelableArrayList(STATE_MOVIES);
        }
        Log.v(LOG_TAG, "onCreate > mMovieList size=" + mMovieList.size());
        if (savedInstanceState != null && savedInstanceState.containsKey(STATE_SORT_CRITERIA)) {
            mSortCriteria = SortCriteria.valueOf(savedInstanceState.getString(STATE_SORT_CRITERIA));
        }
        if (savedInstanceState != null && savedInstanceState.containsKey(STATE_START_PAGE)) {
            mStartPage = savedInstanceState.getInt(STATE_START_PAGE);
        }

        Log.v(LOG_TAG, "onCreate > mStartPage=" + mStartPage + " mSortCriteria=" + mSortCriteria.toString());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        final View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        ButterKnife.bind(this, rootView);
        // improve performance
        mRecylerView.setHasFixedSize(true);
        rootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (Build.VERSION.SDK_INT < 16) {
                    rootView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                } else {
                    rootView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }

                int gridWidth = mRecylerView.getWidth();
                int optimalColumnCount = Math.max(Math.round((1f * gridWidth) / mDesiredColumnWidth), 1);
                int actualPosterViewWidth = gridWidth / optimalColumnCount;

                Toast.makeText(getActivity(), "gridWidth=" + gridWidth + ", actualPosterViewWidth:" + actualPosterViewWidth, Toast.LENGTH_SHORT).show();

                mLayoutManager = new GridLayoutManager(mRecylerView.getContext(), optimalColumnCount);
                mRecylerView.setLayoutManager(mLayoutManager);

                //specify an adapter
                mAdapter = new MovieAdapter(getActivity(), mMovieList, actualPosterViewWidth, (MovieAdapter.OnMovieClickListener) getActivity());
                mRecylerView.setAdapter(mAdapter);
            }
        });

//        gridView.smoothScrollToPosition(0);
//        int visibleThreshold = 8;
//        gridView.setOnScrollListener(new EndlessScrollListener(visibleThreshold, mStartPage) {
//            @Override
//            public boolean onLoadMore(int page, int totalItemsCount) {
//                Log.v(LOG_TAG, "EndlessScrollListener.onLoadMore(" + page + ", " + totalItemsCount + ")");
//                if (mSortCriteria == SortCriteria.FAVORITES) {
//                    return true;
//                }
//                mStartPage = page - 1;
//                loadMoreMoviesFromApi(page);
//                return true;
//            }
//        });
        mSwipeRefreshLayout.setOnRefreshListener(this);

        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(STATE_MOVIES, mMovieList);
        outState.putString(STATE_SORT_CRITERIA, mSortCriteria.name());
        outState.putInt(STATE_START_PAGE, mStartPage);
        Log.v(LOG_TAG, "onSaveInstanceState() > mStartPage: " + mStartPage);
    }

    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        Log.v(LOG_TAG, "onViewStateRestored");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.v(LOG_TAG, "onResume");
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.v(LOG_TAG, "onStart()");
        Log.v(LOG_TAG, "onStart() > mMovieList size: " + mMovieList.size() + " mStartPage=" + mStartPage);
        if (mMovieList.isEmpty()) {
            loadMovies(1);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        stopRefreshing();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    @Override
    public void onRefresh() {
        Log.v(LOG_TAG, "onRefresh()");
        loadMovies(1);
    }

    public void stopRefreshing() {
        mSwipeRefreshLayout.setRefreshing(false);
    }

    public void setSortCriteria(SortCriteria criteria) {
        if (mSortCriteria != criteria) {
            mSortCriteria = criteria;
            loadMovies(1);
        }
    }

    private void loadMovies(int page) {
        Log.v(LOG_TAG, "loadMovies(" + page + ") > fetsch more mTotalPages: " + mTotalPages);
        if (page <= 1 && mMovieList.isEmpty() == false) {
            Log.v(LOG_TAG, "clear mMovieList, mMoviesAdapter size: " + mAdapter.getItemCount());
            mStartPage = 0;
            mMovieList.clear();
            mAdapter.notifyDataSetChanged();
        }

        if (mSortCriteria == SortCriteria.FAVORITES) {
            getLoaderManager().restartLoader(MOVIE_LOADER, null, this);
            return;
        }
        if (mTotalPages == 0 || page <= mTotalPages) {
            Log.v(LOG_TAG, "create TheMovieDBService and enqueue");
            if (mSwipeRefreshLayout != null && !mSwipeRefreshLayout.isRefreshing()) {
                mSwipeRefreshLayout.setRefreshing(true);  // This show the spinner on top of activity
            }
            TheMovieDBService.TMDBAPI tmdbapi = TheMovieDBService.getRetrofitBuild().create(TheMovieDBService.TMDBAPI.class);
            Call<Movie.Response> call = tmdbapi.getMovies(mSortCriteria.toString(), page, 300);
            call.enqueue(new Callback<Movie.Response>() {
                @Override
                public void onResponse(Response<Movie.Response> response) {
                    Movie.Response moviesResponse = response.body();
                    try {
                        List<Movie> movies = moviesResponse.movies;
                        mTotalPages = moviesResponse.total_pages;
                        mMovieList.addAll(movies);
                        mAdapter.notifyDataSetChanged();
                        mSwipeRefreshLayout.setRefreshing(false);
                        Toast.makeText(getActivity(), "finished loading page " + moviesResponse.page, Toast.LENGTH_SHORT).show();
                        Log.i(LOG_TAG, response.raw().request().url().toString());
                    } catch (NullPointerException e) {
                        Toast.makeText(getActivity(), "NullPointerException " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                    stopRefreshing();
                }

                @Override
                public void onFailure(Throwable t) {
                    Log.e(LOG_TAG, t.getMessage(), t);
                    Snackbar.make(getView(), "Error while fetching data", Snackbar.LENGTH_LONG).show();
                    stopRefreshing();
                    setSortCriteria(SortCriteria.FAVORITES);
                }
            });


        } else {
            Log.v(LOG_TAG, "do not load more movies");
        }
    }

    /*
    * Callback that's invoked when the system has initialized the Loader and
    * is ready to start the query. This usually happens when initLoader() is
    * called. The loaderID argument contains the ID value passed to the
    * initLoader() call.
    */
    @Override
    public Loader<Cursor> onCreateLoader(int loaderID, Bundle bundle) {
        if (mSwipeRefreshLayout != null && !mSwipeRefreshLayout.isRefreshing()) {
            mSwipeRefreshLayout.setRefreshing(true);  // This show the spinner on top of activity
        }
        /*
        * Takes action based on the ID of the Loader that's being created
        */
        switch (loaderID) {
            case MOVIE_LOADER:
                // Returns a new CursorLoader
                return new CursorLoader(
                        getActivity(),   // Parent activity context
                        MovieContract.MovieEntry.CONTENT_URI,        // Table to query
                        FORECAST_COLUMNS,     // Projection to return
                        null,            // No selection clause
                        null,            // No selection arguments
                        null             // Default sort order
                );
            default:
                // An invalid id was passed in
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        mStartPage = 0;
        mMovieList.clear();

        while (cursor.moveToNext()) {
            Movie movie = new Movie();

            movie.setId(cursor.getInt(COL_MOVIE_ID));
            movie.setTitle(cursor.getString(COL_MOVIE_TITEL));
            movie.setOverview(cursor.getString(COL_MOVIE_OVERVIEW));
            movie.setPosterPath(cursor.getString(COL_MOVIE_POSTERPATH));
            movie.setBackdropPath(cursor.getString(COL_MOVIE_BACKDROP));
            movie.setRating(cursor.getFloat(COL_MOVIE_RATING));
            Date releaseDate = new Date();
            releaseDate.setTime(cursor.getLong(COL_MOVIE_RELEASEDATE));
            movie.setReleaseDate(releaseDate);
//            movie.setAdult(cursor.getString() == 1 ? true : false);
//            movie.setOriginalLanguage(cursor.getString());

            mMovieList.add(movie);
        }
        mAdapter.notifyDataSetChanged();
        stopRefreshing();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
//        mForecastAdapter.swapCursor(null);
    }
}
