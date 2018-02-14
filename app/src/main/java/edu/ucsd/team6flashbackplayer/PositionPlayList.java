package edu.ucsd.team6flashbackplayer;

import java.util.ArrayList;
import java.util.List;

// position of the songs in the global songlist
// In this way we can easily pass these data across activities and services.
public class PositionPlayList {

    private ArrayList<Integer> positionList = new ArrayList<>();;
    private List<Song> songs = SongList.getSongs();

    public PositionPlayList(Song song) {
        positionList.add(songs.indexOf(song));
    }

    public PositionPlayList(Album album) {
        for (Song song: album.getSongs()) {
            positionList.add(songs.indexOf(song));
        }
    }

    // TODO: another ctor for flashback pl needed

    public ArrayList<Integer> getPositionList() {
        return positionList;
    }

}

