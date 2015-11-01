package org.sezavar.app.udacitypopularmovies2.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.Nullable;

import org.sezavar.app.udacitypopularmovies2.Constants;
import org.sezavar.app.udacitypopularmovies2.Movie;

/**
 * Created by amir on 10/16/15.
 */
public class MovieProvider extends ContentProvider {
    static final int MOVIE = 100;
    static final int MOVIE_POPULARITY = 101;
    static final int MOVIE_RATING = 102;
    static final int MOVIE_FAVORITES = 103;
    static final int MOVIE_WITH_MD_ID = 104;
    static final int TRAILER = 201;
    static final int TRAILER_WITH_MOVIE_ID = 202;
    private static final UriMatcher sUriMatcher = buildUriMatcher();

    static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = MovieContract.CONTENT_AUTHORITY;

        matcher.addURI(authority, MovieContract.PATH_MOVIE, MOVIE);
        matcher.addURI(authority, MovieContract.PATH_MOVIE + "/" + MovieContract.MovieEntry.PATH_POPULAR_MOVIES, MOVIE_POPULARITY);
        matcher.addURI(authority, MovieContract.PATH_MOVIE + "/" + MovieContract.MovieEntry.PATH_HIGH_RATED_MOVIES, MOVIE_RATING);
        matcher.addURI(authority, MovieContract.PATH_MOVIE + "/" + MovieContract.MovieEntry.PATH_FAVORITES_MOVIES, MOVIE_FAVORITES);
        matcher.addURI(authority, MovieContract.PATH_MOVIE + "/*", MOVIE_WITH_MD_ID);
        matcher.addURI(authority, MovieContract.PATH_TRAILER, TRAILER);
        matcher.addURI(authority, MovieContract.PATH_TRAILER + "/*", TRAILER_WITH_MOVIE_ID);

        return matcher;
    }

    private static final SQLiteQueryBuilder sTrailerByMovieSettingQueryBuilder;
    //movie.movie_id = ?
    private static final String sMovieIdSelection =
            MovieContract.MovieEntry.TABLE_NAME +
                    "." + MovieContract.MovieEntry._ID +
                    " = ? ";
    //movie.mdb_id = ?
    private static final String sMovieMdIdSelection =
            MovieContract.MovieEntry.TABLE_NAME +
                    "." + MovieContract.MovieEntry.COLUMN_MDB_ID +
                    " = ? ";
    //movie.favorite=?
    private static final String sFavoriteMovieSelection =
            MovieContract.MovieEntry.TABLE_NAME +
                    "." + MovieContract.MovieEntry.COLUMN_FAVORITE +
                    " = ? ";


    static {
        sTrailerByMovieSettingQueryBuilder = new SQLiteQueryBuilder();
        sTrailerByMovieSettingQueryBuilder.setTables(
                MovieContract.MovieEntry.TABLE_NAME + " LEFT JOIN " +
                        MovieContract.TrailerEntry.TABLE_NAME +
                        " ON " + MovieContract.TrailerEntry.TABLE_NAME +
                        "." + MovieContract.TrailerEntry.COLUMN_MOVIE_ID +
                        " = " + MovieContract.MovieEntry.TABLE_NAME +
                        "." + MovieContract.MovieEntry._ID);
    }

    private MovieDbHelper mOpenHelper;


    @Override
    public boolean onCreate() {
        mOpenHelper = new MovieDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Cursor returnCursor;
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case MOVIE:
                returnCursor = mOpenHelper.getReadableDatabase().query(MovieContract.MovieEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            case MOVIE_WITH_MD_ID:
                returnCursor = mOpenHelper.getReadableDatabase().query(MovieContract.MovieEntry.TABLE_NAME,
                        projection,
                        sMovieMdIdSelection,
                        new String[]{MovieContract.MovieEntry.getMovieMdIdFromUri(uri)},
                        null,
                        null,
                        sortOrder);
                break;
            case MOVIE_POPULARITY:
                returnCursor = mOpenHelper.getReadableDatabase().query(MovieContract.MovieEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        MovieContract.MovieEntry.COLUMN_POPULARITY
                                + " DESC limit " + Constants.MAX_NUMBER_OF_SHOWN_MOVIES);
                break;
            case MOVIE_RATING:
                returnCursor = mOpenHelper.getReadableDatabase().query(MovieContract.MovieEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        MovieContract.MovieEntry.COLUMN_USER_RATING
                                + " DESC limit " + Constants.MAX_NUMBER_OF_SHOWN_MOVIES);
                break;
            case MOVIE_FAVORITES:
                returnCursor = mOpenHelper.getReadableDatabase().query(MovieContract.MovieEntry.TABLE_NAME,
                        projection,
                        sFavoriteMovieSelection,
                        new String[]{"1"},
                        null,
                        null,
                        MovieContract.MovieEntry.COLUMN_POPULARITY
                                + " DESC");
                break;
            case TRAILER:
                returnCursor = mOpenHelper.getReadableDatabase().query(MovieContract.MovieEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            case TRAILER_WITH_MOVIE_ID:
                returnCursor = sTrailerByMovieSettingQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                        projection,
                        sMovieIdSelection,
                        new String[]{MovieContract.TrailerEntry.getMovieIdFromUri(uri)},
                        null,
                        null,
                        sortOrder);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);

        }
        returnCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return returnCursor;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case MOVIE:
                return MovieContract.MovieEntry.CONTENT_TYPE;
            case MOVIE_POPULARITY:
                return MovieContract.MovieEntry.CONTENT_TYPE;
            case MOVIE_RATING:
                return MovieContract.MovieEntry.CONTENT_TYPE;
            case MOVIE_FAVORITES:
                return MovieContract.MovieEntry.CONTENT_TYPE;
            case TRAILER:
                return MovieContract.TrailerEntry.CONTENT_TYPE;
            case TRAILER_WITH_MOVIE_ID:
                return MovieContract.TrailerEntry.CONTENT_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnUri = null;

        switch (match) {
            case MOVIE: {
                values.put(MovieContract.MovieEntry.COLUMN_FAVORITE, 0);
                Cursor c = this.query(MovieContract.MovieEntry.buildMovieUri(values.getAsString(MovieContract.MovieEntry.COLUMN_MDB_ID)),
                        new String[]{MovieContract.MovieEntry._ID, MovieContract.MovieEntry.COLUMN_FAVORITE},
                        null,
                        null,
                        null);
                if (c.moveToFirst()) {
                    long movieId = c.getLong(0);
                    boolean isItFavorite = (c.getInt(1) == 0 ? false : true);
                    if (isItFavorite) {
                        returnUri = MovieContract.MovieEntry.buildMovieUri(movieId);
                    }
                    this.delete(MovieContract.TrailerEntry.CONTENT_URI,
                            MovieContract.TrailerEntry.COLUMN_MOVIE_ID + " = ?",
                            new String[]{String.valueOf(movieId)});
                }
                if (returnUri == null) {
                    long id = db.insert(MovieContract.MovieEntry.TABLE_NAME, null, values);
                    if (id > 0) {
                        returnUri = MovieContract.MovieEntry.buildMovieUri(id);
                    } else {
                        throw new SQLException("Failed to insert row into " + uri);
                    }
                }
                break;
            }
            case TRAILER: {
                long id = db.insert(MovieContract.TrailerEntry.TABLE_NAME, null, values);
                if (id > 0) {
                    returnUri = MovieContract.TrailerEntry.buildTrailerUri(id);
                } else {
                    throw new SQLException("Failed to insert row into " + uri);
                }
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);

        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int numOfAffectedRows = 0;
        if (selection == null)
            selection = "1";
        switch (match) {
            case MOVIE:
                numOfAffectedRows = db.delete(MovieContract.MovieEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case TRAILER:
                numOfAffectedRows = db.delete(MovieContract.TrailerEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if (numOfAffectedRows > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return numOfAffectedRows;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int numOfAffectedRows = 0;

        switch (match) {
            case MOVIE:
                numOfAffectedRows = db.update(MovieContract.MovieEntry.TABLE_NAME, values, selection, selectionArgs);
        }

        if (numOfAffectedRows > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return numOfAffectedRows;
    }

}
