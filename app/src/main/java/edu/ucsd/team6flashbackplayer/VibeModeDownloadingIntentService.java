package edu.ucsd.team6flashbackplayer;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.media.AudioRouting;
import android.webkit.MimeTypeMap;

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


    public VibeModeDownloadingIntentService() {
        super("VibeModeDownloadingIntentService");
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
    }

    /**
     * Intent corresponds to download requests.
     * @param intent download request
     */
    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            synchronized (this) {
                // check if is just a song or an url
                String url = intent.getStringExtra(AUTO_DOWNLOAD_REQ_URL);
                String fileExtenstion = MimeTypeMap.getFileExtensionFromUrl(url);

                if (songDownloader.getHandler().checkExtension(fileExtenstion)) {
                    songDownloader.downloadFromUrl(url);
                }
                else if (albumDownloader.getHandler().checkExtension(fileExtenstion)) {
                    albumDownloader.downloadFromUrl(url);
                }
                else {
                    throw new RuntimeException(TAG+" ERROR: unrecognized file format from firebase.");
                }
            }
        }
    }


}
