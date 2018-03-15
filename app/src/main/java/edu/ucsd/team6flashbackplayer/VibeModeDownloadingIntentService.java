package edu.ucsd.team6flashbackplayer;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import org.apache.commons.io.FilenameUtils;

import java.util.HashSet;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class VibeModeDownloadingIntentService extends IntentService {

    public static final String AUTO_DOWNLOAD_REQ_URL = "urlOfDownloadRequest";

    private static final String TAG = VibeModeDownloadingIntentService.class.getName();

    // auto download
    private WebMusicDownloader songDownloader;
    private WebMusicDownloader albumDownloader;

    private static HashSet<WebMusicDownloader.UrlFnamePair> downloadsQueued = new HashSet<>();


    public VibeModeDownloadingIntentService() {
        super("VibeModeDownloadingIntentService");

    }

    /**
     * Intent corresponds to download requests.
     * @param intent download request
     */
    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {

            synchronized (this) {
                if (songDownloader == null)
                    songDownloader = new WebMusicDownloader(
                            new VibeModeDownloadedFileHanlderDecorator(
                                    new DownloadedSongHandler(this)
                            )
                    );

                if (albumDownloader == null)
                    albumDownloader = new WebMusicDownloader(
                            new VibeModeDownloadedFileHanlderDecorator(
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
                    throw new RuntimeException(TAG+" ERROR: unrecognized file format from firebase.");
                }
            }
        }
    }


}
