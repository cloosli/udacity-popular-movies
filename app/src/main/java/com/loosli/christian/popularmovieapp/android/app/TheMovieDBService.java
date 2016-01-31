package com.loosli.christian.popularmovieapp.android.app;

import com.loosli.christian.popularmovieapp.android.app.entity.TMDBReviews;
import com.loosli.christian.popularmovieapp.android.app.entity.TMDBVideos;
import com.loosli.christian.popularmovieapp.android.app.util.Util;

import retrofit2.Call;
import retrofit2.GsonConverterFactory;
import retrofit2.Retrofit;
import retrofit2.http.GET;
import retrofit2.http.Path;

/**
 * Created by ChristianL on 30.01.16.
 */
public final class TheMovieDBService {

    private static final String APPAPI_KEY = "api_key=" + BuildConfig.THE_MOVIE_DB_API_KEY;

    public static Retrofit getRetrofitBuild() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Util.THEMOVIEDB_BASE_URI.toString())
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        return retrofit;
    }

    public interface TMDBAPI {
        @GET("movie/{movieId}/videos?" + APPAPI_KEY)
        Call<TMDBVideos> getVideos(@Path("movieId") String movieId);

        @GET("movie/{movieId}/reviews?" + APPAPI_KEY)
        Call<TMDBReviews> getReviews(@Path("movieId") String movieId);
    }
}
