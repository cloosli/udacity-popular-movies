package com.loosli.christian.popularmovieapp.android.app;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import com.loosli.christian.popularmovieapp.android.app.entity.Movie;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {
    private final String LOG_TAG = MainActivityFragment.class.getSimpleName();
    private static final String STATE_MOVIES = "state_movies";
    private static final String STATE_SORT_CRITERIA = "state_sort_criteria";
    private static final String STATE_START_PAGE = "state_start_page";

    private MoviesAdapter mMoviesAdapter;
    private ArrayList<Movie> mMovieList;
//    private ProgressBar mProgressBar;

    @Bind(R.id.main_swipe_refresh_layout)
    SwipeRefreshLayout mSwipeRefreshLayout;

    private int mTotalPageNumber = 1000;
    private SortCriteria mSortCriteria = SortCriteria.POPULARITY;
    private int mStartPage = 0;


    public void setSortCriteria(SortCriteria criteria) {
        if (mSortCriteria != criteria) {
            mSortCriteria = criteria;
            updateMovies(1);
        }
    }

    public enum SortCriteria {
        POPULARITY("popularity.desc"), RATING("vote_average.desc");
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
    public void onCreate(Bundle savedInstanceState) {
        Log.v(LOG_TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        if (savedInstanceState != null && savedInstanceState.containsKey(STATE_MOVIES)) {
            mMovieList = savedInstanceState.getParcelableArrayList(STATE_MOVIES);
        } else {
            mMovieList = new ArrayList<>();
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
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.movies_fragment, menu);
//        if (mSortCriteria == SortCriteria.POPULARITY) {
//            MenuItem item = menu.findItem(R.id.action_sort_popularity);
//            item.setEnabled(false);
//        } else {
//            MenuItem item = menu.findItem(R.id.action_sort_rating);
//            item.setEnabled(false);
//        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final int id = item.getItemId();

        if (id == R.id.action_sort_popularity) {
            setSortCriteria(SortCriteria.POPULARITY);
            return true;
        }
        if (id == R.id.action_sort_rating) {
            setSortCriteria(SortCriteria.RATING);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        ButterKnife.bind(this, rootView);

        mMoviesAdapter = new MoviesAdapter(getActivity(), mMovieList);
        Log.v(LOG_TAG, "onCreateView() mMovieList size: " + mMovieList.size() + " mMoviesAdapter size: " + mMoviesAdapter.getCount() + " mStartPage=" + mStartPage);
//        mProgressBar = (ProgressBar)rootView.findViewById(R.id.progressBar);
//        mProgressBar.setVisibility(View.GONE);
        GridView gridView = (GridView) rootView.findViewById(R.id.movies_gridview);
        gridView.setAdapter(mMoviesAdapter);
        Log.d(LOG_TAG, "gridView.getNumColumns() = " + gridView.getNumColumns());
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Movie movie = mMoviesAdapter.getItem(position);
                Intent intent = new Intent(getActivity(), MovieDetailActivity.class);
                intent.putExtra(BundleKeys.MOVIE, movie);
                getActivity().startActivity(intent);
            }
        });
        int visibleThreshold = 5;
        gridView.setOnScrollListener(new EndlessScrollListener(visibleThreshold, mStartPage) {
            @Override
            public boolean onLoadMore(int page, int totalItemsCount) {
                Log.v(LOG_TAG, "EndlessScrollListener.onLoadMore(" + page + ", " + totalItemsCount + ")");
                mStartPage = page - 1;
                loadMoreMoviesFromApi(page);
                return true;
            }
        });
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // do nothing!
            }
        });
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

    private void loadMoreMoviesFromApi(int offset) {
        Log.v(LOG_TAG, "loadMoreMoviesFromApi(" + offset + ")");
        updateMovies(offset);
    }

    private void updateMovies(int page) {
        Log.v(LOG_TAG, "updateMovies(" + page + ") > fetsch more mTotalPageNumber: " + mTotalPageNumber);
        if (page <= 1 && mMovieList.isEmpty() == false) {
            Log.v(LOG_TAG, "clear mMovieList, mMoviesAdapter size: " + mMoviesAdapter.getCount());
            mStartPage = 0;
            mMoviesAdapter.clearData();
//            mMoviesAdapter.notifyDataSetChanged();
        }
        if (mTotalPageNumber == 0 || page <= mTotalPageNumber) {
            FetchMoviesTask task = new FetchMoviesTask(getActivity(), mMoviesAdapter);
            task.execute(mSortCriteria.toString(), Integer.toString(page));
//            mSwipeRefreshLayout.setRefreshing(true);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.v(LOG_TAG, "onStart()");
        Log.v(LOG_TAG, "onStart() > mMovieList size: " + mMovieList.size() + " mStartPage=" + mStartPage);
        if (mMovieList.isEmpty()) {
            updateMovies(1);
        }
    }

    @Override public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }
}
