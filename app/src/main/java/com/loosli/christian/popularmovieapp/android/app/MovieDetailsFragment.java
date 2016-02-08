package com.loosli.christian.popularmovieapp.android.app;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.AsyncQueryHandler;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.loosli.christian.popularmovieapp.android.app.api.TheMovieDBService;
import com.loosli.christian.popularmovieapp.android.app.data.MovieContract;
import com.loosli.christian.popularmovieapp.android.app.entity.Movie;
import com.loosli.christian.popularmovieapp.android.app.entity.Review;
import com.loosli.christian.popularmovieapp.android.app.entity.Video;
import com.loosli.christian.popularmovieapp.android.app.util.Util;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * A placeholder fragment containing a simple view.
 */
public class MovieDetailsFragment extends Fragment implements View.OnClickListener, LoaderManager.LoaderCallbacks<Cursor> {
    private static final String LOGTAG = MovieDetailsFragment.class.getSimpleName();

    public static final String ARG_MOVIE = "arg_movie";
    private static final String STATE_REVIEWS = "state_reviews";
    private static final String STATE_VIDEOS = "state_trailers";
    private static final int DETAIL_LOADER = 1;

    private static final String[] DETAIL_COLUMNS = {
            MovieContract.MovieEntry.TABLE_NAME + "." + MovieContract.MovieEntry._ID,
    };
    static final int COL_MOVIE_ID = 0;

    private Movie mMovie;
    private List<Video> mVideos = Collections.EMPTY_LIST;
    private List<Review> mReviews = Collections.EMPTY_LIST;

    private MenuItem mMenuItemShare;

    @Bind(R.id.title)
    TextView mTitle;
    @Bind(R.id.release_date)
    TextView mReleaseDate;
    @Bind(R.id.rating)
    TextView mRating;
    @Bind(R.id.synopsis)
    TextView mOverview;

    @Bind(R.id.detail_movie_videos)
    LinearLayout mVideosLayout;

    @Bind(R.id.detail_movie_reviews)
    LinearLayout mReviewsLayout;

    @Bind(R.id.play_button)
    ImageButton mPlayButton;

    @Bind(R.id.fab_fav)
    FloatingActionButton mFavFAB;

    @Bind(R.id.poster)
    ImageView mPoster;

    @Bind(R.id.backdrop)
    ImageView mBackdrop;

    public MovieDetailsFragment() {
        setHasOptionsMenu(true);
    }

    public static MovieDetailsFragment newInstance(Movie movie) {
        Bundle args = new Bundle();
        args.putParcelable(MovieDetailsFragment.ARG_MOVIE, movie);
        MovieDetailsFragment fragment = new MovieDetailsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(LOGTAG, "onCreate");
        if (getArguments() != null && getArguments().containsKey(ARG_MOVIE)) {
            // Load the dummy content specified by the fragment
            // arguments. In a real-world scenario, use a Loader
            // to load content from a content provider.
            mMovie = getArguments().getParcelable(MovieDetailsFragment.ARG_MOVIE);
            if (mMovie == null) {
                throw new IllegalStateException("No movie given!");
            }
            Activity activity = this.getActivity();
//            CollapsingToolbarLayout appBarLayout = (CollapsingToolbarLayout) activity.findViewById(R.id.toolbar_layout);
//            if (appBarLayout != null) {
//                appBarLayout.setTitle(mMovie.getTitle());
//            }
            Toolbar toolbar = ButterKnife.findById(activity, R.id.toolbar);
            if (toolbar != null && mMovie != null) {
                toolbar.setTitle(mMovie.getTitle());
            }
        }
        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(STATE_VIDEOS)) {
                mVideos = savedInstanceState.getParcelableArrayList(STATE_VIDEOS);
            }
            if (savedInstanceState.containsKey(STATE_REVIEWS)) {
                mReviews = savedInstanceState.getParcelableArrayList(STATE_REVIEWS);
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.i(LOGTAG, "onCreateView");
        final View rootView = inflater.inflate(R.layout.fragment_movie_detail, container, false);
        ButterKnife.bind(this, rootView);

        rootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (Build.VERSION.SDK_INT < 16) {
                    rootView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                } else {
                    rootView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }
                if (mMovie != null) {
                    updateMovieDetails();
                }
                Log.d(LOGTAG, "rootView.width:" + rootView.getWidth());
            }
        });

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.i(LOGTAG, "onActivityCreated");
    }

    private void updateMovieDetails() {
        Log.i(LOGTAG, "updateMovieDetails");
        updateFavoriteBtn();
        updateShareBtn();
        if (!mMovie.isFavored()) {
            getLoaderManager().restartLoader(DETAIL_LOADER, null, this);
        }

        String voteAverage = String.format("%1$2.1f", mMovie.getRating());
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(mMovie.getReleaseDate());

        mTitle.setText(mMovie.getTitle());
        mOverview.setText(mMovie.getOverview());
        mRating.setText(voteAverage);
        mReleaseDate.setText(Integer.toString(calendar.get(Calendar.YEAR)));

        int posterWidth = getResources().getDimensionPixelSize(R.dimen.details_poster_width);
        int posterHeight = getResources().getDimensionPixelSize(R.dimen.details_poster_height);

        Picasso picasso = Picasso.with(getActivity());
        picasso.load(Util.buildPosterUrl(mMovie.getPosterPath(), posterWidth))
                .resize(posterWidth, posterHeight)
                .centerCrop()
                .placeholder(R.color.colorPoster)
                .into(mPoster);

        int backdropWidth = mBackdrop.getWidth(); //works because this method called after layout is ready //getResources().getDimensionPixelSize(R.dimen.details_backdrop_width);
        int backdropHeight = getResources().getDimensionPixelSize(R.dimen.details_backdrop_height);
        Log.v(LOGTAG, "getBackdropUrl for dimension: w" + backdropWidth + " * h" + backdropHeight);
        picasso.load(Util.buildBackdropUrl(mMovie.getBackdropPath(), backdropWidth))
                .resize(backdropWidth, backdropHeight)
                .centerCrop()
                .placeholder(R.color.colorBackdrop)
                .into(mBackdrop);

        TheMovieDBService.TMDBAPI tmdbapi = TheMovieDBService.getRetrofitBuild().create(TheMovieDBService.TMDBAPI.class);
        boolean connected = true; //TODO Util.hasNetworkConnection(getActivity()); // needs permission
        if (mVideos.isEmpty()) {
            fetchTrailerList(tmdbapi);
        } else {
            updateTrailers(mVideos);
        }
        if (mReviews.isEmpty()) {
            fetchReviewsList(tmdbapi);
        } else {
            updateReviews(mReviews);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        Log.i(LOGTAG, "onCreateOptionsMenu()");
        inflater.inflate(R.menu.fragmen_movie_detail, menu);
        mMenuItemShare = menu.findItem(R.id.action_share);
        updateShareBtn();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_share) {
            if (mPlayButton.getTag() != null) {
                getActivity().startActivity(Intent.createChooser(createShareTrailerIntent(), getResources().getString(R.string.title_share_trailer)));
            }
        }
        return true;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.i(LOGTAG, "onSaveInstanceState");
        // When tablets rotate, the currently selected list item needs to be saved.
        if (mReviews != null) {
            outState.putParcelableArrayList(STATE_REVIEWS, new ArrayList<Parcelable>(mReviews));
        }
        if (mVideos != null) {
            outState.putParcelableArrayList(STATE_VIDEOS, new ArrayList<Parcelable>(mVideos));
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    private void fetchReviewsList(TheMovieDBService.TMDBAPI tmdbapi) {
        Call<Review.Response> call = tmdbapi.getReviews(Long.toString(mMovie.getId()));
        call.enqueue(new Callback<Review.Response>() {
            @Override
            public void onResponse(Response<Review.Response> response) {
                try {
                    Review.Response reviewResult = response.body();
                    List<Review> reviews = reviewResult.reviews;
                    updateReviews(reviews);
                } catch (NullPointerException e) {
                    Log.e(LOGTAG, "NullPointerException: " + response.raw().body().toString(), e);
                }
            }

            @Override
            public void onFailure(Throwable t) {
                Log.e(LOGTAG, "getReviews threw: " + t.getMessage(), t);
            }
        });
    }

    private void fetchTrailerList(TheMovieDBService.TMDBAPI tmdbapi) {
        Call<Video.Response> call = tmdbapi.getVideos(Long.toString(mMovie.getId()));
        call.enqueue(new Callback<Video.Response>() {
            @Override
            public void onResponse(Response<Video.Response> response) {
                try {
                    Video.Response videoResult = response.body();
                    List<Video> videos = videoResult.videos;
                    updateTrailers(videos);
                    Log.v(LOGTAG, "fetchTrailerList: " + response.raw().request().url().toString());
                } catch (NullPointerException e) {
                    if (response != null && getActivity() != null) {
                        Log.e(LOGTAG, "NullPointerException: " + response.raw().body().toString(), e);
                        final String message = response.message() + " code: " + response.code();
                        Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
                    } else {
                        Log.e(LOGTAG, "NullPointerException: " + e.getMessage(), e);
                    }
                }
            }

            @Override
            public void onFailure(Throwable t) {
                Log.e(LOGTAG, "getVideos threw: " + t.getMessage(), t);
            }
        });
    }

    private void updateTrailers(List<Video> trailers) {
        mVideos = trailers;
        mVideosLayout.removeAllViews();
        mPlayButton.setTag(null);
        LayoutInflater inflater = getActivity().getLayoutInflater();
        Picasso picasso = Picasso.with(getActivity());
        for (Video trailer : trailers) {
            if (!trailer.isYouTube()) {
                Log.i(LOGTAG, "no youtube trailer");
                continue;
            }
            if (mPlayButton.getTag() == null) {
                mPlayButton.setTag(trailer.getKey());
                // update the share intent
                updateShareBtn();
            }
            ViewGroup thumbContainer = (ViewGroup) inflater.inflate(R.layout.detail_movie_video, mVideosLayout, false);
            ImageView thumbView = (ImageView) thumbContainer.findViewById(R.id.video_thumb);
            thumbView.setTag(trailer.getKey());
            thumbView.setOnClickListener(this);
            picasso.load(Video.getThumbnailUrl(trailer))
                    .resizeDimen(R.dimen.video_width, R.dimen.video_height)
                    .centerCrop()
                    .placeholder(R.color.colorBlueGrey300)
                    .into(thumbView);
            mVideosLayout.addView(thumbContainer);
        }
    }

    private void updateReviews(List<Review> reviews) {
        mReviews = reviews;
        mReviewsLayout.removeAllViews();
        LayoutInflater inflater = getActivity().getLayoutInflater();
        for (Review review : reviews) {
            ViewGroup thumbContainer = (ViewGroup) inflater.inflate(R.layout.detail_movie_review, mReviewsLayout, false);
            ((TextView) ButterKnife.findById(thumbContainer, R.id.review_author)).setText(review.getAuthor());
            ((TextView) ButterKnife.findById(thumbContainer, R.id.review_content)).setText(review.getContent());
            Button button = ButterKnife.findById(thumbContainer, R.id.review_link);
            button.setTag(review.getUrl());
            button.setOnClickListener(this);
            mReviewsLayout.addView(thumbContainer);
        }
    }

    @Override
    @OnClick({R.id.play_button, R.id.fab_fav})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.review_link:
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse((String) v.getTag()));
                startActivity(intent);
                break;
            case R.id.play_button:
            case R.id.video_thumb:
                if (v.getTag() != null) {
                    watchYoutubeVideo((String) v.getTag());
                }
                break;
            case R.id.fab_fav:
                favoriteFABClicked();
                break;
        }
    }

    private void favoriteFABClicked() {
        if (mMovie == null) {
            return;
        }
        mFavFAB.setEnabled(false);
        AsyncQueryHandler handler = new AsyncQueryHandler(getActivity().getContentResolver()) {
            @Override
            protected void onDeleteComplete(int token, Object cookie, int result) {
                super.onDeleteComplete(token, cookie, result);
                mFavFAB.setEnabled(true);
                updateFavoriteBtn();
                Log.v(LOGTAG, "delete complete");
            }

            @Override
            protected void onInsertComplete(int token, Object cookie, Uri uri) {
                super.onInsertComplete(token, cookie, uri);
                mFavFAB.setEnabled(true);
                updateFavoriteBtn();
                Log.v(LOGTAG, "insert complete");
            }
        };
        if (mMovie.isFavored()) {
            Log.v(LOGTAG, "delete movie");
            mMovie.setFavored(false);
            String where = MovieContract.MovieEntry._ID + "=?";
            String[] args = new String[]{String.valueOf(mMovie.getId())};
            handler.startDelete(-1, null, MovieContract.MovieEntry.CONTENT_URI, where, args);
        } else {
            Log.v(LOGTAG, "insert movie");
            mMovie.setFavored(true);
            handler.startInsert(-1, null, MovieContract.MovieEntry.CONTENT_URI, new Movie.Builder().movie(mMovie).build());
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Log.d(LOGTAG, "onCreateLoader isFavored:" + mMovie.isFavored());
        // Now create and return a CursorLoader that will take care of
        // creating a Cursor for the data being displayed.
        mFavFAB.setEnabled(false);
        return new CursorLoader(
                getActivity(),
                MovieContract.MovieEntry.buildMovieUri(mMovie.getId()),
                DETAIL_COLUMNS,
                null,
                null,
                null
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data != null && data.moveToFirst()) {
            long movieId = data.getLong(COL_MOVIE_ID);
            assert movieId == mMovie.getId();
            mMovie.setFavored(true);
        } else {
            mMovie.setFavored(false);
        }
        Log.d(LOGTAG, "onLoadFinished " + mMovie.getId() + " dataCount: " + data.getCount() + " f: " + mMovie.isFavored());
        updateFavoriteBtn();
        mFavFAB.setEnabled(true);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }

    private void watchYoutubeVideo(String id) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:" + id));
            startActivity(intent);
        } catch (ActivityNotFoundException ex) {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.youtube.com/watch?v=" + id));
            startActivity(intent);
        }
    }

    private Intent createShareTrailerIntent() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("text/plain");
        Video trailer = (Video) mPlayButton.getTag();
        String text = getResources().getString(R.string.share_template, trailer.getName(), " http://www.youtube.com/watch?v=" + trailer.getKey());
        shareIntent.putExtra(Intent.EXTRA_TEXT, text);
        return shareIntent;
    }

    public void setMovie(Movie movie) {
        if (movie.getId() == mMovie.getId()) {
            return;
        }
        mMovie = movie;
        updateTrailers(Collections.EMPTY_LIST);
        updateReviews(Collections.EMPTY_LIST);
        mPlayButton.setTag(null);
        updateMovieDetails();
    }

    private void updateShareBtn() {
        if (mMenuItemShare != null) {
            mMenuItemShare.setVisible(mPlayButton.getTag() != null);
        }
    }

    private void updateFavoriteBtn() {
        if (mMovie != null && mMovie.isFavored()) {
            mFavFAB.setImageResource(R.drawable.ic_favorite_white_48dp);
        } else {
            mFavFAB.setImageResource(R.drawable.ic_favorite_border_white_48dp);
        }
    }
}
