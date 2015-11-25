package com.loosli.christian.popularmovieapp.android.app;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import java.util.Arrays;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {

    private String[] data = new String[]{
            "http://lorempixel.com/300/400/abstract/1",
            "http://lorempixel.com/300/400/abstract/2",
            "http://lorempixel.com/300/400/abstract/3",
            "http://lorempixel.com/300/400/abstract/4",
            "http://lorempixel.com/300/400/abstract/5",
            "http://lorempixel.com/300/400/abstract/6",
            "http://lorempixel.com/300/400/abstract/7",
            "http://lorempixel.com/300/400/abstract/8",
            "http://lorempixel.com/300/400/abstract/9",
            "http://lorempixel.com/300/400/abstract/10"
    };
    private MoviesAdapter mMoviesAdapter;

    public MainActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        mMoviesAdapter = new MoviesAdapter(getActivity());
        mMoviesAdapter.addAll(Arrays.asList(data));

        GridView gridView = (GridView) rootView.findViewById(R.id.movies_gridview);
        gridView.setAdapter(mMoviesAdapter);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String movie = mMoviesAdapter.getItem(position);
                Toast.makeText(getActivity(), movie + " " + position, Toast.LENGTH_SHORT).show();
            }
        });
        return rootView;
    }
}
