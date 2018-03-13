package edu.ucsd.team6flashbackplayer;

/**
 * Created by frankwang on 3/6/18.
 */

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;

/**
 * Class DownloadedSongHandler
 * Handles a downloaded song. Main function is to copy this song to music folder.
 */
public class DownloadedSongHandler implements DownloadedFileHandlerStrategy {

    private static final String TAG = "DownloadedSongHandler";

    private Context context;    // context needed for toasts
    private LocalBroadcastManager localBroadcastManager;    // broadcast manager for posting song updated

    /**
     * Constructor that takes a context.
     * @param c context
     */
    public DownloadedSongHandler(Context c) {
        this.context = c.getApplicationContext();
    }

    /**
     * Get the application context.
     * @return context
     */
    @Override
    public Context getContext() {
        return context;
    }

    /**
     * Check if the file already exist in the music folder. If not, copy over and delete
     * the copy in the download folder; otherwise remove this file.
     * @param filename filename of the downloaded file's source
     * @return list of files that's copied over to the music directory
     */
    @Override
    public LinkedList<String> process(String url, String filename) {
        // file in download and music dir. The file in download dir is gauranteed to exist.

        File fileInMusicDir = new File(makeDirStr(MusicPlayerActivity.MUSIC_DIR, filename));
        File fileInDownloadDir = new File(makeDirStr(WebMusicDownloader.DOWNLOAD_DIR, filename));

        // make song objects, add them to the global song list.
        if (!checkExtension(FilenameUtils.getExtension(filename))) {
            Log.d(TAG, "Extension: " + FilenameUtils.getExtension(filename));
            FileUtils.deleteQuietly(fileInDownloadDir);
            return null;
        }

        // if the same file (or at least with the same name) already exist in music dir,
        // remove the downloaded file and return
        if (fileInMusicDir.exists()) {
            FileUtils.deleteQuietly(fileInDownloadDir);
            return new LinkedList<>();
        }

        // otherwise move the file
        else {
            fileInDownloadDir.renameTo(fileInMusicDir);
            return new LinkedList<>(Collections.singletonList(filename));
        }
    }

    /**
     * Check if the extension is mp3.
     * @param ext file extension
     * @return true if is, false otherwise
     */
    @Override
    public boolean checkExtension(String ext) {
        return ext.toLowerCase().equals("mp3");
    }



}
