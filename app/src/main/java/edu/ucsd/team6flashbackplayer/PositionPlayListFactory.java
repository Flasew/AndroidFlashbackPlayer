package edu.ucsd.team6flashbackplayer;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.lang.reflect.Array;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;

/**
 * class PositionPlayListFactory
 * PPL means the position of the songs in the global songlist. This class is used to generate
 * such a list from a list of songs or relevant information (Album, time/loc, etc.)
 * In this way we can easily pass these data across activities and services.
 */
public class PositionPlayListFactory {

    static final String POS_LIST_INTENT = "posList";     // label of position playlist intents.

    private static final String TAG = "PositionPlaylist";

//    private ArrayList<Integer> positionList = new ArrayList<>();
//    private List<Song> songs = SongList.getSongs();

//
//    public PositionPlayListFactory(Song song) {
//        Log.d(TAG, "Generating playlist for song " + song.getTitle());
//        positionList.add(songs.indexOf(song));
//    }

    /**
     * Create a ppl from a song, basically an arraylist of only one element corresponding to
     * the position of the song in the global list.
     * @param song song of this ppl.
     */
    public static int makeList(Song song) {
        Log.d(TAG, "Generating playlist for song " + song.getTitle());
        return SongList.getSongs().indexOf(song);
    }


//    public PositionPlayListFactory(Album album) {
//        Log.d(TAG, "Generating playlist for album " + album.getName());
//        for (Song song: album.getSongs()) {
//            positionList.add(songs.indexOf(song));
//        }
//    }

    /**
     * Create a ppl from an album. An arraylist of all songs' position in the album.
     * @param album album of this ppl
     */
    public static ArrayList<Integer> makeList(Album album) {
        Log.d(TAG, "Generating playlist for album " + album.getName());
        ArrayList<Integer> list = new ArrayList<>();
        for (Song song: album.getSongs()) {
            list.add(SongList.getSongs().indexOf(song));
        }
        return list;
    }

//    /**
//     * Constructor for flashback
//     * This constructor will created the list of songs ordered by
//     * the priorities for flashback mode, all disliked songs will be removed
//     * @param currLoc location when entering flashback mode
//     * @param currTime time when entering flashback mode
//     */
//    @Deprecated
//    public PositionPlayListFactory(LatLng currLoc, ZonedDateTime currTime ){
//
//        Log.d(TAG, "Generating FB playlist...");
//        Queue<Integer> pq = new PriorityQueue<Integer>(new SongScoreComparator(currLoc, currTime));
//
//        for( int index = 0; index < songs.size(); index++ ){
//            Song s = songs.get(index);
//            // get rid of disliked songs
//            if( !(s.isDisliked()) ) {
//                // get rid of songs not liked and not played before
//                if( s.isLiked() || !(s.getLatestTime() == null) ) {
//                    pq.add(index);
//                }
//            }
//        }
//
//        while(!(pq.isEmpty())) {
//            positionList.add(pq.poll());
//        }
//    }
    /**
     * factory for vibe
     * This constructor will created the list of songs ordered by
     * the priorities for flashback mode, all disliked songs will be removed
     * @param currLoc location when entering flashback mode
     * @param currTime time when entering flashback mode
     * @ensure User.self != null (otherwise should not allow vibe mode to be turned on
     */
    public static ArrayList<Integer> makeList(LatLng currLoc, ZonedDateTime currTime ) {
        return new ArrayList<>();
    }
//
//    /**
//     * get the arraylist of song positions correspond to the information passed in.
//     * @return arraylist of song positions
//     */
//    public ArrayList<Integer> getPositionList() {
//        return positionList;
//    }

}

