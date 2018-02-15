package edu.ucsd.team6flashbackplayer;

import com.google.android.gms.maps.model.LatLng;

import java.time.ZonedDateTime;
import java.util.Comparator;

import static java.time.temporal.ChronoUnit.NANOS;

/**
 * Created by HRZhang on 2/14/2018.(I know, I have no life...)
 */

public class SongScoreComparator implements Comparator<Song> {

    private LatLng currLoc;
    private ZonedDateTime currTime;

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
     * @param s1 first song to compare
     * @param s2 second song to compare
     * @return -1 if first song should be ordered earlier in flashback
     *          1 if second song should be ordered earlier in flashback
     *          0 theoretically when two song are identical in order, should never occur
     */
    @Override
    public int compare( Song s1, Song s2 ){
        if( SongScoreCalculator.calcScore(s1, currLoc, currTime)
                == SongScoreCalculator.calcScore(s2, currLoc, currTime) ){
            if( s1.isLiked() == s2.isLiked() ){
                // song with earlier last played time will be ordered first
                return s1.getLatestTime().until( s2.getLatestTime(), NANOS ) < 0 ? -1 : 1;
            }

            // Liked song < Neutral Song
            return s1.isLiked()?-1:1;
        }

        // song with larger score < song with smaller score
        return SongScoreCalculator.calcScore(s1, currLoc, currTime)
                    > SongScoreCalculator.calcScore(s2, currLoc, currTime)?-1:1;
    }

}
