package edu.ucsd.team6flashbackplayer;

import android.content.Intent;
import android.media.MediaMetadataRetriever;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.io.FileInputStream;
import java.io.InputStream;
import java.security.MessageDigest;
import java.util.LinkedList;

public class VibeModeDownloadedFileHanlderDecorator extends DownloadedFileHandlerDecorator {

    private LocalBroadcastManager localBroadcastManager;
    private static final String TAG = VibeModeDownloadedFileHanlderDecorator.class.getName();
    static final String VIBE_FILE_FINISHED_PROCESS = "vibeModeFileFinishedProcessing";

    /**
     * constructor just delegate to super since it only sets a field.
     * @param fileHandler file handler to be decorated
     */
    public VibeModeDownloadedFileHanlderDecorator(DownloadedFileHandlerStrategy fileHandler) {
        super(fileHandler);
        localBroadcastManager = LocalBroadcastManager.getInstance(getContext());
    }

    @Override
    public LinkedList<String> process(String url, String filename) {
        LinkedList<String> copiedFiles = fileHandler.process(url, filename);

        if (copiedFiles == null)
            return null;

        // make song objects, add them to the global song list.

        for (String path: copiedFiles) {
            String fullpath = makeDirStr(MusicPlayerActivity.MUSIC_DIR, path);
            String md5 = getMd5OfFile(fullpath);

            Log.d(TAG, "Processing " + fullpath);
            Song toAdd = null;
            for (Song s : FirebaseSongList.getSongs()) {
                if (s.getId().equals(md5))
                    toAdd = s;
            }

            if (toAdd == null) {
                // something is seriously wrong.
                throw new RuntimeException(TAG + ": ERROR: didn't find the downloaded song in firebase list");
            }

            SongList.addSong(toAdd);
            AlbumList.addFromSong(toAdd);

            Log.d(TAG,"Downloaded song id is: " + md5);
            Log.d(TAG,"Song is titled " + toAdd.getTitle());
        }

        Intent intent = new Intent(VIBE_FILE_FINISHED_PROCESS);
        localBroadcastManager.sendBroadcast(intent);

        return copiedFiles;
    }

    /**
     * Generate a MD5 Hash value based on the file located at a given file path
     * Adapted from https://stackoverflow.com/questions/13152736/
     * TODO: put this method at appropriate place.
     * @param filePath the location of the file to get a hash value for
     * @return the String that is the MD5 hash
     */
    private static String getMd5OfFile(String filePath)
    {
        String returnVal = "";
        try
        {
            InputStream input   = new FileInputStream(filePath);
            byte[]        buffer  = new byte[1024];
            MessageDigest md5Hash = MessageDigest.getInstance("MD5");
            int           numRead = 0;
            while (numRead != -1)
            {
                numRead = input.read(buffer);
                if (numRead > 0)
                {
                    md5Hash.update(buffer, 0, numRead);
                }
            }
            input.close();

            byte [] md5Bytes = md5Hash.digest();
            for (int i=0; i < md5Bytes.length; i++)
            {
                returnVal += Integer.toString( ( md5Bytes[i] & 0xff ) + 0x100, 16).substring( 1 );
            }
        }
        catch(Throwable t) {t.printStackTrace();}
        return returnVal.toUpperCase();
    }
}
