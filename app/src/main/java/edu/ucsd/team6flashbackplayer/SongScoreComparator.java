package edu.ucsd.team6flashbackplayer;

import com.google.android.gms.maps.model.LatLng;

import java.time.ZonedDateTime;
import java.util.Comparator;
import java.util.List;

import static java.time.temporal.ChronoUnit.NANOS;

/**
 * Created by HRZhang on 2/14/2018.(I know, I have no life...)
 */

/**
 * class SongScoreComparator
 * A comparator class used for creating FB play list.
 */
public class SongScoreComparator implements Comparator<Integer> {

    private LatLng currLoc;
    private ZonedDateTime currTime;
    private List<Song> songs = SongList.getSongs();

    /**
     * Constructor
     * Stores current location and current time for score calculation
     * @param l current location
     * @param t current time
     */
    public SongScoreComparator( LatLng l, ZonedDateTime t ){
        this.currLoc = l;
        this.currTime = t;
    }
    /**
     * Comparator function, orders songs first by their scores according to the
     * SongScoreCalculator.
     * If the scores are the same, order by likedness with liked song < neutral song
     * If likeness are the same, order by played more recently < played less recently
     * @param index1 index of first song to compare
     * @param index2 index of second song to compare
     * @return -1 if first song should be ordered earlier in flashback
     *          1 if second song should be ordered earlier in flashback
     *          0 theoretically when two song are identical in order, should never occur
     */
    @Override
    public int compare( Integer index1, Integer index2 ){

        Song s1 = songs.get(index1);
        Song s2 = songs.get(index2);
        int score1 = SongScoreCalculator.calcScore(s1, currLoc, currTime);
        int score2 = SongScoreCalculator.calcScore(s2, currLoc, currTime);
        if( score1 == score2) {
            if( s1.isLiked() == s2.isLiked() ){

                // check if either song is not played before
                if(s1.getLatestTime() == null) {
                    if(s2.getLatestTime() == null) {
                        return 0;
                    }
                    return 1;
                }

                if(s2.getLatestTime() == null) {
                    return -1;
                }

                // song with earlier last played time will be ordered first
                long timediff = s1.getLatestTime().until( s2.getLatestTime(), NANOS );
                if(timediff == 0) {
                    return 0;
                }

                return timediff < 0 ? -1 : 1;
            }

            // Liked song < Neutral Song
            return s1.isLiked() ? -1 : 1;
        }

        // song with larger score < song with smaller score
        return score1 > score2 ? -1 : 1;
    }

}
