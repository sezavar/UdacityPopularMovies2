package org.sezavar.app.udacitypopularmovies2;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.sezavar.app.udacitypopularmovies2.data.MovieContract;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLConnection;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by amir on 10/5/15.
 */
public class FetchMoviesListTask extends AsyncTask<Void, Void, Void> {


    private static final String LOG_TAG = FetchMoviesListTask.class.getSimpleName();
    private GridViewAdapter mAdapter;


    private Context mContext;
    private final String apiKey;

    public FetchMoviesListTask(GridViewAdapter adapter, Context context) {
        this.mAdapter = adapter;
        this.mContext = context;
        this.apiKey = mContext.getResources().getString(R.string.the_movie_db_api_key);
    }

    @Override
    protected Void doInBackground(Void... params) {
        List<Long> addedMovies = fetchAndStoreMovies(mContext.getString(R.string.pref_sort_by_popularity));
        addedMovies.addAll(fetchAndStoreMovies(mContext.getString(R.string.pref_sort_by_rating)));
        this.cleanDataBase(addedMovies);
        return null;

    }

    private List<Long> fetchAndStoreMovies(String sortingKey) {
        List<Long> addedMoviesMdIds = new ArrayList<>();
        for (int i = 1; i <= 2; i++) {
            final String SORT_PARAM = "sort_by";
            final String API_KEY_PARAM = "api_key";
            final String PAGE_PARAM = "page";
            Uri builtUri = Uri.parse(Constants.DISCOVERY_BASE_URL).buildUpon().
                    appendQueryParameter(SORT_PARAM, sortingKey).
                    appendQueryParameter(API_KEY_PARAM, this.apiKey).appendQueryParameter(PAGE_PARAM, String.valueOf(i)).
                    build();


            String getResult = runGetHttpRequest(builtUri);
            if (getResult == null) {
                return null;
            }
            List<Movie> movies = null;
            try {
                movies = getMoviesDataFromJason(getResult);
            } catch (JSONException e) {
                Log.e(LOG_TAG, "Error", e);
            }


            if (movies != null) {
                final String PATH = "videos";
                for (Movie movie : movies) {
                    Uri builtTrailerUri = Uri.parse(Constants.MOVIE_TRAILERS_BASE_URL).buildUpon().
                            appendPath(String.valueOf(movie.getId())).
                            appendPath(PATH).
                            appendQueryParameter(API_KEY_PARAM, this.apiKey).
                            build();
                    String trailersGetResult = this.runGetHttpRequest(builtTrailerUri);
                    List<String> trailers = new ArrayList<>();
                    try {
                        trailers = this.getTrailersDataFromJason(trailersGetResult);
                    } catch (JSONException e) {
                        Log.e(LOG_TAG, "Error", e);
                    }
                    ContentValues movieValues = new ContentValues();
                    movieValues.put(MovieContract.MovieEntry.COLUMN_MDB_ID, movie.getId());
                    movieValues.put(MovieContract.MovieEntry.COLUMN_TITLE, movie.getTitle());
                    movieValues.put(MovieContract.MovieEntry.COLUMN_POSTER_PATH, movie.getPath());
                    movieValues.put(MovieContract.MovieEntry.COLUMN_POPULARITY, movie.getPopularity());
                    movieValues.put(MovieContract.MovieEntry.COLUMN_USER_RATING, movie.getUserRating());
                    movieValues.put(MovieContract.MovieEntry.COLUMN_OVERVIEW, movie.getOverview());
                    movieValues.put(MovieContract.MovieEntry.COLUMN_RELEASE_DATE, movie.getDate());

                    Uri movieUri = mContext.getContentResolver().insert(MovieContract.MovieEntry.CONTENT_URI, movieValues);

                    addedMoviesMdIds.add(movie.getId());

                    for (String trailerKey : trailers) {
                        ContentValues trailerValues = new ContentValues();
                        trailerValues.put(MovieContract.TrailerEntry.COLUMN_MOVIE_ID, MovieContract.MovieEntry.getMovieMdIdFromUri(movieUri));
                        trailerValues.put(MovieContract.TrailerEntry.COLUMN_TRAILER_PATH, trailerKey);
                        mContext.getContentResolver().insert(MovieContract.TrailerEntry.CONTENT_URI, trailerValues);
                    }
                }

            }
        }
        return addedMoviesMdIds;
    }

    private void cleanDataBase(List<Long> addedMovies) {
        StringBuilder inQuery = new StringBuilder();
        inQuery.append("(");
        boolean first = true;
        for (Long item : addedMovies) {
            if (first) {
                first = false;
                inQuery.append("'").append(item).append("'");
            } else {
                inQuery.append(", '").append(item).append("'");
            }
        }
        inQuery.append(")");
        String addedMoviesStr = inQuery.toString();
        mContext.getContentResolver().delete(MovieContract.MovieEntry.CONTENT_URI,
                MovieContract.MovieEntry.COLUMN_MDB_ID + " NOT IN " + addedMoviesStr + " AND " + MovieContract.MovieEntry.COLUMN_FAVORITE + " != 1",
                null);

    }

    private String runGetHttpRequest(Uri uri) {
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        try {

            String urlStr = uri.toString();
            URL url = new URL(urlStr);

            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod(("GET"));
            urlConnection.connect();
            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                return null;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line + "\n");
            }

            if (buffer.length() == 0) {
                return null;
            }
            return buffer.toString();
        } catch (MalformedURLException e) {
            Log.e(LOG_TAG, "Error", e);
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error", e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e(LOG_TAG, "Error closing stream", e);
                }
            }
        }
        return null;
    }

    private List<Movie> getMoviesDataFromJason(String jsonStr) throws JSONException {
        List<Movie> movies = new ArrayList<>();

        final String MDB_ID = "id";
        final String MDB_ORIGINAL_TITLE = "original_title";
        final String MDB_POSTER_PATH = "poster_path";
        final String MDB_OVERVIEW = "overview";
        final String MDB_VOTE_AVERAGE = "vote_average";
        final String MDB_RELEASE_DATE = "release_date";
        final String MDB_POPULARITY = "popularity";

        JSONObject moviesJson = new JSONObject(jsonStr);
        JSONArray moviesArray = moviesJson.getJSONArray("results");

        for (int i = 0; i < moviesArray.length(); i++) {
            long id;
            String originalTitle;
            String moviePosterPath;
            String overview;
            double voteAverage;
            long releaseDate;
            double popularity;

            JSONObject movieJson = moviesArray.getJSONObject(i);
            id = movieJson.getLong(MDB_ID);
            originalTitle = movieJson.getString(MDB_ORIGINAL_TITLE);
            moviePosterPath = movieJson.getString(MDB_POSTER_PATH);
            overview = movieJson.getString(MDB_OVERVIEW);
            if (overview == null || overview.equals("null")) {
                overview = "";
            }
            voteAverage = movieJson.getDouble(MDB_VOTE_AVERAGE);
            popularity = movieJson.getDouble(MDB_POPULARITY);

            try {
                releaseDate = Constants.MOVIE_RELEASE_DATE_FORMAT_TO_READ.parse(movieJson.getString(MDB_RELEASE_DATE)).getTime();
            } catch (ParseException e) {
                Log.w(LOG_TAG, "The release date format is wrong.", e);
                releaseDate = new Date(System.currentTimeMillis()).getTime();
            }
            movies.add(new Movie(id, originalTitle, moviePosterPath, overview, voteAverage, releaseDate, popularity, 0));
        }
        Log.i(LOG_TAG, "Fetching movies from json res is done, and num of movies is " + movies.size());
        return movies;
    }

    private List<String> getTrailersDataFromJason(String jsonStr) throws JSONException {
        List<String> trailersKeys = new ArrayList<>();

        final String TRAILER_KEY = "key";

        JSONObject moviesJson = new JSONObject(jsonStr);
        JSONArray trailersArray = moviesJson.getJSONArray("results");

        for (int i = 0; i < trailersArray.length(); i++) {
            JSONObject trailersArrayJSONObject = trailersArray.getJSONObject(i);
            String trailerKey = trailersArrayJSONObject.getString(TRAILER_KEY);
            trailersKeys.add(trailerKey);
        }
        return trailersKeys;
    }


}
