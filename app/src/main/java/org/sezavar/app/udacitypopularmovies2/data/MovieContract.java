package org.sezavar.app.udacitypopularmovies2.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.net.Uri;
import android.provider.BaseColumns;

import org.sezavar.app.udacitypopularmovies2.R;

/**
 * Created by amir on 10/16/15.
 */
public class MovieContract {
    public static final String CONTENT_AUTHORITY = "org.sezavar.app.udacitypopularmovies2";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_MOVIE = "movie";
    public static final String PATH_TRAILER = "trailer";


    public static final class MovieEntry implements BaseColumns {
        public static final String PATH_POPULAR_MOVIES = "popular";
        public static final String PATH_HIGH_RATED_MOVIES = "high-rated";
        public static final String PATH_FAVORITES_MOVIES = "favorite";
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_MOVIE).build();
        public static final Uri POPULAR_MOVIES_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_MOVIE).appendEncodedPath(PATH_POPULAR_MOVIES).build();
        public static final Uri HIGH_RATED_MOVIES_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_MOVIE).appendEncodedPath(PATH_HIGH_RATED_MOVIES).build();
        public static final Uri FAVORITE_MOVIES_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_MOVIE).appendEncodedPath(PATH_FAVORITES_MOVIES).build();
        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MOVIE;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MOVIE;

        public final static String TABLE_NAME = "movie";
        public final static String COLUMN_MDB_ID = "mdb_id";
        public final static String COLUMN_TITLE = "title";
        public final static String COLUMN_POSTER_PATH = "poster_path";
        public final static String COLUMN_OVERVIEW = "overview";
        public final static String COLUMN_USER_RATING = "user_rating";
        public final static String COLUMN_RELEASE_DATE = "release_date";
        public final static String COLUMN_POPULARITY = "popularity";
        public final static String COLUMN_FAVORITE = "favorite";

        public static Uri buildMovieUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static Uri buildMovieUri(String mdId) {
            return CONTENT_URI.buildUpon().appendEncodedPath(mdId).build();
        }
        public static Uri buildMovieUriBasedOnSortingField(String sortingOrder, Context context){
            Uri uri=null;
            if (sortingOrder.equals(context.getString(R.string.pref_sort_by_rating))) {
                uri = MovieContract.MovieEntry.HIGH_RATED_MOVIES_URI;
            } else if (sortingOrder.equals(context.getString(R.string.pref_sort_by_popularity))) {
                uri = MovieContract.MovieEntry.POPULAR_MOVIES_URI;
            } else {
                uri = MovieContract.MovieEntry.FAVORITE_MOVIES_URI;
            }
            return uri;
        }

        public static String getMovieMdIdFromUri(Uri uri) {
            return uri.getPathSegments().get(1);
        }
    }

    public static final class TrailerEntry implements BaseColumns {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_TRAILER).build();
        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_TRAILER;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_TRAILER;

        public final static String TABLE_NAME = "trailer";
        public final static String COLUMN_MOVIE_ID = "movie_id";
        public final static String COLUMN_TRAILER_PATH = "trailer_path";

        public static Uri buildTrailerUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static Uri buildTrailerMovie(long movieId) {
            return CONTENT_URI.buildUpon().appendPath(String.valueOf(movieId)).build();
        }

        public static String getMovieIdFromUri(Uri uri) {
            return uri.getPathSegments().get(1);
        }
    }
}
