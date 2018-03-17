package edu.ucsd.team6flashbackplayer;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import org.apache.commons.io.FilenameUtils;

import java.util.HashSet;

/**
 * An intent service class for downloading songs in the vibe mode.
 */
public class VibeModeDownloadingIntentService extends IntentService {

    public static final String AUTO_DOWNLOAD_REQ_URL = "urlOfDownloadRequest";

    private static final String TAG = VibeModeDownloadingIntentService.class.getName();

    // downloaders to handle downloading files
    private WebMusicDownloader songDownloader;
    private WebMusicDownloader albumDownloader;

    // the hashset represent the download requests that are already processed. Since two songs
    // can potential have the same url, this one is used to filter those songs that have the same
    // url (album) and to avoid repeat downloading
    private static HashSet<WebMusicDownloader.UrlFnamePair> downloadsQueued = new HashSet<>();

    /**
     * Default constructor required.
     */
    public VibeModeDownloadingIntentService() {
        super("VibeModeDownloadingIntentService");

    }

    /**
     * Intent corresponds to download requests. These intents are passed from vibe mode,
     * to start automatic downloading of songs and album. The service will check the file extension
     * to pass the file to the correct downloader (album or song)
     * @param intent download request
     */
    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {

            // synchronize on this to start download in order
            synchronized (this) {

                // instantiate downloader if there're not here. Use the vibemodedownloaddecorator
                // to handle the downloaded file.
                if (songDownloader == null)
                    songDownloader = new WebMusicDownloader(
                            new VibeModeDownloadedFileHandlerDecorator(
                                    new DownloadedSongHandler(this)
                            )
                    );

                if (albumDownloader == null)
                    albumDownloader = new WebMusicDownloader(
                            new VibeModeDownloadedFileHandlerDecorator(
                                    new DownloadedAlbumHandler(this)
                            )
                    );
                // check if is just a song or an album
                Bundle extra = intent.getExtras();
                String url = extra.getString(AUTO_DOWNLOAD_REQ_URL);
                Log.d(TAG, "URL passed to intent service: " + url);
                WebMusicDownloader.UrlFnamePair urlFnamePair =
                        WebMusicDownloader.getUrlFnamePair(url);
                String fileExtenstion = FilenameUtils.getExtension(urlFnamePair.filename);

                // start the download in the correct handler.
                // this operation will add the UrlFnamePair to known downloads.
                if (songDownloader.getHandler().checkExtension(fileExtenstion)) {
                    if (!downloadsQueued.contains(urlFnamePair)) {
                        Log.d(TAG, "Adding " + urlFnamePair.filename + ", " + urlFnamePair.url + " to known downloaded song");
                        songDownloader.download(urlFnamePair);
                        downloadsQueued.add(urlFnamePair);
                    }
                }
                else if (albumDownloader.getHandler().checkExtension(fileExtenstion)) {
                    if (!downloadsQueued.contains(urlFnamePair)) {
                        Log.d(TAG, "Adding " + urlFnamePair.filename + ", " + urlFnamePair.url + " to known downloaded album");
                        albumDownloader.download(urlFnamePair);
                        downloadsQueued.add(urlFnamePair);
                    }

                }
                else {
                    // songs in the firebase list really should have known extensions.
                    throw new RuntimeException(TAG+" ERROR: unrecognized file format from firebase.");
                }
            }
        }
    }


}
