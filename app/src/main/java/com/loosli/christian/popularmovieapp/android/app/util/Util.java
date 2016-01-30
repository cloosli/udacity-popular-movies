package com.loosli.christian.popularmovieapp.android.app.util;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;

import com.loosli.christian.popularmovieapp.android.app.BuildConfig;

/**
 * Created by ChristianL on 30.11.15.
 * <p/>
 * Some parts are from vickychijwani,
 * repo https://github.com/vickychijwani/udacity-p1-p2-popular-movies/blob/master/app/src/main/java/me/vickychijwani/popularmovies/util/Util.java
 */
public class Util {

    public static final Uri THEMOVIEDB_BASE_URI = Uri.parse("http://api.themoviedb.org/3/");
    public static final Uri TMDB_IMAGE_BASE_URI = Uri.parse("http://image.tmdb.org/t/p/");

    private interface TMDbImageWidth {
        String getWidthString();

        int getMaxWidth();
    }

    public enum TMDbPosterWidth implements TMDbImageWidth {
        W92(92), W154(154), W185(185), W342(342), W500(500), W780(780), ORIGINAL(Integer.MAX_VALUE);

        public final int maxWidth;

        TMDbPosterWidth(int maxWidth) {
            this.maxWidth = maxWidth;
        }

        public int getMaxWidth() {
            return this.maxWidth;
        }

        public String getWidthString() {
            return (this == ORIGINAL) ? "original" : "w" + this.maxWidth;
        }
    }

    public enum TMDbBackdropWidth implements TMDbImageWidth {
        W300(300), W780(780), W1280(1280), ORIGINAL(Integer.MAX_VALUE);

        public final int maxWidth;

        TMDbBackdropWidth(int maxWidth) {
            this.maxWidth = maxWidth;
        }

        public int getMaxWidth() {
            return this.maxWidth;
        }

        public String getWidthString() {
            return (this == ORIGINAL) ? "original" : "w" + this.maxWidth;
        }
    }

    public static String buildPosterUrl(String posterPath, int posterWidth) {
        return buildImageUrl(posterPath, computeNextLowestPosterWidth(posterWidth));
    }

    public static String buildPosterUrl(String posterPath, TMDbPosterWidth tmdbPosterWidth) {
        return buildImageUrl(posterPath, tmdbPosterWidth);
    }

    public static String buildBackdropUrl(String backdropPath, int backdropWidth) {
        return buildImageUrl(backdropPath, computeNextLowestBackdropWidth(backdropWidth));
    }

    public static String buildBackdropUrl(String backdropPath, TMDbBackdropWidth tmdbBackdropWidth) {
        return buildImageUrl(backdropPath, tmdbBackdropWidth);
    }

    private static <T extends TMDbImageWidth> String buildImageUrl(String imagePath, T tmdbImageWidth) {
        if (BuildConfig.DEBUG) {
            Log.d("Picasso", "Loading image of width " + tmdbImageWidth.getMaxWidth() + "px");
        }
        String relativePath = tmdbImageWidth.getWidthString() + "/" + imagePath;
        return Uri.withAppendedPath(TMDB_IMAGE_BASE_URI, relativePath).toString();
    }

    public static int getScreenWidth(@NonNull Context context) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        wm.getDefaultDisplay().getMetrics(displayMetrics);
        return displayMetrics.widthPixels;
    }

    public static int multiplyColor(int srcColor, float factor) {
        int alpha = Color.alpha(srcColor);
        int red = (int) (Color.red(srcColor) * factor);
        int green = (int) (Color.green(srcColor) * factor);
        int blue = (int) (Color.blue(srcColor) * factor);
        return Color.argb(alpha, red, green, blue);
    }

    /**
     * This method converts dp unit to equivalent pixels, depending on device density.
     *
     * @param dp      A value in dp (density independent pixels) unit. Which we need to convert into pixels
     * @param context Context to get resources and device specific display metrics
     * @return A float value to represent px equivalent to dp depending on device density
     */
    public static float convertDpToPixel(float dp, Context context) {
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float px = dp * (metrics.densityDpi / 160f);
        return px;
    }

    /**
     * This method converts device specific pixels to density independent pixels.
     *
     * @param px      A value in px (pixels) unit. Which we need to convert into db
     * @param context Context to get resources and device specific display metrics
     * @return A float value to represent dp equivalent to px value
     */
    public static float convertPixelsToDp(float px, Context context) {
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float dp = px / (metrics.densityDpi / 160f);
        return dp;
    }

    // private methods

    // 50 => W92, 92 => W92, 93 => W185, 999 => ORIGINAL
    private static TMDbPosterWidth computeNextLowestPosterWidth(int posterWidth) {
        for (TMDbPosterWidth enumWidth : TMDbPosterWidth.values()) {
            if (0.8 * posterWidth <= enumWidth.maxWidth) {
                return enumWidth;
            }
        }
        return TMDbPosterWidth.ORIGINAL;
    }

    private static TMDbBackdropWidth computeNextLowestBackdropWidth(int backdropWidth) {
        for (TMDbBackdropWidth enumWidth : TMDbBackdropWidth.values()) {
            if (0.8 * backdropWidth <= enumWidth.maxWidth) {
                return enumWidth;
            }
        }
        return TMDbBackdropWidth.ORIGINAL;
    }

    public static boolean hasNetworkConnection(Context context) {
        boolean hasConnectedWifi = false;
        boolean hasConnectedMobile = false;

        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] netInfo = cm.getAllNetworkInfo();
        for (NetworkInfo ni : netInfo) {
            if (ni.getTypeName().equalsIgnoreCase("WIFI")) {
                if (ni.isConnected()) {
                    hasConnectedWifi = true;
                }
            }
            if (ni.getTypeName().equalsIgnoreCase("MOBILE")) {
                if (ni.isConnected()) {
                    hasConnectedMobile = true;
                }
            }
        }
        return hasConnectedWifi || hasConnectedMobile;
    }
}
