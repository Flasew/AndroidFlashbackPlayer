package edu.ucsd.team6flashbackplayer;

/**
 * Created by frankwang on 3/6/18.
 */

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

/**
 * Interface DownloadedFileHandlerDecorator
 * Decorator class that gives extra properties for DownloadedFileHandlerStrategy
 * Also deals with UI updates
 */
public abstract class DownloadedFileHandlerDecorator implements DownloadedFileHandlerStrategy {


    protected static final String TAG = "DownloadedFileHandlerDecorator";

    protected Context c;

    protected DownloadedFileHandlerStrategy fileHandler;  // handler to be decorated

    /**
     * Constructor that takes the file handler to be decorated.
     * @param fh file handler to be decorated
     */
    protected DownloadedFileHandlerDecorator(DownloadedFileHandlerStrategy fh) {
        this.fileHandler = fh;
    }

    /**
     * Use the handler being decorated to check extension
     * @param ext file extension
     * @return true if ext is acceptable, false otherwise
     */
    @Override
    public boolean checkExtension(String ext) {
        return fileHandler.checkExtension(ext);
    }

    /**
     * get the context of the file handler decorated.
     * @return application context
     */
    @Override
    public Context getContext() {
        return fileHandler.getContext();
    }

}
