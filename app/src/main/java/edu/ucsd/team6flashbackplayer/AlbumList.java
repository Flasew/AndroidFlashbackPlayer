package edu.ucsd.team6flashbackplayer;

import android.content.Context;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by frankwang on 2/10/18.
 */

public class AlbumList {
    private static final HashMap<String, Album> albums = new HashMap<>();

    public AlbumList () {
    }

    public AlbumList(List<Song> songs) {
        loadFromSongList(songs);
    }

    public static ArrayList<Album> getAlbums() {
        return new ArrayList<Album>(albums.values());
    }

    public static Album getAlbum(String albumName) {
        return albums.get(albumName);
    }

    private void loadFromSongList(List<Song> songs) {
        for (Song s: songs) {
            String albumName = s.getAlbum();
            Album album = albums.get(albumName);
            if (album == null) {
                album = new Album(albumName);
                album.addSong(s);
                albums.put(albumName, album);
            }
            else {
                album.addSong(s);
            }
        }
    }
}
