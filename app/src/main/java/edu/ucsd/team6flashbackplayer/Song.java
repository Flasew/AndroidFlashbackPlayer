package edu.ucsd.team6flashbackplayer;

import com.google.android.gms.maps.model.LatLng;
import java.io.Serializable;
import java.time.DayOfWeek;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by frankwang on 2/6/18.
 */

public final class Song implements Serializable {
    private final String ID;      // URI seems to be taken...
    private final String TITLE;
    private final String ARTIST;
    private final String ALBUM;

    // latest information, about last played. Put here in case we use
    // unordered data structures and would be unable to track the information.
    // ZonedDateTime stores everything we need about time
    private LatLng latestLoc;  // perhaps string of longitude & latitude?
    private ZonedDateTime latestTime;

    // all history of location of longitude and latitude
    private Collection<LatLng> locHist;
    // day of week and time of day, by their nature, could be implemented
    // as boolean arrays to achieve the fastest speed.
    private boolean[] timeHist;
    private boolean[] dayHist;    // 8 since GC.TIME_OF_WEEK ranges [1, 7]

    // like and dislike
    private boolean liked;
    private boolean disliked;

    // Ctor
    public Song(String id, String title, String artist, String album) {
        ID = id;
        TITLE = title;
        ARTIST = artist;
        ALBUM = album;

        latestLoc = null;
        latestTime = null;

        // let's say array list first...
        locHist = new ArrayList<LatLng>();

        timeHist = new boolean[3];
        dayHist  = new boolean[8];

        for (int i = 0; i < timeHist.length; i++)
            timeHist[i] = false;

        for (int i = 0; i < dayHist.length; i++)
            dayHist[i] = false;

        liked = false;
        disliked = false;
    }

    // getters of const fields
    public String getId()      { return ID; }
    public String getTitle()   { return TITLE; }
    public String getAlbum()   { return ALBUM; }
    public String getArtist()  { return ARTIST; }

    // getter & setter methods of location and time
    public LatLng getLatestLoc()              { return latestLoc; }
    public ZonedDateTime getLatestTime()      { return latestTime; }

    // following setters set the latest fields and add them to the history collection
    // Location should be made immutable.
    public void setLatestLoc(LatLng l) {
        latestLoc = l;
        locHist.add(l);
    }

    public void setLatestTime(ZonedDateTime t) {
        latestTime = t;
        timeHist[timeOfDay(t.getHour())] = true;
        dayHist[t.getDayOfWeek().getValue()] = true;
    }
    
    public boolean[] getTimeHist()         { return timeHist; }
    public boolean[] getDayHist()          { return dayHist; }
    public Collection<LatLng> getLocHist() { return locHist; }
    // like and dislike
    // NEW: like and dislike detailed implementation moved to songprefernce class
    public void setLike(boolean l)     { liked = l; }
    public void setDislike(boolean l)  { disliked = l; }

    // getter
    public boolean isLiked()        {return liked; }
    public boolean isDisliked()     {return disliked; }

    private static int timeOfDay(int h) {
        if (5 <= h && h < 11)
            return -1;
        if (11 <= h && h < 17)
            return 0;
        return 1;
    }


}
