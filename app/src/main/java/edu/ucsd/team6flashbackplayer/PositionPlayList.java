package edu.ucsd.team6flashbackplayer;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;

// position of the songs in the global songlist
// In this way we can easily pass these data across activities and services.
public class PositionPlayList {

    private static final String TAG = "PositionPlaylist";

    private ArrayList<Integer> positionList = new ArrayList<>();;
    private List<Song> songs = SongList.getSongs();

    public PositionPlayList(Song song) {
        if (!song.isDisliked())
            positionList.add(songs.indexOf(song));
    }

    public PositionPlayList(Album album) {
        for (Song song: album.getSongs()) {
            if (!song.isDisliked())
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

        Log.d(TAG, "Generating FB playlist...");
        Queue<Integer> pq = new PriorityQueue<Integer>(new SongScoreComparator(currLoc, currTime));

        for( int index = 0; index < songs.size(); index++ ){
            Song s = songs.get(index);
            // get rid of disliked songs
            if( !(s.isDisliked()) ) {
                // get rid of songs not liked and not played before
                if( s.isLiked() || !(s.getLatestTime() == null) ) {
                    pq.add(index);
                }
            }
        }

        while(!(pq.isEmpty())) {
            positionList.add(pq.poll());
        }
    }

    // TODO: another ctor for flashback pl needed

    public ArrayList<Integer> getPositionList() {
        return positionList;
    }

}

