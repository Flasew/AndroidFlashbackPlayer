package edu.ucsd.team6flashbackplayer;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashSet;
// JSON parsing - for SharedPreferences
import com.eclipsesource.json.*;

/**
 * Created by frankwang on 2/6/18.
 * JSON edits by alice on 2/14/18
 */

public final class Song {

    public static final String NO_INFO = "---";
    public static final double NO_LOC = 1000;
    public static final int NO_TIME = 10000;
    private final String ID;
    private final String TITLE;
    private final String ARTIST;
    private final String ALBUM;

    // latest information, about last played. Put here in case we use
    // unordered data structures and would be unable to track the information.
    // ZonedDateTime stores everything we need about time
    private LatLng latestLoc;  // lat and long in doubles
    private ZonedDateTime latestTime;

    // all history of location of longitude and latitude
    private HashSet<LatLng> locHist;
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

        locHist = new HashSet<LatLng>();

        timeHist = new boolean[3];
        dayHist  = new boolean[8];

        for (int i = 0; i < timeHist.length; i++)
            timeHist[i] = false;

        for (int i = 0; i < dayHist.length; i++)
            dayHist[i] = false;

        liked = false;
        disliked = false;

        // Parse the default fields into JSON on creation
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

    // following setters set the latest fields and add them to the history set
    // Location should be made immutable.
    public void setLatestLoc(LatLng l) {
        latestLoc = l;
    }
    public void setLocHist(HashSet<LatLng> l) {
        locHist = l;
    }

    public void setLatestTime(ZonedDateTime t) {
        latestTime = t;
    }

    public boolean[] getTimeHist()         { return timeHist; }
    public void setTimeHist(boolean[] history) { timeHist = history; }
    public boolean[] getDayHist()          { return dayHist; }
    public void setDayHist(boolean[] history) { dayHist = history; }

    public HashSet<LatLng> getLocHist() { return locHist; }

    // NEW: like and dislike detailed implementation moved to SongPreference class
    public void setLike(boolean l)     { liked = l; }
    public void setDislike(boolean l)  { disliked = l; }

    // getter
    public boolean isLiked()        {return liked; }
    public boolean isDisliked()     {return disliked; }

    // Getters and setters for the Json string
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
     * Given the JSON string representing the saved fields of a certain song, load/set
     * those fields of the Song object
     * @param json The string (from SharedPreferences) representing the saved fields/info
     */
    public void jsonPopulate(String json) {
        JsonObject obj = Json.parse(json).asObject();

        // Title/Album/Artist don't need to be populated from SharedPreferences

        JsonArray latestLocArray = obj.get("LatestLocation").asArray();
        // Check if the latest location exists - song was played before
        // If not then set latest location and location history to default values
        if (latestLocArray.get(0).asDouble() == NO_LOC) {
            setLatestLoc(null);
            setLocHist(new HashSet<LatLng>());
        }
        // Otherwise get the locations and set to the fields
        else {
            double latitude = latestLocArray.get(0).asDouble();
            double longitude = latestLocArray.get(1).asDouble();
            setLatestLoc(new LatLng(latitude,longitude));

            // Iterate over the JsonArray to get all locations and put into a HashSet
            HashSet<LatLng> allLoc = new HashSet<>();
            JsonArray locationHistory = obj.get("LocationHistory").asArray();
            for (int m = 0; m < locationHistory.size(); m++) {
                JsonArray currLoc = locationHistory.get(m).asArray();
                // Get the lat and long of that location and add it to the HashSet
                allLoc.add(new LatLng(currLoc.get(0).asDouble(),currLoc.get(1).asDouble()));
            }
            setLocHist(allLoc);
        }

        // Check if song never played before (time)
        JsonArray lastTime = obj.get("LatestTime").asArray();
        if (lastTime.get(0).asInt() == NO_TIME) {
            setLatestTime(null);
        }
        else {
            // Get the current time zone of the system
            ZoneId zone = ZoneId.systemDefault();
            // Getting and setting the latest time song was played (creating new ZonedDateTime)
            ZonedDateTime time = ZonedDateTime.of(lastTime.get(0).asInt(),lastTime.get(1).asInt(),lastTime.get(2).asInt(),
                    lastTime.get(3).asInt(),lastTime.get(4).asInt(),lastTime.get(5).asInt(),lastTime.get(6).asInt(), zone);
            setLatestTime(time);
        }

        setLike(obj.get("Liked").asBoolean());
        setDislike(obj.get("Disliked").asBoolean());

        JsonArray timeOfDay = obj.get("TimeOfDay").asArray();
        boolean[] timeHist = new boolean[timeOfDay.size()];
        for (int i = 0; i < timeOfDay.size(); i++) {
            timeHist[i] = timeOfDay.get(i).asBoolean();
        }
        setTimeHist(timeHist);

        JsonArray dayOfWeek = obj.get("DayOfWeek").asArray();
        boolean[] dayHist = new boolean[dayOfWeek.size()];
        for (int i = 0; i < dayOfWeek.size(); i++) {
            dayHist[i] = dayOfWeek.get(i).asBoolean();
        }
        setDayHist(dayHist);
    }

    /**
     * Gives the JSON representation of song metadata (in a String form)
     * @return a string in JSON form representing the metadata of a song
     */
    public String jsonParse() {
        // The builder for the entire JSON string
        JsonObject builder = new JsonObject();

        builder.add("ID", getId());
        builder.add("Title", getTitle());
        builder.add("Album", getAlbum());
        builder.add("Artist", getArtist());

        // Getting location into and converting to JSON
        JsonArray location = Json.array();
        JsonArray allLocations = Json.array();

        LatLng latestLoc = getLatestLoc();
        HashSet<LatLng> locHist = getLocHist();
        // Check if song was never listened to before, if so then the arrays will just contain NO_INFO elem
        if (latestLoc == null) {
            location.add(NO_LOC);
            allLocations.add(NO_LOC);
        }
        // Otherwise get the info and add it to the JSON
        else {
            location = Json.array(latestLoc.latitude, latestLoc.longitude);
            for (LatLng loc : locHist) {
                JsonArray place = Json.array(loc.latitude,loc.longitude);
                allLocations.add(place);
            }
        }

        builder.add("LatestLocation", location);

        ZonedDateTime latestTime = getLatestTime();
        JsonArray timeArray = Json.array();
        if (latestTime == null) {
            timeArray.add(NO_TIME);
        }
        else {
            timeArray = Json.array(latestTime.getYear(), latestTime.getMonthValue(), latestTime.getDayOfMonth(),
                    latestTime.getHour(), latestTime.getMinute(), latestTime.getSecond(), latestTime.getNano());
        }
        builder.add("LatestTime", timeArray);

        builder.add("Liked", isLiked());
        builder.add("Disliked", isDisliked());

        JsonArray timeOfDay = Json.array(getTimeHist());
        builder.add("TimeOfDay", timeOfDay);
        JsonArray dayOfWeek = Json.array(getDayHist());
        builder.add("DayOfWeek", dayOfWeek);

        builder.add("LocationHistory", allLocations);
        //Log.d("Song metadata", builder.toString());
        return builder.toString();
    }

    /**
     * Update the Shared Preferences attached to the specific Song
     * with the new latest time played and location it was played at
     * @param time new latest time of the song in ZonedDateTime
     * @param loc new latest location of the song in LatLng
     */
    public void updateLocTime(ZonedDateTime time, LatLng loc) {
        setLatestTime(time);
        if (time != null) {
            timeHist[timeOfDay(time.getHour())] = true;
            dayHist[time.getDayOfWeek().getValue()] = true;
        }

        setLatestLoc(loc);
        locHist.add(loc);

        // Update the jsonString
        refreshJson();
    }

    /**
     * Updates the field of a song based on the passed in boolean determining if the
     * like button of the Song was clicked (doesn't necessarily mean always will be liked)
     */
    public void refreshJson() {
        setJsonString(jsonParse());
    }

    @Override
    public String toString() {
        return getTitle();
    }

}
