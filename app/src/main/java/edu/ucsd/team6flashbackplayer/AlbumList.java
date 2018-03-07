package edu.ucsd.team6flashbackplayer;

import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by frankwang on 2/10/18.
 */

/**
 * Class AlbumList.
 * This class is consist a global list of album, which is populated on application load.
 * Afterwards the entire application will use this list to get information about
 * albums.
 * The global album list is stored as a hashmap, which uses album name as the key.
 * All songs without an album will be categorized to one album.
 */
public class AlbumList {

    private static final String TAG = "AlbumList";

    // global singleton list (hashmap) of albums
    private static HashMap<String, Album> albums;

    /**
     * Takes a global list of song and generate an album list.
     * @param songs list of all the songs available
     */
    public static void initAlbumList(List<Song> songs) {
        Log.d(TAG, "Creating global album list...");
        albums = new HashMap<>();
        addFromSongList(songs);
    }

    /**
     * Get the list of albums. Only return the values of the hashmap as an
     * arraylist.
     * @return arraylist of all albums.
     */
    public static ArrayList<Album> getAlbums() {
        return new ArrayList<Album>(albums.values());
    }

    /**
     * Get a specific album by the album name.
     * @param albumName name of the album.
     * @return album corresponding to this name.
     */
    public static Album getAlbum(String albumName) {
        return albums.get(albumName);
    }

    /**
     * Load the album list from a song list.
     * @param songs the list of all the songs.
     */
    public static void addFromSongList(List<Song> songs) {
        for (Song s: songs) {
            addFromSong(s);
        }
    }

    /**
     * add a song to the album list
     * @param song song to be added
     */
    public static void addFromSong(Song song) {
        String albumName = song.getAlbum();
        Album album = albums.get(albumName);
        // if we've never seen this album, create it and add the song.
        // other wise just add the song.
        if (album == null) {
            album = new Album(albumName);
            album.addSong(song);
            albums.put(albumName, album);
            Log.d(TAG, "Added album " + album.getName());
            Log.d(TAG, "Added song " + song.getTitle() + " to album " + album.getName());
        }
        else {
            Log.d(TAG, "Added song " + song.getTitle() + " to album " + album.getName());
            album.addSong(song);
        }
    }
}
