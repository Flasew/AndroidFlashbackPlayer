package edu.ucsd.team6flashbackplayer;

/**
 * Created by frankwang on 3/6/18.
 */

/**
 * SharedSong class
 * A shared song is used in vibe mode, which contains extra information like URL, songID, lastplayed
 * User, etc.
 * Notice that NOT ALL information should be pushed to Firebase.
 */
public class SharedSong extends Song {

    private final String ID;    // unique id of the song
    private final String URL;   // url of the source of this song, could be URL of an album

    private String LastPlayedUserUid;   // last played by user with this uid

    /**
     * Constructor. Delegate most field to the base Song class, but set the URL and ID field.
     * A song will have a uniqie ID of URL + "::" + PATH.
     * @param url url of source of the song
     * @param path path of the song stored locally. Basically a filename.
     * @param title title of the song
     * @param artist artist of the song
     * @param album album of the song
     */
    public SharedSong(String url, String path,
                      String title, String artist,
                      String album) {
        super(path, title, artist, album);
        URL = url;
        ID = url + "::" + getPath();

    }

    // Auto generated getters
    public String getID() {
        return ID;
    }

    public String getURL() {
        return URL;
    }

    public String getLastPlayedUserUid() {
        return LastPlayedUserUid;
    }

}
