package edu.ucsd.team6flashbackplayer;

import java.util.Comparator;

class SongOrderByFavoriteStatusComparator implements SongOrderComparator {

    /**
     * Compare songs s1 and s2. The liked song is considered as "smaller".
     * dislike is considered as the greatest (last).
     * @param s2 first song
     * @param s2 second song
     * @return -1 if s1 is played before the other one, 0 if same time
     *          1 otherwise.
     */
    @Override
    public int compare(Song s1, Song s2) {

        return  s1.isLiked() == s2.isLiked()
                    ? s1.isDisliked() == s2.isDisliked()
                        ? 0
                        : s1.isDisliked() ? 1 : -1
                    : s1.isLiked() ? -1 : 1;
    }
}
