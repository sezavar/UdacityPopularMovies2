package org.sezavar.app.udacitypopularmovies2;


import android.net.Uri;
import android.support.v4.app.LoaderManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import org.sezavar.app.udacitypopularmovies2.data.MovieContract;

/**
 * A placeholder fragment containing a simple view.
 */
public class PopularMoviesFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private GridViewAdapter mMoviesAdapter;
    private static final int MOVIE_LIST_LOADER_ID = 0;
    private static boolean isFetchingTaskRun = false;
    GridView mGridOfMovies =null;
    static final int COL_MOVIE_ID = 0;
    static final int COL_POSTER_PATH = 1;
    private static final String[] MOVIE_COLUMNS = {
            MovieContract.MovieEntry.TABLE_NAME + "." + MovieContract.MovieEntry._ID,
            MovieContract.MovieEntry.COLUMN_POSTER_PATH,
    };

    public PopularMoviesFragment() {
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.popular_movies_fragment, container, false);
        this.mMoviesAdapter = new GridViewAdapter(getContext(), null, 0);
         mGridOfMovies = (GridView) rootView.findViewById(R.id.gridview_popular_movies);

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            mGridOfMovies.setNumColumns(4);
        } else {
            mGridOfMovies.setNumColumns(3);
        }

        mGridOfMovies.setAdapter(this.mMoviesAdapter);
        mGridOfMovies.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView adapterView, View view, int position, long l) {
                Cursor cursor = (Cursor) mMoviesAdapter.getItem(position);
                if (cursor != null) {
                    long movieId = cursor.getLong(COL_MOVIE_ID);
                    ((Callback) getActivity()).onItemSelected(MovieContract.TrailerEntry.buildTrailerMovie((movieId)));
                }
            }
        });
        if (!isFetchingTaskRun) {
            updateMovieList();
        }
        return rootView;
    }

    void updateMovieList() {
        FetchMoviesListTask fetchMoviesListTask = new FetchMoviesListTask(this.mMoviesAdapter, getContext());
        fetchMoviesListTask.execute();
        isFetchingTaskRun = true;
    }

    void onSortingChanged() {
        getLoaderManager().restartLoader(MOVIE_LIST_LOADER_ID, null, this);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(MOVIE_LIST_LOADER_ID, null, this);
        super.onActivityCreated(savedInstanceState);
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String sorting = Utility.getPreferredSorting(getContext());

        return new CursorLoader(getContext(),
                MovieContract.MovieEntry.buildMovieUriBasedOnSortingField(sorting, getContext()),
                MOVIE_COLUMNS,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mMoviesAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mMoviesAdapter.swapCursor(null);
    }

    public interface Callback {
        public void onItemSelected(Uri dateUri);
    }

}
