package com.loosli.christian.popularmovieapp.android.app;

import android.content.ActivityNotFoundException;
import android.content.AsyncQueryHandler;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.loosli.christian.popularmovieapp.android.app.data.MovieContract;
import com.loosli.christian.popularmovieapp.android.app.entity.Movie;
import com.loosli.christian.popularmovieapp.android.app.entity.Review;
import com.loosli.christian.popularmovieapp.android.app.entity.Video;
import com.loosli.christian.popularmovieapp.android.app.util.Util;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Calendar;
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
public class MovieDetailActivityFragment extends Fragment implements View.OnClickListener, LoaderManager.LoaderCallbacks<Cursor> {
    private static final String LOGTAG = MovieDetailActivityFragment.class.getSimpleName();

    public static final String ARG_MOVIE = "arg_movie";
    private static final String STATE_REVIEWS = "state_reviews";
    private static final String STATE_VIDEOS = "state_trailers";
    private static final int DETAIL_LOADER = 0;

    private static final String[] DETAIL_COLUMNS = {
            MovieContract.MovieEntry.TABLE_NAME + "." + MovieContract.MovieEntry._ID,
    };
    static final int COL_MOVIE_ID = 0;

    private Movie mMovie;
    private List<Video> mVideos;
    private List<Review> mReviews;
    private Video mFirstTrailer;

    @Bind(R.id.detail_movie_videos)
    LinearLayout mVideosLayout;

    @Bind(R.id.detail_movie_reviews)
    LinearLayout mReviewsLayout;

    @Bind(R.id.fab_trailer)
    FloatingActionButton mTrailerFAB;

    @Bind(R.id.fab_fav)
    FloatingActionButton mFavFAB;

    @Bind(R.id.poster)
    ImageView mPoster;

    @Bind(R.id.backdrop)
    ImageView mBackdrop;

    public MovieDetailActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.i(LOGTAG, "onCreateView");
        View rootView = inflater.inflate(R.layout.fragment_movie_detail, container, false);
        ButterKnife.bind(this, rootView);
        // The detail Activity called via intent.  Inspect the intent for movie data.
        Intent intent = getActivity().getIntent();
        if (intent != null && intent.hasExtra(BundleKeys.MOVIE)) {
            mMovie = intent.getParcelableExtra(BundleKeys.MOVIE);
        }
        if (mMovie == null) {
            throw new IllegalStateException("no given movie!");
        }

        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(STATE_VIDEOS)) {
                mVideos = savedInstanceState.getParcelableArrayList(STATE_VIDEOS);
            }
            if (savedInstanceState.containsKey(STATE_REVIEWS)) {
                mReviews = savedInstanceState.getParcelableArrayList(STATE_REVIEWS);
            }
        }

        int posterWidth = getResources().getDimensionPixelSize(R.dimen.details_poster_width);
        int posterHeight = getResources().getDimensionPixelSize(R.dimen.details_poster_height);

        Picasso picasso = Picasso.with(getActivity());
        picasso.load(Util.buildPosterUrl(mMovie.getPosterPath(), posterWidth))
                .resize(posterWidth, posterHeight)
                .centerCrop()
//                .centerInside()
                .noPlaceholder()
                .into(mPoster);

        int backdropWidth = Util.getScreenWidth(getActivity());
        int backdropHeight = getResources().getDimensionPixelSize(R.dimen.details_backdrop_height);
        Log.d(LOGTAG, "getBackdropUrl for dimension: w" + backdropWidth + " * h" + backdropHeight);
        picasso.load(Util.buildBackdropUrl(mMovie.getBackdropPath(), backdropWidth))
                .resize(backdropWidth, backdropHeight)
                .centerCrop()
                .noPlaceholder()
                .into(mBackdrop);

        getActivity().setTitle(mMovie.getTitle());

        ((TextView) rootView.findViewById(R.id.title)).setText(mMovie.getTitle());
        ((TextView) rootView.findViewById(R.id.synopsis)).setText(mMovie.getOverview());

        String voteAverage = String.format("%1$2.1f", mMovie.getRating());

        ((TextView) rootView.findViewById(R.id.rating)).setText(voteAverage);
//        DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(getActivity());
//        getString(R.string.released) + dateFormat.format(mMovie.getReleaseDate()));
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(mMovie.getReleaseDate());
        ((TextView) rootView.findViewById(R.id.release_date)).setText(Integer.toString(calendar.get(Calendar.YEAR)));

        mFavFAB.setVisibility(View.INVISIBLE);
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.i(LOGTAG, "onActivityCreated");
        getLoaderManager().initLoader(DETAIL_LOADER, null, this);
//        onMovieLoaded(getArguments().getParcelable(ARG_MOVIE));
//        if (mReviews != null) onReviewsLoaded(mReviews);
//        else loadReviews();
//        if (mVideos != null) onVideosLoaded(mVideos);
//        else loadVideos();
        TheMovieDBService.TMDBAPI tmdbapi = TheMovieDBService.getRetrofitBuild().create(TheMovieDBService.TMDBAPI.class);
        boolean connected = true; //Util.hasNetworkConnection(getActivity());
        if (mVideos == null && connected) {
            updateTrailerList(tmdbapi);
        }
        if (mReviews == null && connected) {
            updateReviewList(tmdbapi);
        }
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

    @OnClick(R.id.fab_fav)
    public void fabClicked(FloatingActionButton fab) {
        mFavFAB.setEnabled(false);
        AsyncQueryHandler handler = new AsyncQueryHandler(getActivity().getContentResolver()) {
            @Override
            protected void onDeleteComplete(int token, Object cookie, int result) {
                super.onDeleteComplete(token, cookie, result);
                mFavFAB.setEnabled(true);
                Toast.makeText(getActivity(), "Delete complete", Toast.LENGTH_SHORT).show();
            }

            @Override
            protected void onInsertComplete(int token, Object cookie, Uri uri) {
                super.onInsertComplete(token, cookie, uri);
                mFavFAB.setEnabled(true);
                Toast.makeText(getActivity(), "Insert complete", Toast.LENGTH_SHORT).show();
            }
        };
        if (mMovie.isFavored()) {
            Toast.makeText(getActivity(), "delete movie", Toast.LENGTH_SHORT).show();
            mMovie.setFavored(false);
            String where = MovieContract.MovieEntry._ID + "=?";
            String[] args = new String[]{String.valueOf(mMovie.getId())};
            handler.startDelete(-1, null, MovieContract.MovieEntry.CONTENT_URI, where, args);
        } else {
            Toast.makeText(getActivity(), "insert movie", Toast.LENGTH_SHORT).show();
            mMovie.setFavored(true);
            fab.setImageResource(R.drawable.ic_favorite_white_48dp);
            handler.startInsert(-1, null, MovieContract.MovieEntry.CONTENT_URI, new Movie.Builder().movie(mMovie).build());
        }
    }

    public void watchYoutubeVideo(String id) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:" + id));
            startActivity(intent);
        } catch (ActivityNotFoundException ex) {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.youtube.com/watch?v=" + id));
            startActivity(intent);
        }
    }

    private void updateReviewList(TheMovieDBService.TMDBAPI tmdbapi) {
        Call<Review.Response> call = tmdbapi.getReviews(Long.toString(mMovie.getId()));
        call.enqueue(new Callback<Review.Response>() {
            @Override
            public void onResponse(Response<Review.Response> response) {
                try {
                    Review.Response reviewResult = response.body();
                    List<Review> reviews = reviewResult.reviews;
                    addReviews(reviews);
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

    private void updateTrailerList(TheMovieDBService.TMDBAPI tmdbapi) {
        Call<Video.Response> call = tmdbapi.getVideos(Long.toString(mMovie.getId()));
        call.enqueue(new Callback<Video.Response>() {
            @Override
            public void onResponse(Response<Video.Response> response) {
                try {
                    Video.Response videoResult = response.body();
                    List<Video> videos = videoResult.videos;
                    addTrailers(videos);
                } catch (NullPointerException e) {
                    Log.e(LOGTAG, "NullPointerException: " + response.raw().body().toString(), e);
                    Toast.makeText(getActivity(), response.message() + " code: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Throwable t) {
                Log.e(LOGTAG, "getVideos threw: " + t.getMessage(), t);
            }
        });
    }

    private void addTrailers(List<Video> trailers) {
        mVideosLayout.removeAllViews();
        mTrailerFAB.setTag(null);
        LayoutInflater inflater = getActivity().getLayoutInflater();
        Picasso picasso = Picasso.with(getActivity());
        for (Video trailer : trailers) {
            if (!trailer.isYouTube()) {
                Log.i(LOGTAG, "no youtube trailer");
                continue;
            }
            if (mTrailerFAB.getTag() == null) {
                mTrailerFAB.setTag(trailer.getKey());
            }
            ViewGroup thumbContainer = (ViewGroup) inflater.inflate(R.layout.detail_movie_video, mVideosLayout, false);
            ImageView thumbView = (ImageView) thumbContainer.findViewById(R.id.video_thumb);
            thumbView.setTag(trailer.getKey());
            thumbView.setOnClickListener(this);
            picasso.load(Video.getThumbnailUrl(trailer))
                    .resizeDimen(R.dimen.video_width, R.dimen.video_height)
                    .centerCrop()
                    .placeholder(R.drawable.thumbnail_placeholder)
                    .into(thumbView);
            mVideosLayout.addView(thumbContainer);
        }
    }

    private void addReviews(List<Review> reviews) {
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
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    @Override
    @OnClick(R.id.fab_trailer)
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.review_link:
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse((String) v.getTag()));
                startActivity(intent);
                break;
            case R.id.fab_trailer:
            case R.id.video_thumb:
                watchYoutubeVideo((String) v.getTag());
                break;
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Log.v(LOGTAG, "In onCreateLoader");
        // Now create and return a CursorLoader that will take care of
        // creating a Cursor for the data being displayed.
        if (!mMovie.isFavored()) {
            return new CursorLoader(
                    getActivity(),
                    MovieContract.MovieEntry.buildMovieUri(mMovie.getId()),
                    DETAIL_COLUMNS,
                    null,
                    null,
                    null
            );
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data != null && data.moveToFirst()) {
            mMovie.setFavored(true);
            mFavFAB.setImageResource(R.drawable.ic_favorite_white_48dp);
            Toast.makeText(getActivity(), "found movie in db id: " + data.getString(COL_MOVIE_ID), Toast.LENGTH_SHORT).show();
        } else {
            mFavFAB.setImageResource(R.drawable.ic_favorite_border_white_48dp);
            Toast.makeText(getActivity(), "no movie found in db", Toast.LENGTH_SHORT).show();
        }
        mFavFAB.setVisibility(View.VISIBLE);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }
}
