package com.loosli.christian.popularmovieapp.android.app.entity;

import java.util.List;

/**
 * Created by ChristianL on 30.01.16.
 */
public class TMDBReviews {
//    {
//        id: 286217,
//                page: 1,
//            results: [{
//        id: "5619f70d9251415d3100129c",
//                author: "Frank Ochieng",
//                content: "'The Martian’ is definitely in the.....",
//                url: "http://j.mp/1OtCJjC"
//    }],
//        total_pages: 1,
//                total_results: 1
//    }

    private List<Item> results;

    public List<Item> getResults() {
        return results;
    }

    public void setResults(List<Item> results) {
        this.results = results;
    }

    public static class Item {
        private String author;
        private String content;
        private String url;

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
    }
}
