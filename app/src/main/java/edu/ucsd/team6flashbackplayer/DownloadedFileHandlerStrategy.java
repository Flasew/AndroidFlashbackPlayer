package edu.ucsd.team6flashbackplayer;

/**
 * Created by frankwang on 3/6/18.
 */

import android.content.Context;
import android.os.Environment;

import java.util.LinkedList;

/**
 * Interface DownloadedFileHandlerStrategy
 * responsible for handling a file after it's being downloaded. Depending on the different file
 * type, the handler should perform differently.
 */
public interface DownloadedFileHandlerStrategy {

    static final String BROADCAST_FILE_HANDLED = "downloadedFileHandled";

    /**
     * Check if the file extension is acceptable as the specific file type to be handled.
     * @param ext file extension
     * @return true if allowed, false otherwise
     */
    boolean checkExtension(String ext);

    /**
     * Process a downloaded file located at download directory with name filename.
     * @param filename filename of the downloaded file's source
     * @return list of files that are copied over to the music directory
     */
    LinkedList<String> process(String url, String filename);

    /**
     * Get the context associated with this file handler. Application context is expected.
     * @return context
     */
    Context getContext();

    /**
     * Make the string path given a directory and some sub-directories.
     * @param path absolute root path
     * @param subDir sub directories
     * @return absolute path of the subdirectory
     */

    default String makeDirStr(String path, String... subDir) {

        for (int i = 0; i < subDir.length; i++) {
            path += "/" + subDir[i];
        }

        return path;
    }
}
