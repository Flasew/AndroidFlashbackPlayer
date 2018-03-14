package edu.ucsd.team6flashbackplayer;

import java.util.ArrayList;
import java.util.List;

/**
 * Factory class for making a sorted song list.
 */
public class SongSortedListFactory {

    public enum SortType {LAST_PLAYED, SONGNAME, FAVORITE, ALBUMNAME, ARTISTNAME}

    private SongOrderComparator comparator; // comparator used to sort the list
    private SortType sortType;

    /**
     * Factory default: sort using last played by info.
     */
    public SongSortedListFactory() {
        sortType = SortType.LAST_PLAYED;
    }

    /**
     * Set the sort type
     * @param sortType
     */
    public void setSortType(SortType sortType) {
        this.sortType = sortType;
    }

    /**
     * return a sorted Song list of the songlist snap passed in.
     * @return as stated
     */
    public List<Song> makeList(List<Song> inList) {
        List<Song> snapshot = new ArrayList<>(inList);
        snapshot.sort(getComparator(sortType));
        return snapshot;
    }

    /**
     * return a sorted Song list of the songlist snap passed in.
     * wrapper for set-sort
     * @param sortType intended sorting method.
     * @return as stated
     */
    public List<Song> makeList(List<Song> inList, SortType sortType) {
        List<Song> snapshot = new ArrayList<>(inList);
        setSortType(sortType);
        snapshot.sort(getComparator(sortType));
        return snapshot;
    }

    /**
     * get a comparator for the sorting method
     * @param sortType sorting method
     * @return comparator correspond to that method
     */
    private SongOrderComparator getComparator(SortType sortType) {
        switch (sortType) {
            case FAVORITE:      return new SongOrderByFavoriteStatusComparator();
            case SONGNAME:      return new SongOrderByTitleComparator();
            case ALBUMNAME:     return new SongOrderByAlbumComparator();
            case ARTISTNAME:    return new SongOrderByArtistComparator();
            default: case LAST_PLAYED:   return new SongOrderByLastPlayedComparator();

        }
    }


}
