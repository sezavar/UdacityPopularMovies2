package org.sezavar.app.udacitypopularmovies2;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import butterknife.ButterKnife;
import butterknife.InjectView;

import org.sezavar.app.udacitypopularmovies2.data.MovieContract.*;

import java.util.ArrayList;
import java.util.List;

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailActivityFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final int DETAIL_LOADER = 0;
    private static final String LOG_TAG = DetailActivityFragment.class.getSimpleName();
    @InjectView(R.id.movie_thumbnail)
    public ImageView mThumbnail;
    @InjectView(R.id.detail_title_textview)
    public TextView mTitleView;
    @InjectView(R.id.detail_overview_textview)
    public TextView mOverviewView;
    @InjectView(R.id.detail_rating_textview)
    public TextView mRatingView;
    @InjectView(R.id.detail_release_date_textview)
    public TextView mReleaseDateView;
    @InjectView(R.id.detail_favorite_button)
    public Button mFavoriteButton;

    public static final int COL_ID = 0;
    public static final int COL_TITLE = 1;
    public static final int COL_POSTER_PATH = 2;
    public static final int COL_OVERVIEW = 3;
    public static final int COL_RATING = 4;
    public static final int COL_RELEASE_DATE = 5;
    public static final int COL_FAVORITE = 6;
    public static final int COL_TRAILER_PATH = 7;

    private Uri mUri;


    private static final String[] DETAIL_COLUMNS = {
            MovieEntry.TABLE_NAME + "." + MovieEntry._ID,
            MovieEntry.COLUMN_TITLE,
            MovieEntry.COLUMN_POSTER_PATH,
            MovieEntry.COLUMN_OVERVIEW,
            MovieEntry.COLUMN_USER_RATING,
            MovieEntry.COLUMN_RELEASE_DATE,
            MovieEntry.COLUMN_FAVORITE,
            TrailerEntry.TABLE_NAME + "." + TrailerEntry.COLUMN_TRAILER_PATH
    };
    private String firstTrailerUrl =null;
    private ShareActionProvider mShareActionProvider;

    public DetailActivityFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Bundle args = getArguments();
        if (args != null) {
            mUri = args.getParcelable(Constants.DETAIL_URI_KEY);

        }
        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
        ButterKnife.inject(this, rootView);
        mFavoriteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Movie movie = (Movie) v.getTag();
                if (movie != null) {
                    ContentValues values = new ContentValues();
                    values.put(MovieEntry.COLUMN_FAVORITE, movie.getFavorite() == 1 ? 0 : 1);
                    if (getActivity().getContentResolver().update(MovieEntry.CONTENT_URI, values, MovieEntry._ID + " = ?", new String[]{String.valueOf(movie.getId())}) > 0)
                        if (movie.getFavorite() == 0) {
                            mFavoriteButton.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.main));
                            mFavoriteButton.setText(getString(R.string.favorite_button_text));
                        } else {
                            mFavoriteButton.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.light_main));
                            mFavoriteButton.setText(getString(R.string.make_it_favorite_button_text));
                        }
                }
            }
        });
        return rootView;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        ButterKnife.reset(this);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(DETAIL_LOADER, null, this);

    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if ( mUri != null) {
            return new CursorLoader(getActivity(),
                    mUri,
                    DETAIL_COLUMNS,
                    null,
                    null,
                    null);
        } else {
            showPleaseSelectMovie();
        }
        return null;

    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        if (data != null && data.moveToFirst()) {
            boolean isFirstRideDone = false;
            List<String> trailers = new ArrayList<>();
            do {
                if (!isFirstRideDone) {
                    String title = data.getString(COL_TITLE);
                    mTitleView.setText(title);

                    String overview = data.getString(COL_OVERVIEW);
                    mOverviewView.setText(overview);

                    int rating = data.getInt(COL_RATING);
                    String ratingStr = rating + "/10";
                    mRatingView.setText(ratingStr);

                    long releaseDate = data.getLong(COL_RELEASE_DATE);
                    String releaseDateStr = Constants.MOVIE_RELEASE_DATE_FORMAT_TO_WRITE.format(releaseDate);
                    mReleaseDateView.setText(releaseDateStr);

                    final int favorite = data.getInt(COL_FAVORITE);
                    final long movieId = data.getLong(COL_ID);

                    String posterPath = data.getString(COL_POSTER_PATH);


                    Transformation transformer = new RoundedTransformation(6, 0);

                    if (posterPath != null && !posterPath.equals("null"))
                        Picasso.with(getContext()).
                                load(Constants.MOVIE_POSTER_BASE_URL + posterPath).transform(transformer).
                                placeholder(R.drawable.place_holder).error(R.drawable.not_available).
                                fit().
                                into(mThumbnail);
                    else
                        Picasso.with(getContext()).
                                load(R.drawable.not_available).transform(transformer).
                                placeholder(R.drawable.place_holder).
                                fit().
                                into(mThumbnail);

                    isFirstRideDone = true;
                    Movie movie = new Movie(movieId, title, posterPath, overview, rating, releaseDate, 0, favorite);
                    mFavoriteButton.setTag(movie);

                    if (favorite > 0) {
                        mFavoriteButton.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.main));
                        mFavoriteButton.setText(getString(R.string.favorite_button_text));
                    } else {
                        mFavoriteButton.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.light_main));
                        mFavoriteButton.setText(getString(R.string.make_it_favorite_button_text));
                    }

                }
                String trailerPath = data.getString(COL_TRAILER_PATH);
                if (trailerPath != null)
                    trailers.add(trailerPath);
            } while (data.moveToNext());
            this.addTrailersToDetailFragment(trailers);
            if (mShareActionProvider != null) {
                mShareActionProvider.setShareIntent(createShareForecastIntent());
            }
        }


    }


    private void addTrailersToDetailFragment(final List<String> trailers) {
        View trailerView = getView().findViewById(R.id.trailers_layout);
        if (trailerView != null) {
            ViewGroup mainLayout = ((ViewGroup) getView().findViewById(R.id.detail_main_layout));
            mainLayout.removeView(trailerView);
            View trailersLayout = getView().findViewById(R.id.trailers_layout);
            if (trailersLayout != null) {
                mainLayout.removeView(trailersLayout);
            }
        }

        if (trailers.size() > 0) {
            this.firstTrailerUrl =Constants.YOUTUBE_BASE_URL + trailers.get(0);
            ViewGroup trailersLayout = (ViewGroup) View.inflate(getContext(), R.layout.trailers_layout, null);
            for (int i = 0; i < trailers.size(); i++) {
                ViewGroup trailerItemLayout = (ViewGroup) View.inflate(getContext(), R.layout.trailer_item, null);
                ImageButton button = (ImageButton) trailerItemLayout.findViewById(R.id.image_button);
                TextView textView = (TextView) trailerItemLayout.findViewById(R.id.trailer_name_textview);
                final String trailerPath = trailers.get(i);
                textView.setText( getString(R.string.trailer_prefix)+" " +(i + 1));
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(Constants.YOUTUBE_BASE_URL + trailerPath)));
                        ;
                    }
                });
                trailersLayout.addView(trailerItemLayout);
            }
            ((ViewGroup) getView().findViewById(R.id.detail_main_layout)).addView(trailersLayout);
        }
    }

    void showPleaseSelectMovie() {
        View view = getView();
        if (view != null) {
            ViewGroup main = ((ViewGroup) getView().findViewById(R.id.detail_main_layout));
            if (main != null) {
                main.removeAllViews();
                main.addView(View.inflate(getContext(), R.layout.detail_not_selected_page, null));
            }
        }
    }


    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    public void setIsUriOutDated(boolean isUriOutDated) {
        isUriOutDated = isUriOutDated;
    }
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_detail, menu);
        MenuItem menuItem = menu.findItem(R.id.action_share);
        mShareActionProvider =
                (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);


        if (firstTrailerUrl != null) {
            mShareActionProvider.setShareIntent(createShareForecastIntent());
        }
    }
    private Intent createShareForecastIntent() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, firstTrailerUrl);
        return shareIntent;
    }
}
