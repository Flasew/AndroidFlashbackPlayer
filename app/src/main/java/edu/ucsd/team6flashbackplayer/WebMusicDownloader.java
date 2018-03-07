package edu.ucsd.team6flashbackplayer;

/**
 * Created by frankwang on 3/6/18.
 */

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.util.LongSparseArray;
import android.webkit.MimeTypeMap;
import android.webkit.URLUtil;
import android.widget.Toast;


/**
 * Class WebMusicDownloader
 * Responsible for download a music related file (a song or an album) to the download folder. Once
 * the file is downloaded, the class will delegate it to the DownloadedFileHandlerStrategy to properly
 * process the downloaded file.
 */
public class WebMusicDownloader {

    static final String DOWNLOAD_DIR = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();

    private static final String TAG = "WebMucicDownloader";

    private Context context;        // context needed to get a DownloadManager
    private DownloadManager downloadManager;    // download manager that actually handels a download
    private DownloadedFileHandlerStrategy fileHandler; // class that handles downloaded file.
    private BroadcastReceiver broadcastReceiver;    // receive the broadcast when download finishes.
    private LongSparseArray<UrlFnamePair> requestIds = new LongSparseArray<>();   // download request ids -> filenames
    private LocalBroadcastManager localBroadcastManager;

    /**
     * Constructor. Acquire a download manager object from the context.
     * @param fh class that handles the downloaded file.
     */
    public WebMusicDownloader(DownloadedFileHandlerStrategy fh) {
        context = fh.getContext();
        downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        localBroadcastManager = LocalBroadcastManager.getInstance(context);
        fileHandler = fh;
    }

    /**
     * Download a file from the given url
     * @param url url to be downloaded
     */
    public void downloadFromUrl(String url) {

        // if the boradcast receiver if its not registered (i.e. no downloads is running), register it
        registerAutoBroadcastReceiver();

        // parse the url to a uri
        Uri uri = Uri.parse(url);
        Log.d(TAG, "Parsed URI: " + uri);

        String fileExtenstion = MimeTypeMap.getFileExtensionFromUrl(url);

        // if the extension is wrong, make a Toast and do nothing.
        if (!fileHandler.checkExtension(fileExtenstion)) {
            Toast.makeText(context, "Invalid file.", Toast.LENGTH_SHORT).show();
            return;
        }

        String filename = URLUtil.guessFileName(url, null, fileExtenstion);
        Log.d(TAG, "Filename of the file to be downloaded: " + filename);

        // create a downlad request.
        DownloadManager.Request request = new DownloadManager.Request(uri);
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE);
        request.setTitle("Music Player Downloading");
        request.setDescription("Downloading " + filename);
        request.setVisibleInDownloadsUi(true);
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "/"  + filename);


        requestIds.put(downloadManager.enqueue(request), UrlFnamePair.mkpair(url, filename));

    }

    /**
     * Register the if its not registered (i.e. no downloads is running)
     */
    private void registerAutoBroadcastReceiver() {
        if (broadcastReceiver == null) {
            broadcastReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {

                    // download global reference ID
                    long referenceId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
                    UrlFnamePair ufp = requestIds.get(referenceId);
                    requestIds.delete(referenceId);

                    // if the file does belong to this download session, process the downloaded file.
                    if (ufp.filename != null) {
                        AsyncFileProcessor runner = new AsyncFileProcessor(WebMusicDownloader.this);
                        runner.execute(ufp.url, ufp.filename);
                    }

                    // if all downloads are completed, remove this broadcast listener.
                    if (requestIds.size() == 0)
                    {
                        context.unregisterReceiver(broadcastReceiver);
                        broadcastReceiver = null;
                    }
                }
            };
            context.registerReceiver(broadcastReceiver,
                    new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
        }
    }


    /**
     * Send a broadcast to notify UI update
     */
    public void broadcastFileHandled() {
        Log.d(TAG, "Broadcasting song info update.");
        Intent intent = new Intent(DownloadedFileHandlerStrategy.BROADCAST_FILE_HANDLED);
        localBroadcastManager.sendBroadcast(intent);
    }


    private static class AsyncFileProcessor extends AsyncTask<String, Void, Void> {

        private WebMusicDownloader associatedDownloader;
        public AsyncFileProcessor(WebMusicDownloader wd) {
            associatedDownloader = wd;
        }

        @Override
        protected Void doInBackground(String... params) {
            associatedDownloader.fileHandler.process(params[0], params[1]);
            return null;
        }

        @Override
        protected void onPostExecute(Void ignored) {
            associatedDownloader.broadcastFileHandled();
        }

        @Override
        protected void onPreExecute() {

        }

        @Override
        protected void onProgressUpdate(Void... ignored) {

        }
    }

    /**
     * I just need a pair which Java for some reason doesn't have... fine.
     */
    private static class UrlFnamePair {
        String url;
        String filename;
        public UrlFnamePair(String u, String f) {
            url = u;
            filename = f;
        }

        public static UrlFnamePair mkpair(String u, String f) {
            return new UrlFnamePair(u, f);
        }
    }
}
