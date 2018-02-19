package edu.ucsd.team6flashbackplayer;

/**
 * Created by frankwang on 2/18/18.
 */

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.google.android.gms.maps.model.LatLng;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashSet;

/**
 * class SongJsonParser
 * Encapsulate the song to json (and backward) methods
 */
public class SongJsonParser {

    private static final String NO_INFO = "---";     // noinfo string
    private static final double NO_LOC = 1000;
    private static final int NO_TIME = 10000;

    /**
     * Given the JSON string representing the saved fields of a certain song, load/set
     * those fields of the Song object
     * @param s song to be populated
     * @param json The string (from SharedPreferences) representing the saved fields/info
     */
    public static void jsonPopulate(Song s, String json) {
        JsonObject obj = Json.parse(json).asObject();

        // Title/Album/Artist don't need to be populated from SharedPreferences

        JsonArray latestLocArray = obj.get("LatestLocation").asArray();
        // Check if the latest location exists - song was played before
        // If not then set latest location and location history to default values
        if (latestLocArray.get(0).asDouble() == NO_LOC) {
            s.setLatestLoc(null);
            s.setLocHist(new HashSet<LatLng>());
        }
        // Otherwise get the locations and set to the fields
        else {
            double latitude = latestLocArray.get(0).asDouble();
            double longitude = latestLocArray.get(1).asDouble();
            s.setLatestLoc(new LatLng(latitude,longitude));

            // Iterate over the JsonArray to get all locations and put into a HashSet
            HashSet<LatLng> allLoc = new HashSet<>();
            JsonArray locationHistory = obj.get("LocationHistory").asArray();
            for (int m = 0; m < locationHistory.size(); m++) {
                JsonArray currLoc = locationHistory.get(m).asArray();
                // Get the lat and long of that location and add it to the HashSet
                allLoc.add(new LatLng(currLoc.get(0).asDouble(),currLoc.get(1).asDouble()));
            }
            s.setLocHist(allLoc);
        }

        // Check if song never played before (time)
        JsonArray lastTime = obj.get("LatestTime").asArray();
        if (lastTime.get(0).asInt() == NO_TIME) {
            s.setLatestTime(null);
        }
        else {
            // Get the current time zone of the system
            ZoneId zone = ZoneId.systemDefault();
            // Getting and setting the latest time song was played (creating new ZonedDateTime)
            ZonedDateTime time = ZonedDateTime.of(lastTime.get(0).asInt(),lastTime.get(1).asInt(),lastTime.get(2).asInt(),
                    lastTime.get(3).asInt(),lastTime.get(4).asInt(),lastTime.get(5).asInt(),lastTime.get(6).asInt(), zone);
            s.setLatestTime(time);
        }

        s.setLike(obj.get("Liked").asBoolean());
        s.setDislike(obj.get("Disliked").asBoolean());

        JsonArray timeOfDay = obj.get("TimeOfDay").asArray();
        boolean[] timeHist = new boolean[timeOfDay.size()];
        for (int i = 0; i < timeOfDay.size(); i++) {
            timeHist[i] = timeOfDay.get(i).asBoolean();
        }
        s.setTimeHist(timeHist);

        JsonArray dayOfWeek = obj.get("DayOfWeek").asArray();
        boolean[] dayHist = new boolean[dayOfWeek.size()];
        for (int i = 0; i < dayOfWeek.size(); i++) {
            dayHist[i] = dayOfWeek.get(i).asBoolean();
        }
        s.setDayHist(dayHist);
    }

    /**
     * Gives the JSON representation of song metadata (in a String form)
     * @param s song to be parsed
     * @return a string in JSON form representing the metadata of a song
     */
    public static String jsonParse(Song s) {
        // The builder for the entire JSON string
        JsonObject builder = new JsonObject();

        builder.add("ID", s.getId());
        builder.add("Title", s.getTitle());
        builder.add("Album", s.getAlbum());
        builder.add("Artist", s.getArtist());

        // Getting location into and converting to JSON
        JsonArray location = Json.array();
        JsonArray allLocations = Json.array();

        LatLng latestLoc = s.getLatestLoc();
        HashSet<LatLng> locHist = s.getLocHist();
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

        ZonedDateTime latestTime = s.getLatestTime();
        JsonArray timeArray = Json.array();
        if (latestTime == null) {
            timeArray.add(NO_TIME);
        }
        else {
            timeArray = Json.array(latestTime.getYear(), latestTime.getMonthValue(), latestTime.getDayOfMonth(),
                    latestTime.getHour(), latestTime.getMinute(), latestTime.getSecond(), latestTime.getNano());
        }
        builder.add("LatestTime", timeArray);

        builder.add("Liked", s.isLiked());
        builder.add("Disliked", s.isDisliked());

        JsonArray timeOfDay = Json.array(s.getTimeHist());
        builder.add("TimeOfDay", timeOfDay);
        JsonArray dayOfWeek = Json.array(s.getDayHist());
        builder.add("DayOfWeek", dayOfWeek);

        builder.add("LocationHistory", allLocations);
        //Log.d("Song metadata", builder.toString());
        return builder.toString();
    }

    /**
     * Update the Shared Preferences attached to the specific Song
     * with the new latest time played and location it was played at
     * @param s song to be updated
     * @param time new latest time of the song in ZonedDateTime
     * @param loc new latest location of the song in LatLng
     */
    public static void updateSongLocTime(Song s, ZonedDateTime time, LatLng loc) {
        s.setLatestTime(time);
        if (time != null) {
            s.getTimeHist()[Song.timeOfDay(time.getHour())] = true;
            s.getDayHist()[time.getDayOfWeek().getValue()] = true;
        }

        s.setLatestLoc(loc);
        s.getLocHist().add(loc);

        // Update the jsonString
        refreshJson(s);
    }

    /**
     * Updates the field of a song based on the passed in boolean determining if the
     * like button of the Song was clicked (doesn't necessarily mean always will be liked)
     * @param s song whose json string is to be set.
     */
    public static void refreshJson(Song s) {
        s.setJsonString(jsonParse(s));
    }
}
