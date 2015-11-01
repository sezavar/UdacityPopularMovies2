package org.sezavar.app.udacitypopularmovies2;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by amir on 10/9/15.
 */
public class Utility {
    public static String getPreferredSorting(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(context.getString(R.string.pref_sort_by_key),
                context.getString(R.string.pref_sort_by_popularity));
    }
}
