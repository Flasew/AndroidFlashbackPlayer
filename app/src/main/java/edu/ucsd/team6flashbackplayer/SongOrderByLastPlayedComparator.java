package edu.ucsd.team6flashbackplayer;

import java.util.ArrayList;
import java.util.List;

import static java.time.temporal.ChronoUnit.NANOS;

/**
 * Order songs by last played time
 *
 * Comparator takes song index in the a SongList
 */
class SongOrderByLastPlayedComparator implements SongOrderComparator {


    /**
     * Compare songs s1 and s2. The song played more recently is considered as "smaller"
     * @param s2 first song
     * @param s2 second song
     * @return -1 if SongList.getSongs().get(i1) is played before the other one, 0 if same time
     *          1 otherwise.
     */
    @Override
    public int compare(Song s1, Song s2) {
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
}
