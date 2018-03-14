package edu.ucsd.team6flashbackplayer;

import android.support.annotation.NonNull;

import com.google.android.gms.maps.model.LatLng;
import java.time.ZonedDateTime;
import java.util.HashSet;

/**
 * Created by frankwang on 2/6/18.
 * JSON edits by alice on 2/14/18
 */

public class Song {

    static final String NO_INFO = "---";     // noinfo string

    private final String PATH;
    private final String TITLE;
    private final String ARTIST;
    private final String ALBUM;

    private String id;
    private String url;
    private String LastPlayedUserUid;

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

    // json sting of this song.
    private String jsonString;


    /**
     * Constructor. Takes song's relevant information and make a new song object
     * @param path id of the song. for now it's the path.
     * @param title title of the song
     * @param artist artist created the song
     * @param album album the song belongs to
     */
    public Song(String path, String title, String artist, String album) {
        PATH = path;
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
        jsonString = SongJsonParser.jsonParse(this);
    }

    /**
     * Constructor. Makes a new Song object based on relevant fiels - for use with FIREBASE
     * @param url the url from where the song was downloaded
     * @param path the path the song is
     * @param title title of the song
     * @param artist artist of the song
     * @param album album that the song is in
     * @param id a hash generated from the file itself
     */
    public Song(String url, String path, String title, String artist, String album, String id) {
        PATH = path;
        TITLE = title;
        ARTIST = artist;
        ALBUM = album;

        latestLoc = null;
        latestTime = null;

        locHist = new HashSet<LatLng>();

        this.url = url;
        this.id = id;
        this.LastPlayedUserUid = NO_INFO;

        liked = false;
        disliked = false;

        jsonString = SongJsonParser.jsonParseFirebase(this);
    }

    // getters of const fields
    public String getPath()    { return PATH; }
    public String getTitle()   { return TITLE != null ? TITLE : NO_INFO; }
    public String getAlbum()   { return ALBUM != null ? ALBUM : NO_INFO; }
    public String getArtist()  { return ARTIST != null ? ARTIST : NO_INFO; }

    // getter & setter methods of location and time
    // following setters set the latest fields and add them to the history set
    // Location should be made immutable.
    public LatLng getLatestLoc()              { return latestLoc; }
    public void setLatestLoc(LatLng l) {
        latestLoc = l;
    }

    public HashSet<LatLng> getLocHist() { return locHist; }
    public void setLocHist(HashSet<LatLng> l) {
        locHist = l;
    }

    public ZonedDateTime getLatestTime()      { return latestTime; }
    public void setLatestTime(ZonedDateTime t) {
        latestTime = t;
    }

    public boolean[] getTimeHist()         { return timeHist; }
    public void setTimeHist(boolean[] history) { timeHist = history; }

    public boolean[] getDayHist()          { return dayHist; }
    public void setDayHist(boolean[] history) { dayHist = history; }

    // getter
    public boolean isLiked()        { return liked; }
    public boolean isDisliked()     { return disliked; }

    // NEW: like and dislike detailed implementation moved to SongPreference class
    public void setLike(boolean l)     { liked = l; }
    public void setDislike(boolean l)  { disliked = l; }

    // Getters and setters for the Json string
    public void setJsonString(String json) {
        jsonString = json;
    }
    public String getJsonString() {return jsonString; }

    // Getters and setters for new fields (id, url, lastplayeduserid)
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public String getLastPlayedUserUid() { return LastPlayedUserUid; }
    public void setLastPlayedUserUid(String lastPlayedUserUid) { LastPlayedUserUid = lastPlayedUserUid; }


    /** To determine what time of day a song was listened to split into
     * Three sections of the day (morning, afternoon, night)
     * @int hour
     * @return int representing the enum of the time of day
     * 0 for morning, 1 for afternoon, 2 for night
     */
    public static int timeOfDay(int h) {
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
     * toString will get the title of the song.
     * @return title of song
     */
    @Override
    public String toString() {
        return getTitle();
    }

}
