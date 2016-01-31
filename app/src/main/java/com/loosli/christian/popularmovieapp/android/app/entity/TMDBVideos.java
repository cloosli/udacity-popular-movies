package com.loosli.christian.popularmovieapp.android.app.entity;

import java.util.List;

/**
 * Created by ChristianL on 30.01.16.
 */
public class TMDBVideos {
// result from tmdv
//    {
//        id: 286217,
//                results: [
//        {
//            id: "5641ebe392514128a9002ccb",
//                    iso_639_1: "en",
//                key: "8E8N8EKbpV4",
//                name: "The Martian Official Trailer 1 HD",
//                site: "YouTube",
//                size: 1080,
//                type: "Trailer"
//        }
//        ]
//    }

    private long id;
    private List<TMDBItem> results;

    public List<TMDBItem> getResults() {
        return results;
    }

    public void setResults(List<TMDBItem> results) {
        this.results = results;
    }

    public static class TMDBItem {
        public static final String TYPE_TRAILER = "Trailer";
        public static final String SITE_YOUTUBE = "YouTube";

        public static final String YOUTUBE_VND_URL = "vnd.youtube:"; //+id
        public static final String YOUTUBE_WEB_URL = "http://www.youtube.com/watch?v=";//+id

        private String id;
        private String key;
        private String name;
        private String type;
        private String site;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getSite() {
            return site;
        }

        public void setSite(String site) {
            this.site = site;
        }

        public boolean isYouTube() {
            return "YouTube".equals(getSite());
        }

        /**
         * @return the thumbnail url:
         * http://img.youtube.com/vi/8E8N8EKbpV4/0.jpg
         */
        public static String getThumbnailUrl(TMDBItem video) {
            if (video.isYouTube()) {
                return String.format("http://img.youtube.com/vi/%1$s/0.jpg", video.getKey());
            } else {
                throw new UnsupportedOperationException("Only YouTube is supported!");
            }
        }
    }
}
