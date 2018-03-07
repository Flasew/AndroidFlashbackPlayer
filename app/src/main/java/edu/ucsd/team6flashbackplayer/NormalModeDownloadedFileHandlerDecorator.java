package edu.ucsd.team6flashbackplayer;

/**
 * Created by frankwang on 3/6/18.
 */

import android.content.SharedPreferences;
import android.media.MediaMetadataRetriever;
import android.util.Log;

import java.util.LinkedList;

import static android.content.Context.MODE_PRIVATE;

/**
 * Decorator of downloading a song in normal mode. Responsible for add the song to the global
 * Firebase playlist.
 */
public class NormalModeDownloadedFileHandlerDecorator extends DownloadedFileHandlerDecorator {

    /**
     * constructor just delegate to super since it only sets a field.
     * @param fileHandler file handler to be decorated
     */
    public NormalModeDownloadedFileHandlerDecorator(DownloadedFileHandlerStrategy fileHandler) {
        super(fileHandler);
    }

    @Override
    public LinkedList<String> process(String url, String filename) {
        LinkedList<String> copiedFiles = fileHandler.process(url, filename);

        // make song objects, add them to the global song list.

        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        for (String path: copiedFiles) {
            try {
                String fullpath = makeDirStr(MusicPlayerActivity.MUSIC_DIR, path);
                Log.d(TAG, "Processing " + fullpath);
                mmr.setDataSource(fullpath);

                SharedSong toAdd = new SharedSong(
                        url,
                        path,
                        mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE),
                        mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST),
                        mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM));

                FirebaseSongList.addSong(toAdd);
                AlbumList.addFromSong(toAdd);


                // Try to get the song information from Shared Preferences metadata
                SharedPreferences sharedPref = getContext().getSharedPreferences("metadata", MODE_PRIVATE);
                String jsonInfo = sharedPref.getString(toAdd.getPath(), null);
                // Check if it exists or not - if not then we need to create it in the SharedPreferences
                if (jsonInfo == null) {
                    Log.d(TAG, "SharedPref Exists: " + "Null");
                    // Add the initial metadata of the song to the shared preferences for metadata
                    SharedPreferences.Editor editor = sharedPref.edit();
                    // The info is keyed on the ID of the song(path name) and the json string is created on construction
                    editor.putString(toAdd.getPath(), toAdd.getJsonString());
                    editor.apply();
                }
                // Else get the data and save it to the Song's fields
                else {
                    Log.d(TAG, "SharedPref Exists: " + "Not Null");
                    SongJsonParser.jsonPopulate(toAdd, jsonInfo);
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        return copiedFiles;
    }


}
