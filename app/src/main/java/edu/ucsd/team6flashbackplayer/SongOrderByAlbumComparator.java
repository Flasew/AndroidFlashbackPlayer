package edu.ucsd.team6flashbackplayer;

class SongOrderByAlbumComparator implements SongOrderComparator {

    /**
     * Compare songs s1 and s2. The song's album alphabetically less is considered as "smaller".
     * NO INFO is considered as the greatest (last).
     * @param s2 first song
     * @param s2 second song
     * @return -1 if s1 is played before the other one, 0 if same time
     *          1 otherwise.
     */
    @Override
    public int compare(Song s1, Song s2) {

        // NO INFO get the lowest priority
        if(s1.getAlbum().equals(Song.NO_INFO)) {
            if(s2.getAlbum().equals(Song.NO_INFO)) {
                return 0;
            }
            return 1;
        }

        if(s2.getAlbum().equals(Song.NO_INFO)) {
            return -1;
        }

        return s1.getAlbum().compareTo(s1.getAlbum());
    }
}
