package edu.ucsd.team6flashbackplayer;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by frankwang on 2/10/18.
 */

// global song list
public class SongList {

    private static final List<Song> songs = new ArrayList<>();

    public SongList() {}
    public SongList(List<Song> songs) {
        this.songs.addAll(songs);
    }

    public static List<Song> getSongs() {
        return songs;
    }


}
