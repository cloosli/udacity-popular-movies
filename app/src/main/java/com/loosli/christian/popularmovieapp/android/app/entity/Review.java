package com.loosli.christian.popularmovieapp.android.app.entity;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by ChristianL on 30.01.16.
 * {
 * id: 286217,
 * page: 1,
 * results: [{
 * id: "5619f70d9251415d3100129c",
 * author: "Frank Ochieng",
 * content: "'The Martian’ is definitely in the.....",
 * url: "http://j.mp/1OtCJjC"
 * }],
 * total_pages: 1,
 * total_results: 1
 * }
 */
public class Review implements Parcelable {

    private String id;
    private String author;
    private String content;
    private String url;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.id);
        dest.writeString(this.author);
        dest.writeString(this.content);
        dest.writeString(this.url);
    }

    protected Review(Parcel in) {
        this.id = in.readString();
        this.author = in.readString();
        this.content = in.readString();
        this.url = in.readString();
    }

    public static final Parcelable.Creator<Review> CREATOR = new Parcelable.Creator<Review>() {
        public Review createFromParcel(Parcel source) {
            return new Review(source);
        }

        public Review[] newArray(int size) {
            return new Review[size];
        }
    };

    public static final class Response {
        @Expose
        public long id;

        @Expose
        @SerializedName("results")
        public List<Review> reviews;
    }
}
