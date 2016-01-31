package com.loosli.christian.popularmovieapp.android.app;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.loosli.christian.popularmovieapp.android.app.entity.Movie;
import com.loosli.christian.popularmovieapp.android.app.entity.TMDBReviews;
import com.loosli.christian.popularmovieapp.android.app.entity.TMDBVideos;
import com.loosli.christian.popularmovieapp.android.app.util.Util;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
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
public class MovieDetailActivityFragment extends Fragment implements View.OnClickListener {
    private static final String LOGTAG = MovieDetailActivityFragment.class.getSimpleName();

    private long mMovieId;
    private TMDBVideos.TMDBItem mFirstTrailer;

    public MovieDetailActivityFragment() {
    }

    @Bind(R.id.detail_movie_videos)
    LinearLayout mVideosLayout;

    @Bind(R.id.detail_movie_reviews)
    LinearLayout mReviewsLayout;

    @Bind(R.id.fab_trailer)
    FloatingActionButton trailerFAB;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_movie_detail, container, false);
        ButterKnife.bind(this, rootView);
        // The detail Activity called via intent.  Inspect the intent for movie data.

        int imageWidth = getActivity().getResources().getDimensionPixelSize(R.dimen.movie_thumb_width);
        int imageHeight = getActivity().getResources().getDimensionPixelSize(R.dimen.movie_thumb_height);

        Movie movie = null;
        Intent intent = getActivity().getIntent();
        if (intent != null && intent.hasExtra(BundleKeys.MOVIE)) {
            movie = intent.getParcelableExtra(BundleKeys.MOVIE);
        }
        if (movie == null) {
            throw new IllegalStateException("no given movie!");
        }

        mMovieId = movie.getId();

        Picasso picasso = Picasso.with(getActivity());
        ImageView poster = (ImageView) rootView.findViewById(R.id.poster);
        ImageView backdrop = (ImageView) rootView.findViewById(R.id.backdrop);

        int posterWidth = getResources().getDimensionPixelSize(R.dimen.details_poster_width);
        int posterHeight = getResources().getDimensionPixelSize(R.dimen.details_poster_height);
        picasso.load(Util.buildPosterUrl(movie.getPosterPath(), posterWidth))
                .resize(posterWidth, posterHeight)
                .centerCrop()
                .noPlaceholder()
                .into(poster);

        int backdropWidth = Util.getScreenWidth(getActivity());
        int backdropHeight = getResources().getDimensionPixelSize(R.dimen.details_backdrop_height);
        picasso.load(Util.buildBackdropUrl(movie.getBackdropPath(), backdropWidth))
                .resize(backdropWidth, backdropHeight)
                .centerCrop()
                .noPlaceholder()
                .into(backdrop);

        getActivity().setTitle(movie.getTitle());

        ((TextView) rootView.findViewById(R.id.title)).setText(movie.getTitle());
        ((TextView) rootView.findViewById(R.id.synopsis)).setText(movie.getOverview());

        String voteAverage = String.format("%1$2.1f", movie.getRating());

        ((TextView) rootView.findViewById(R.id.rating)).setText(voteAverage);
        DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(getActivity());
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(movie.getReleaseDate());

        ((TextView) rootView.findViewById(R.id.release_date)).setText(Integer.toString(calendar.get(Calendar.YEAR)));
        //getString(R.string.released) + dateFormat.format(movie.getReleaseDate()));

        TheMovieDBService.TMDBAPI tmdbapi = TheMovieDBService.getRetrofitBuild().create(TheMovieDBService.TMDBAPI.class);

        updateTrailerList(tmdbapi);
        updateReviewList(tmdbapi);

        return rootView;
    }

    @OnClick(R.id.fab_fav)
    public void fabClicked(View view) {
        Toast.makeText(getActivity(), "fabClicked", Toast.LENGTH_SHORT).show();
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
        Call<TMDBReviews> call = tmdbapi.getReviews(Long.toString(mMovieId));
        call.enqueue(new Callback<TMDBReviews>() {
            @Override
            public void onResponse(Response<TMDBReviews> response) {
                try {
                    TMDBReviews reviewResult = response.body();
                    List<TMDBReviews.Item> reviews = reviewResult.getResults();
                    addReviews(reviews);
                } catch (NullPointerException e) {

                }
            }

            @Override
            public void onFailure(Throwable t) {
                Log.e(LOGTAG, "getReviews threw: " + t.getMessage(), t);
            }
        });
    }

    private void updateTrailerList(TheMovieDBService.TMDBAPI tmdbapi) {
        Call<TMDBVideos> call = tmdbapi.getVideos(Long.toString(mMovieId));
        call.enqueue(new Callback<TMDBVideos>() {
            @Override
            public void onResponse(Response<TMDBVideos> response) {
                try {
                    TMDBVideos videoResult = response.body();
                    List<TMDBVideos.TMDBItem> videos = videoResult.getResults();
                    addTrailers(videos);
                } catch (NullPointerException e) {
                    Log.e(LOGTAG, "" + response.raw().body().toString(), e);
                    Toast.makeText(getActivity(), response.message() + " code: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Throwable t) {
                Log.e(LOGTAG, "getVideos threw: " + t.getMessage(), t);
            }
        });
    }

    private void addTrailers(List<TMDBVideos.TMDBItem> trailers) {
        mVideosLayout.removeAllViews();
        trailerFAB.setTag(null);
        LayoutInflater inflater = getActivity().getLayoutInflater();
        Picasso picasso = Picasso.with(getActivity());
        for (TMDBVideos.TMDBItem trailer : trailers) {
            if (!trailer.isYouTube()) {
                Log.i(LOGTAG, "no youtube trailer");
                continue;
            }
            if (trailerFAB.getTag() == null) {
                trailerFAB.setTag(trailer.getKey());
            }
            ViewGroup thumbContainer = (ViewGroup) inflater.inflate(R.layout.detail_movie_video, mVideosLayout, false);
            ImageView thumbView = (ImageView) thumbContainer.findViewById(R.id.video_thumb);
            thumbView.setTag(trailer.getKey());
            thumbView.setOnClickListener(this);
            picasso.load(TMDBVideos.TMDBItem.getThumbnailUrl(trailer))
                    .resizeDimen(R.dimen.video_width, R.dimen.video_height)
                    .centerCrop()
                    .placeholder(R.drawable.thumbnail_placeholder)
                    .into(thumbView);
            mVideosLayout.addView(thumbContainer);
        }
    }

    private void addReviews(List<TMDBReviews.Item> reviews) {
        mReviewsLayout.removeAllViews();
        LayoutInflater inflater = getActivity().getLayoutInflater();
        for (TMDBReviews.Item review : reviews) {
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
}
