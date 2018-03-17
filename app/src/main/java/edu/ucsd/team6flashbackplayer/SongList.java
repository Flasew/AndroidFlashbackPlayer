package edu.ucsd.team6flashbackplayer;

import java.util.List;

/**
 * Created by frankwang on 2/10/18.
 */

/**
 * class SongList
 * Consist a global list of songs, which are populated on application load.
 * Most other activities get the songs by knowing the song's position in this list.
 */
public class SongList {

    // global song list
    private static List<Song> songs;

    /**
     * Constructor taking a list of songs to populate the song list. Can only be done once.
     * @param songs
     */
    public static void initSongList(List<Song> songs) {
        if (SongList.songs == null)
            SongList.songs = songs;
    }

    /**
     * get the global song list. XXX: consider return a copy instead of the original global list.
     * @return the global song list
     */
    public static List<Song> getSongs() {
        return songs;
    }


    /**
     * Adds the song to the local user list (called on download)
     * Meaning that the song will always never already exist in the list
     * @param song Song to add
     */
    public static void addSong(Song song) {
        // If we are calling this we know that the song doesn't already exist in the local list
        SongList.getSongs().add(song);
    }
}
