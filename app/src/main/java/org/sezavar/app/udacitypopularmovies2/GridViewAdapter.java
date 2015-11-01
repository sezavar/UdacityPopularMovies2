package org.sezavar.app.udacitypopularmovies2;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by amir on 10/7/15.
 */
public class GridViewAdapter extends CursorAdapter {
    private static final String LOG_TAG = GridViewAdapter.class.getSimpleName();
    private int mResourceId;

    public GridViewAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.grid_item_popular_movies, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder viewHolder = (ViewHolder) view.getTag();
        Transformation transformer = new RoundedTransformation(6, 4);
        Log.i(LOG_TAG, Constants.MOVIE_POSTER_BASE_URL + cursor.getString(PopularMoviesFragment.COL_POSTER_PATH));
        String posterPath = cursor.getString(PopularMoviesFragment.COL_POSTER_PATH);
        if (posterPath != null && !posterPath.equals("null"))
            Picasso.with(context).
                    load(Constants.MOVIE_POSTER_BASE_URL + posterPath).transform(transformer).
                    placeholder(R.drawable.place_holder).error(R.drawable.not_available).
                    fit().
                    into(viewHolder.getMovieThumbnail());
        else
            Picasso.with(context).
                    load(R.drawable.not_available).transform(transformer).
                    placeholder(R.drawable.place_holder).
                    fit().
                    into(viewHolder.getMovieThumbnail());
    }

    public static class ViewHolder {
         @InjectView(R.id.movie_thumbnail)
        public ImageView movieThumbnail;

        public ImageView getMovieThumbnail() {
            return movieThumbnail;
        }

        public ViewHolder(View view) {
            movieThumbnail = (ImageView) view.findViewById(R.id.movie_thumbnail);
            ButterKnife.inject(this, view);
        }
    }
}
