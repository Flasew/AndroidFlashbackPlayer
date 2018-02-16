package edu.ucsd.team6flashbackplayer;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import java.time.DayOfWeek;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
// JSON parsing - for SharedPreferences
import com.eclipsesource.json.*;

import org.json.JSONObject;


/**
 * Created by frankwang on 2/6/18.
 * JSON edits by alice on 2/14/18
 */

public final class Song {

    public static final String NO_INFO = "---";
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

    private String jsonString;

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

        jsonString = jsonParse();
    }

    // getters of const fields
    public String getId()      { return ID; }
    public String getTitle()   { return TITLE != null ? TITLE : NO_INFO; }
    public String getAlbum()   { return ALBUM != null ? ALBUM : NO_INFO; }
    public String getArtist()  { return ARTIST != null ? ARTIST : NO_INFO; }

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
    // NEW: like and dislike detailed implementation moved to SongPreference class
    public void setLike(boolean l)     { liked = l; }
    public void setDislike(boolean l)  { disliked = l; }

    // getter
    public boolean isLiked()        {return liked; }
    public boolean isDisliked()     {return disliked; }

    public void setJsonString(String json) {
        jsonString = json;
    }
    public String getJsonString() {return jsonString; }


    /** To determine what time of day a song was listened to split into
     * Three sections of the day (morning, afternoon, night)
     * @int hour
     * @return int representing the enum of the time of day
     * 0 for morning, 1 for afternoon, 2 for night
     */
    private static int timeOfDay(int h) {
        // Between 5 AM and 11 AM
        if (h >= 5 && h < 11)
            return 0;
        // Between 11 AM and 5 PM
        if (h >= 11 && h < 17)
            return 1;
        // Else between 5 PM and 5 AM
        return 2;
    }

    /**
     * Gives the JSON representation of song metadata (in a String form)
     * @return a string in JSON form representing the metadata of a song
     */
    public String jsonParse() {
        //to test setting latest location
        /*LatLng geisel = new LatLng(32.8812, -117.2374);
        double latitude = geisel.latitude;
        double longitude = geisel.longitude;
        setLatestLoc(geisel);*/
        JsonArray location = Json.array();

        /*setLatestLoc(new LatLng(45.415,1.568));
        setLatestLoc(new LatLng(64.2205, 19.996));*/
        JsonArray allLocations = Json.array();

        if (latestLoc == null) {
            location.add(NO_INFO);
            allLocations.add(NO_INFO);
        }
        else {
            location = Json.array(latestLoc.latitude, latestLoc.longitude);
            for (LatLng loc : locHist) {
                JsonArray place = Json.array(loc.latitude,loc.longitude);
                allLocations.add(place);
            }
        }

        JsonObject builder = new JsonObject();

        builder.add("ID", getId());
        builder.add("Title", getTitle());
        builder.add("Album", getAlbum());
        builder.add("Artist", getArtist());

        builder.add("LatestLocation", location);
        //setLatestTime(ZonedDateTime.now());
        JsonArray timeArray = Json.array();
        if (latestTime == null) {
            timeArray.add(NO_INFO);
        }
        else {
            timeArray = Json.array(latestTime.getMonthValue(), latestTime.getDayOfMonth(), latestTime.getYear(),
                    latestTime.getHour(), latestTime.getMinute(), latestTime.getSecond());
        }
        builder.add("LatestTime", timeArray);

        builder.add("Liked", isLiked());
        builder.add("Disliked", isDisliked());

        JsonArray timeOfDay = Json.array(getTimeHist());
        builder.add("TimeOfDay", timeOfDay);
        JsonArray dayOfWeek = Json.array(getDayHist());
        builder.add("DayOfWeek", dayOfWeek);

        builder.add("LocationHistory", allLocations);
        Log.d("Song metadata", builder.toString());
        return builder.toString();
    }

    public String jsonEdit(String json, LatLng loc) {
        JsonObject jsonObject = Json.parse(json).asObject();

        return "";
    }
}
