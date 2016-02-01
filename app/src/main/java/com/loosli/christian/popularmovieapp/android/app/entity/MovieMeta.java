package com.loosli.christian.popularmovieapp.android.app.entity;

import android.content.ContentValues;

import com.loosli.christian.popularmovieapp.android.app.data.MovieContract;

/**
 * Created by ChristianL on 01.02.16.
 */
public interface MovieMeta {
    final class Builder {

        private final ContentValues values = new ContentValues();

        public Builder id(long id) {
            values.put(MovieContract.MovieEntry._ID, id);
            return this;
        }

        public Builder title(String title) {
            values.put(MovieContract.MovieEntry.COLUMN_TITLE, title);
            return this;
        }

        public Builder overview(String overview) {
            values.put(MovieContract.MovieEntry.COLUMN_OVERVIEW, overview);
            return this;
        }

        public Builder backdropPath(String backdropPath) {
            values.put(MovieContract.MovieEntry.COLUMN_BACKDROPPATH, backdropPath);
            return this;
        }

        public Builder posterPath(String posterPath) {
            values.put(MovieContract.MovieEntry.COLUMN_POSTERPATH, posterPath);
            return this;
        }

        public Builder rating(double voteCount) {
            values.put(MovieContract.MovieEntry.COLUMN_RATING, voteCount);
            return this;
        }

//        public Builder favored(boolean favored) {
//            values.put(MovieContract.MovieEntry.MOVIE_FAVORED, favored);
//            return this;
//        }

        public Builder releaseDate(long date) {
            values.put(MovieContract.MovieEntry.COLUMN_RELEASEDATE, date);
            return this;
        }

        public Builder movie(Movie movie) {
            return id(movie.getId())
                    .title(movie.getTitle())
                    .overview(movie.getOverview())
                    .backdropPath(movie.getBackdropPath())
                    .posterPath(movie.getPosterPath())
                    .rating(movie.getRating())
                    .releaseDate(MovieContract.normalizeDate(movie.getReleaseDate().getTime()));
//                    .favored(movie.isFavored());
        }

        public ContentValues build() {
            return values;
        }
    }
}
