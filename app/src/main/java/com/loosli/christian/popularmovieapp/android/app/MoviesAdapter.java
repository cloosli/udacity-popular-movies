package com.loosli.christian.popularmovieapp.android.app;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.loosli.christian.popularmovieapp.android.app.entity.Movie;
import com.loosli.christian.popularmovieapp.android.app.util.Util;
import com.squareup.picasso.Picasso;

import java.util.Collection;
import java.util.List;

/**
 * Created by ChristianL on 25.11.15.
 */
public class MoviesAdapter extends BaseAdapter {

    private static final String LOG_TAG = MoviesAdapter.class.getSimpleName();
    private final Activity mContext;
    private final List<Movie> mMovies;
    //private final int mHeight;
    private final int mWidth;

    public MoviesAdapter(Activity context, List<Movie> movies) {
        mContext = context;
        mMovies = movies;
        //mHeight = Math.round(mContext.getResources().getDimension(R.dimen.poster_height));
        // TODO set height with a ratio from width
        //FIXME this works not if gridview set the column_width to 150dp, in landscape mode the column number is 3
        mWidth = Math.round(Util.getScreenWidth(mContext) / 2);
        //TODO: check connectivity speed
    }

    @Override
    public int getCount() {
        return mMovies.size();
    }

    @Override
    public Movie getItem(int position) {
        if (position >= 0 && position < mMovies.size()) {
            return mMovies.get(position);
        }
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Movie movie = getItem(position);
        if (movie == null) {
            return null;
        }

        ImageView imageView;
        if (convertView == null) {
            // if it's not recycled, initialize some attributes
            imageView = new ImageView(mContext);
            //imageView.setLayoutParams(new GridView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            //imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            //imageView.setScaleType(ImageView.ScaleType.FIT_START);
            imageView.setAdjustViewBounds(true);
            imageView.setPadding(0, 0, 0, 0);
        } else {
            imageView = (ImageView) convertView;
        }

        Picasso.with(mContext)
                .load(Util.buildPosterUrl(movie.getPosterPath(), mWidth))
                .placeholder(R.drawable.empty_photo)
                .into(imageView);

        return imageView;
    }

    public void addAll(Collection<Movie> xs) {
        mMovies.addAll(xs);
        notifyDataSetChanged();
    }

    public void clearData() {
        mMovies.clear();
    }

    public List<Movie> getItems() {
        return mMovies;
    }
}
