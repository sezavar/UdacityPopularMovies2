package org.sezavar.app.udacitypopularmovies2;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;
import java.util.List;

/**
 * Created by amir on 10/6/15.
 */
public class Movie implements Parcelable {
    private long id;
    private String title;
    private String path;
    private String overview;
    private double userRating;
    private long date;
    private double popularity;
    private int favorite;
    private List<String> trailers;


    public long getId() {
        return id;
    }

    public Movie(long id, String title, String path, String overview, double userRating, long date, double popularity,int favorite) {
        this.id = id;
        this.title = title;
        this.path = path;
        this.overview = overview;
        this.userRating = userRating;
        this.date = date;
        this.popularity = popularity;
        this.favorite=favorite;
    }

    protected Movie(Parcel in) {
        id = in.readLong();
        title = in.readString();
        path = in.readString();
        overview = in.readString();
        userRating = in.readDouble();
        date = in.readLong();
        popularity = in.readDouble();
        favorite=in.readInt();
        in.readStringList(trailers);
    }

    public static final Creator<Movie> CREATOR = new Creator<Movie>() {
        @Override
        public Movie createFromParcel(Parcel in) {
            return new Movie(in);
        }

        @Override
        public Movie[] newArray(int size) {
            return new Movie[size];
        }
    };

    public String getTitle() {
        return title;
    }

    public String getPath() {
        return path;
    }

    public String getOverview() {
        return overview;
    }

    public double getUserRating() {
        return userRating;
    }

    public long getDate() {
        return date;
    }

    public double getPopularity() {
        return popularity;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public int getFavorite() {
        return favorite;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(title);
        dest.writeString(path);
        dest.writeString(overview);
        dest.writeDouble(userRating);
        dest.writeLong(date);
        dest.writeDouble(popularity);
        dest.writeInt(favorite);
        dest.writeStringList(trailers);
    }

    public void setTrailers(List<String> trailers) {
        this.trailers = trailers;
    }
}
