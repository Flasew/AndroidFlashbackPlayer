package edu.ucsd.team6flashbackplayer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by frankwang on 2/10/18.
 */

public class Album {
    private final String NAME;      // name of the album
    private List<Song> songs; // songs in the album

    public Album(String n) {
        NAME = n;
        songs = new ArrayList<>();
    }

    public Album(String n, List<Song> s) {
        NAME = n;
        songs = s;
    }

    public void addSong(Song s) {
        songs.add(s);
    }

    public List<Song> getSongs() {
        return songs;
    }

    public String getName() {
        return NAME;
    }

    @Override
    public String toString() {
        return getName();
    }

}
