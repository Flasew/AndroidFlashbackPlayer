package edu.ucsd.team6flashbackplayer;

import java.util.Comparator;

class SongOrderByTitleComparator implements SongOrderComparator {
    /**
     * Compare songs s1 and s2. The song's title alphabetically less is considered as "smaller".
     * NO INFO is considered as the greatest (last).
     * @param s2 first song
     * @param s2 second song
     * @return -1 if s1 is played before the other one, 0 if same time
     *          1 otherwise.
     */
    @Override
    public int compare(Song s1, Song s2) {
//        System.err.println("COMPARE CALLED IN TITLE SORT");
//        System.err.println("COMPARE " + s1 + " WITH " + s2);
        // NO INFO get the lowest priority
        if(s1.getTitle().equals(Song.NO_INFO)) {
            if(s2.getTitle().equals(Song.NO_INFO)) {
//                System.err.println("Both noinfo.");
                return 0;
            }
//            System.err.println("1 noinfo.");
            return 1;
        }

        if(s2.getTitle().equals(Song.NO_INFO)) {
//            System.err.println("2 noinfo.");
            return -1;
        }
//        System.err.println("s1.getTitle().compareTo(s1.getTitle()):" +s1.getTitle().compareTo(s1.getTitle()));

        return s1.getTitle().compareToIgnoreCase(s2.getTitle());



    }
}
