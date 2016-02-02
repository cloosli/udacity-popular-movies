package com.loosli.christian.popularmovieapp.android.app.entity;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import java.util.Date;
import java.util.List;

/**
 * Created by ChristianL on 29.11.15.
 */
public final class Movie implements MovieMeta, Parcelable {

    private long id;
    private String title;
    private String overview;

    @SerializedName("poster_path")
    private String posterPath;
    @SerializedName("backdrop_path")
    private String backdropPath;
    @SerializedName("vote_average")
    private float rating;
    @SerializedName("release_date")
    private Date releaseDate;
    @SerializedName("original_language")
    private String originalLanguage;

    private boolean adult;

    private boolean favored = false;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getOverview() {
        return overview;
    }

    public void setOverview(String overview) {
        this.overview = overview;
    }

    public String getPosterPath() {
        return posterPath;
    }

    public void setPosterPath(String posterPath) {
        this.posterPath = posterPath;
    }

    public String getBackdropPath() {
        return backdropPath;
    }

    public void setBackdropPath(String backdropPath) {
        this.backdropPath = backdropPath;
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public Date getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(Date releaseDate) {
        this.releaseDate = releaseDate;
    }

    public boolean isFavored() {
        return favored;
    }

    public void setFavored(boolean favored) {
        this.favored = favored;
    }

    public String getOriginalLanguage() {
        return originalLanguage;
    }

    public void setOriginalLanguage(String originalLanguage) {
        this.originalLanguage = originalLanguage;
    }

    public boolean isAdult() {
        return adult;
    }

    public void setAdult(boolean adult) {
        this.adult = adult;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(title);
        dest.writeString(overview);
        dest.writeString(posterPath);
        dest.writeString(backdropPath);
        dest.writeFloat(rating);
        dest.writeLong(releaseDate.getTime());
        dest.writeInt(adult ? 1 : 0);
        dest.writeString(originalLanguage);
    }

    public static final Parcelable.Creator<Movie> CREATOR = new Parcelable.Creator<Movie>() {
        @Override
        public Movie createFromParcel(Parcel source) {
            Movie movie = new Movie();
            movie.setId(source.readLong());
            movie.setTitle(source.readString());
            movie.setOverview(source.readString());
            movie.setPosterPath(source.readString());
            movie.setBackdropPath(source.readString());
            movie.setRating(source.readFloat());
            Date releaseDate = new Date();
            releaseDate.setTime(source.readLong());
            movie.setReleaseDate(releaseDate);
            movie.setAdult(source.readInt() == 1 ? true : false);
            movie.setOriginalLanguage(source.readString());
            return movie;
        }

        @Override
        public Movie[] newArray(int size) {
            return new Movie[size];
        }
    };

    public static final class Response {
        public int page;
        public int total_results;
        public int total_pages;

        @SerializedName("results")
        public List<Movie> movies;
    }
}


