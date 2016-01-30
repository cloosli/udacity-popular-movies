package com.loosli.christian.popularmovieapp.android.app;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.loosli.christian.popularmovieapp.android.app.entity.Movie;
import com.loosli.christian.popularmovieapp.android.app.util.Util;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.Calendar;

import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * A placeholder fragment containing a simple view.
 */
public class MovieDetailActivityFragment extends Fragment {

    private long mMovieId;

    public MovieDetailActivityFragment() {
    }

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

        updateTrailerList();
        updateReviewList();

        return rootView;
    }

    @OnClick(R.id.fab_fav)
    public void fabClicked(View view) {
        Toast.makeText(getActivity(), "fabClicked", Toast.LENGTH_SHORT).show();
    }
    private void updateReviewList() {

    }
    private void updateTrailerList() {
        FetchTrailerTask fetchTrailerTask = new FetchTrailerTask(getActivity());
        fetchTrailerTask.execute(Long.toString(mMovieId));
    }

    @Override public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }
}
