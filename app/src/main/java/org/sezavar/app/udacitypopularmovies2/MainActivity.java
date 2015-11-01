package org.sezavar.app.udacitypopularmovies2;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity implements PopularMoviesFragment.Callback {
    private String mSortBy;
    private static final String MOVIE_DETAIL_FRAGMENT_TAG = "MDFTAG";
    private boolean mTwoPane;
    static boolean noItemIsSelected=true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSortBy = Utility.getPreferredSorting(this);
        setContentView(R.layout.activity_main);
        if (findViewById(R.id.movie_detail_container) != null) {
            mTwoPane = true;
            if (savedInstanceState == null || noItemIsSelected) {
                getSupportFragmentManager().beginTransaction().
                        replace(R.id.movie_detail_container, new DetailActivityFragment(), MOVIE_DETAIL_FRAGMENT_TAG)
                        .commit();
            }

        } else {
            mTwoPane = false;

        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        String sortBy = Utility.getPreferredSorting(this);
        if (sortBy != null && !sortBy.equals(mSortBy)) {
            PopularMoviesFragment pmf = (PopularMoviesFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.fragment_popular_movies);
            if (pmf != null) {
                pmf.onSortingChanged();
            }
            DetailActivityFragment daf = (DetailActivityFragment) getSupportFragmentManager().findFragmentByTag(MOVIE_DETAIL_FRAGMENT_TAG);
            if (daf != null) {
                noItemIsSelected=true;
                daf.showPleaseSelectMovie();
            }
            mSortBy = sortBy;
        }
    }

    @Override
    public void onItemSelected(Uri contentUri) {
        if (mTwoPane) {
            Bundle args = new Bundle();
            args.putParcelable(Constants.DETAIL_URI_KEY, contentUri);

            DetailActivityFragment fragment = new DetailActivityFragment();
            fragment.setArguments(args);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.movie_detail_container, fragment, MOVIE_DETAIL_FRAGMENT_TAG)
                    .commit();
            noItemIsSelected=false;
        } else {
            Intent intent = new Intent(this, DetailActivity.class)
                    .setData(contentUri);
            startActivity(intent);
        }
    }
}
