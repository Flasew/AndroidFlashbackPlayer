package edu.ucsd.team6flashbackplayer;

import com.google.android.gms.maps.model.LatLng;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
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

    /**
     * Constructor for flashback
     * This constructor will created the list of songs ordered by
     * the priorities for flashback mode, all disliked songs will be removed
     * @param currLoc location when entering flashback mode
     * @param currTime time when entering flashback mode
     */
    public PositionPlayList(LatLng currLoc, ZonedDateTime currTime ){
        List<Song> sortedList = new ArrayList<Song>(songs);
        Collections.sort(sortedList, new SongScoreComparator(currLoc, currTime));

        for( Song song : sortedList ){
            if(!song.isDisliked()){
                positionList.add(songs.indexOf(song));
            }
        }
    }

    // TODO: another ctor for flashback pl needed

    public ArrayList<Integer> getPositionList() {
        return positionList;
    }

}

