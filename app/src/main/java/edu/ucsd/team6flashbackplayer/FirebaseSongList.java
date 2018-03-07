package edu.ucsd.team6flashbackplayer;

/**
 * Created by frankwang on 3/6/18.
 */

/**
 * Global SongList synced to firebase.
 */
public class FirebaseSongList extends SongList {

    public static boolean addSong(SharedSong song) {
        SongList.getSongs().add(song);
        return true;
    }

}
