package com.loosli.christian.popularmovieapp.android.app;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.loosli.christian.popularmovieapp.android.app.entity.Movie;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;

/**
 * A placeholder fragment containing a simple view.
 */
public class MovieDetailActivityFragment extends Fragment {

    public MovieDetailActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_movie_detail, container, false);
        // The detail Activity called via intent.  Inspect the intent for movie data.

        int imageWidth = getActivity().getResources().getDimensionPixelSize(R.dimen.movie_thumb_width);
        int imageHeight = getActivity().getResources().getDimensionPixelSize(R.dimen.movie_thumb_height);

        Intent intent = getActivity().getIntent();
        if (intent != null && intent.hasExtra(BundleKeys.MOVIE)) {
            Movie movie = intent.getParcelableExtra(BundleKeys.MOVIE);
            getActivity().setTitle(movie.getTitle());
            ((TextView) rootView.findViewById(R.id.movie_detail_titel)).setText(movie.getTitle());
            ((TextView) rootView.findViewById(R.id.movie_detail_description)).setText(movie.getOverview());
            String voteAverage = String.format(getString(R.string.md_vote_average), movie.getRating());
            ((TextView) rootView.findViewById(R.id.movie_detail_vote_average)).setText(voteAverage);
            DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(getActivity());
            ((TextView) rootView.findViewById(R.id.movie_detail_release_date)).setText(getString(R.string.released) +
                    dateFormat.format(movie.getReleaseDate()));

            Picasso.with(getActivity())
                    .load("http://image.tmdb.org/t/p/w185/" + movie.getPosterPath())
                    .resize(imageWidth, imageHeight)
                    .into((ImageView) rootView.findViewById(R.id.movie_detail_poster));

            Picasso.with(getActivity())
                    .load("http://image.tmdb.org/t/p/w185/" + movie.getBackdropPath())
                    .into((ImageView) rootView.findViewById(R.id.backdrop));
        }
        return rootView;
    }
}
