package org.sezavar.app.udacitypopularmovies2;

import java.text.SimpleDateFormat;

/**
 * Created by amir on 10/9/15.
 */
public class Constants {
    public static final String DISCOVERY_BASE_URL = "http://api.themoviedb.org/3/discover/movie";
    public static final SimpleDateFormat MOVIE_RELEASE_DATE_FORMAT_TO_READ = new SimpleDateFormat("yyyy-MM-dd");
    public static final SimpleDateFormat MOVIE_RELEASE_DATE_FORMAT_TO_WRITE = new SimpleDateFormat("yyyy");


    public final static String MOVIE_POSTER_BASE_URL = "http://image.tmdb.org/t/p/w185";
    public final static String MOVIE_TRAILERS_BASE_URL="http://api.themoviedb.org/3/movie";
    public final static String YOUTUBE_BASE_URL="http://www.youtube.com/watch?v=";
    public static final int MAX_NUMBER_OF_SHOWN_MOVIES = 36;
    public static final String DETAIL_URI_KEY = "URI";
}
