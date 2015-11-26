package com.loosli.christian.popularmovieapp.android.app;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by ChristianL on 25.11.15.
 */
public class MoviesAdapter extends BaseAdapter {

    private final Activity mContext;
    private final List<String> mMovies;
    private final int mHeight;
    private final int mWidth;
    private String mBasePosterUrl;

    public MoviesAdapter(Activity context) {
        mContext = context;
        mMovies = new ArrayList<String>();
        mHeight = Math.round(mContext.getResources().getDimension(R.dimen.poster_height));
        mWidth = Math.round(mContext.getResources().getDimension(R.dimen.poster_width));
        int posterSize = 185; //342;//(int) mContext.getResources().getDimension(R.dimen.poster_size);
        mBasePosterUrl = "http://image.tmdb.org/t/p/w" + posterSize + "/";
    }

    public void addAll(Collection<String> xs) {
        mMovies.addAll(xs);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mMovies.size();
    }

    @Override
    public String getItem(int position) {
        if (position < 0 || position >= mMovies.size()) {
            return null;
        }
        return mMovies.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        String movie = getItem(position);
        if (movie == null) {
            return null;
        }

        ImageView imageView;
        if (convertView == null) {
            // if it's not recycled, initialize some attributes
            imageView = new ImageView(mContext);
            imageView.setLayoutParams(new GridView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            //imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            imageView.setScaleType(ImageView.ScaleType.FIT_START);
            imageView.setAdjustViewBounds(true);
        } else {
            imageView = (ImageView) convertView;
        }
        //Uri posterUri = Uri.parse(movie).buildUpon().build();
        Picasso.with(mContext)
                .load(mBasePosterUrl + movie)
                .placeholder(R.drawable.empty_photo)
                        //.fit().centerInside()
                .into(imageView);

        return imageView;
    }
}
