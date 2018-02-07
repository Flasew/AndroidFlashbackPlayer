package edu.ucsd.team6flashbackplayer;

import android.location.Location;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by frankwang on 2/6/18.
 */

public final class Song {
    private final long URI_ID;      // URI seems to be taken...
    private final String NAME;
    private final String ARTIST;
    private final String ALBUM;

    // latest information, about last played. Put here in case we use
    // unordered data structures and would be unable to track the information.
    // ZonedDateTime stores everything we need about time
    private Location latest_loc;  // perhaps string of longitude & latitude?
    private ZonedDateTime latest_time;

    // all history of location
    private Collection<Location> loc_hist;
    // day of week and time of day, by their nature, could be implemented
    // as boolean arrays to achieve the fastest speed.
    private boolean[] time_hist;
    private boolean[] day_hist;    // 8 since GC.TIME_OF_WEEK ranges [1, 7]

    // like and dislike
    private boolean liked;
    private boolean disliked;

    // Ctor
    public Song(long uri, String name, String artist, String album) {
        URI_ID = uri;
        NAME = name;
        ARTIST = artist;
        ALBUM = album;

        latest_loc = null;
        latest_time = null;

        // let's say array list first...
        loc_hist = new ArrayList<Location>();

        time_hist = new boolean[3];
        day_hist  = new boolean[8];

        for (int i = 0; i < time_hist.length; i++)
            time_hist[i] = false;

        for (int i = 0; i < day_hist.length; i++)
            day_hist[i] = false;

        liked = false;
        disliked = false;
    }

    // getters of const fields
    public long get_uri()       { return URI_ID; }
    public String get_name()    { return NAME; }
    public String get_album()   { return ALBUM; }
    public String get_artist()  { return ARTIST; }

    // getter & setter methods of location and time
    public Location get_latest_loc()              { return latest_loc; }
    public ZonedDateTime get_latest_time()    { return latest_time; }

    // following setters set the latest fields and add them to the history collection
    // Location should be made immutable.
    public void set_latest_loc(Location l) {
        latest_loc = l;
        loc_hist.add(l);
    }

    public void set_latest_time(ZonedDateTime t) {
        latest_time = t;
        time_hist[time_of_day(t.getHour())] = true;
        day_hist[t.getDayOfWeek().getValue()] = true;
    }

    // calculate score
    public int loc_score(Location l) {
        for (Location loc: loc_hist) {
            if (l.equals(loc))
                return 1;
        }
        return 0;
    }

    public int tod_score(ZonedDateTime t) {
        if (time_hist[time_of_day(t.getHour())])
            return 1;
        return 0;
    }

    public int dow_score(ZonedDateTime t) {
        if (day_hist[t.getDayOfWeek().getValue()])
            return 1;
        return 0;
    }

    // perhaps an aggregated function?
    public int score(Location l, ZonedDateTime t) {
        return loc_score(l) + tod_score(t) + dow_score(t);
    }

    // like and dislike
    // this function basically emulates what should happed when "like" button
    // is pressed so it's much more complicated than simply inverting...
    // returns the final condition of @liked
    public boolean like() {
        // not liked: like the song and untoggle dislike
        if (!liked) {
            liked = true;
            disliked = false;
        }
        // other wise, the song is already liked so un-toggle the like.
        else {
            liked = false;
        }
        return liked;
    }
    // similar
    public boolean dislike(){
        // not disliked: dislike the song and untoggle like
        if (!disliked) {
            disliked = true;
            liked = false;
        }
        // other wise, the song is already liked so un-toggle the like.
        else {
            disliked = false;
        }
        return disliked;
    }

    // getter
    public boolean is_liked()        {return liked;}
    public boolean is_disliked()     {return disliked;}

    private static int time_of_day(int h) {
        if (5 <= h && h < 11)
            return -1;
        if (11 <= h && h < 17)
            return 0;
        return 1;
    }


}
