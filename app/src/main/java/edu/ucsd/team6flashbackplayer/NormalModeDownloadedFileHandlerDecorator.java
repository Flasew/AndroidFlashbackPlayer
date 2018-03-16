package edu.ucsd.team6flashbackplayer;

/**
 * Created by frankwang on 3/6/18.
 */

import android.content.SharedPreferences;
import android.media.MediaMetadataRetriever;
import android.util.Log;
import android.widget.Toast;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

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
        if (copiedFiles == null)
            return null;

        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        for (String path: copiedFiles) {
            try {
                String fullpath = makeDirStr(MusicPlayerActivity.MUSIC_DIR, path);
                Log.d(TAG, "Processing " + fullpath);
                mmr.setDataSource(fullpath);

                String id = SongJsonParser.getMd5OfFile(fullpath);

                Song toAdd = new Song(
                        url,
                        path,
                        mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE),
                        mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST),
                        mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM),
                        id);

                SongList.addSong(toAdd);
                AlbumList.addFromSong(toAdd);

                Log.d("Downloaded song id is: ", id);
                Log.d("Song is titled ", toAdd.getTitle());
                FirebaseSongList.addSongToFirebase(toAdd);

            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        return copiedFiles;
    }


}
