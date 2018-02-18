package edu.ucsd.team6flashbackplayer;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by frankwang on 2/10/18.
 */

/**
 * Class album
 * Representation of an album. An album consists of a list of songs.
 */
public class Album {
    private final String NAME;      // name of the album
    private List<Song> songs;       // songs in the album

    /**
     * Constructor. Takes a string as album name.
     * @param n album name
     */
    public Album(String n) {
        NAME = n;
        songs = new ArrayList<>();
    }

    /**
     * Constructor. Takes a string as album name and a list of song as the album content.
     * @param n album name
     * @param s list of songs of this album
     */
    public Album(String n, List<Song> s) {
        NAME = n;
        songs = s;
    }

    /**
     * Add a song to this album
     * @param s song to be added
     */
    public void addSong(Song s) {
        songs.add(s);
    }

    /**
     * Get the list of songs in this album.
     * @return list of songs in th album
     */
    public List<Song> getSongs() {
        return songs;
    }

    /**
     * get the name of the album
     * @return the name of the album
     */
    public String getName() {
        return NAME;
    }

    /**
     * toString of Album class will retrun it's name.
     * @return name of the album.
     */
    @Override
    public String toString() {
        return getName();
    }

}
