package com.loosli.christian.popularmovieapp.android.app.api;

import com.google.gson.GsonBuilder;
import com.loosli.christian.popularmovieapp.android.app.BuildConfig;
import com.loosli.christian.popularmovieapp.android.app.entity.Movie;
import com.loosli.christian.popularmovieapp.android.app.entity.Review;
import com.loosli.christian.popularmovieapp.android.app.entity.Video;
import com.loosli.christian.popularmovieapp.android.app.util.Util;

import retrofit2.Call;
import retrofit2.GsonConverterFactory;
import retrofit2.Retrofit;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Created by ChristianL on 30.01.16.
 */
public final class TheMovieDBService {

    private static final String APPAPI_KEY = "api_key=" + BuildConfig.THE_MOVIE_DB_API_KEY;

    private static GsonBuilder gsonBuilder = new GsonBuilder().setDateFormat("yyyy-MM-dd");

    public static Retrofit getRetrofitBuild() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Util.THEMOVIEDB_BASE_URI.toString())
                .addConverterFactory(GsonConverterFactory.create(gsonBuilder.create()))
                .build();
        return retrofit;
    }

    public interface TMDBAPI {

        @GET("discover/movie?" + APPAPI_KEY)
        Call<Movie.Response> getMovies(
                @Query("sort_by") String sortBy,
                @Query("page") int page,
                @Query("vote_count.gte") int voteCount
        );

        @GET("movie/{movieId}/videos?" + APPAPI_KEY)
        Call<Video.Response> getVideos(@Path("movieId") String movieId);

        @GET("movie/{movieId}/reviews?" + APPAPI_KEY)
        Call<Review.Response> getReviews(@Path("movieId") String movieId);
    }
}
