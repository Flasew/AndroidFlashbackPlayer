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
import android.text.Html;
import android.util.Log;
import android.util.LongSparseArray;
import android.webkit.CookieManager;
import android.webkit.URLUtil;
import android.widget.Toast;

import com.google.api.client.util.Charsets;

import org.apache.commons.io.FilenameUtils;
import org.springframework.web.util.UriUtils;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedList;


/**
 * Class WebMusicDownloader
 * Responsible for download a music related file (a song or an album) to the download folder. Once
 * the file is downloaded, the class will delegate it to the DownloadedFileHandlerStrategy to properly
 * process the downloaded file.
 */
public class WebMusicDownloader {

    public static final String DOWNLOAD_DIR =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();

    private static final String TAG = "WebMucicDownloader";

    private Context context;        // context needed to get a DownloadManager
    private DownloadManager downloadManager;    // download manager that actually handels a download
    private DownloadedFileHandlerStrategy fileHandler; // class that handles downloaded file.
    private BroadcastReceiver broadcastReceiver;    // receive the broadcast when download finishes.
    private LongSparseArray<UrlFnamePair> requestIds = new LongSparseArray<>();   // download request ids -> filenames
    private LocalBroadcastManager localBroadcastManager;

    private static final Object lockSynchronizer = new Object(); // synchronize download requests.

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
     * @param urlStr url to be downloaded
     */
    public void downloadFromUrl(String urlStr) {

        synchronized (lockSynchronizer) {
            // if the boradcast receiver if its not registered (i.e. no downloads is running), register it
            registerAutoBroadcastReceiver();


            AsyncDownloadFromUrl downloadFromUrl = new AsyncDownloadFromUrl(this);
            downloadFromUrl.execute(urlStr);

        }

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
                        Log.d(TAG, "File just downloaded: " + ufp.filename);
                        AsyncFileProcessor runner = new AsyncFileProcessor(WebMusicDownloader.this);
                        runner.execute(ufp.url, ufp.filename);
                    }

                    // if all downloads are completed, remove this broadcast listener.
                    if (requestIds.size() == 0)
                    {
                        Log.d(TAG, "All download processed, unregister broadcast receiver");
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

    public DownloadedFileHandlerStrategy getHandler() {
        return fileHandler;
    }


    /**
     * AsyncTask class for song unzipping (file processing in general)
     */
    private static class AsyncFileProcessor extends AsyncTask<String, Void, LinkedList<String>> {

        private WebMusicDownloader associatedDownloader;
        public AsyncFileProcessor(WebMusicDownloader wd) {
            associatedDownloader = wd;
        }

        @Override
        protected LinkedList<String> doInBackground(String... params) {
            return associatedDownloader.fileHandler.process(params[0], params[1]);
        }

        @Override
        protected void onPostExecute(LinkedList<String> result) {
            associatedDownloader.broadcastFileHandled();
            if (result == null)
                Toast.makeText(associatedDownloader.context, "Downloaded file is invalid.", Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(associatedDownloader.context, "New songs added to the library.", Toast.LENGTH_SHORT).show();

            associatedDownloader = null;
        }

        @Override
        protected void onPreExecute() {
            Toast.makeText(associatedDownloader.context, "Downloading finished.", Toast.LENGTH_SHORT).show();
        }

        @Override
        protected void onProgressUpdate(Void... ignored) {

        }
    }

    /**
     * Async. get file name from url and start to download it. Code segments acquired from
     * https://stackoverflow.com/questions/23069965/get-file-name-from-headers-with-downloadmanager-in-android/23164914
     */
    private static class AsyncDownloadFromUrl extends AsyncTask<String, Integer, UrlFnamePair> {

        private WebMusicDownloader associatedDownloader;
        public AsyncDownloadFromUrl(WebMusicDownloader wd) {
            associatedDownloader = wd;
        }

        protected UrlFnamePair doInBackground(String... urls)
        {
            URL url;
            String filename = null;

            // first guess from URL
            String filenameGuessed = URLUtil.guessFileName(urls[0], null, null);
            Log.d(TAG, "Filename guessed from URL: " + filenameGuessed);
            Log.d(TAG, "Extension: " + FilenameUtils.getExtension(filenameGuessed));

            if (!FilenameUtils.getExtension(filenameGuessed).equals("") && !FilenameUtils.getExtension(filenameGuessed).equals("bin"))
                return new UrlFnamePair(urls[0], filenameGuessed);

            try {
                url = new URL(urls[0]);
                String cookie = CookieManager.getInstance().getCookie(urls[0]);
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestProperty("Cookie", cookie);
                con.setRequestMethod("HEAD");
                con.setInstanceFollowRedirects(false);
                con.connect();

                String content = con.getHeaderField("Content-Disposition");
                String contentSplit[] = content.split("filename=");
                filename = contentSplit[1].replace("filename=", "").replace("\"", "").trim();
                Log.d(TAG, "Filename from URL: " + filename);
                filename = UriUtils.decode(filename, Charsets.UTF_8.toString());
            } catch (IOException | NullPointerException e1) {
                e1.printStackTrace();
                return null;
            }

            return new UrlFnamePair(urls[0], filename);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }
        @Override
        protected void onPostExecute(UrlFnamePair result) {
            if (result == null) {
                Toast.makeText(associatedDownloader.context, "Invalid URL.", Toast.LENGTH_SHORT).show();
                return;
            }
            String filename = result.filename;
            String fileExtenstion = FilenameUtils.getExtension(filename);

//            // if the extension is wrong, make a Toast and do nothing.
//            if (!fileHandler.checkExtension(fileExtenstion)) {
//                Toast.makeText(context, "Invalid file.", Toast.LENGTH_SHORT).show();
//                return;
//            }

//            String filename = URLUtil.guessFileName(url, null, fileExtenstion);
//            Log.d(TAG, "Filename of the file to be downloaded: " + filename);

            try {
                // create a downlad request.
                // parse the url to a uri
                Uri uri = Uri.parse(result.url);
                Log.d(TAG, "Parsed URI: " + uri);
                Log.d(TAG, "Filename to be downloaded: " + filename);

                DownloadManager.Request request = new DownloadManager.Request(uri);
                request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE);
                request.setTitle("Music Player Downloading");
                request.setDescription("Downloading...");
                request.setVisibleInDownloadsUi(true);
                request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "/" + filename);



                associatedDownloader.requestIds.put(associatedDownloader.downloadManager.enqueue(request),
                        UrlFnamePair.mkpair(result.url, filename));
            } catch (IllegalArgumentException e) {
                Toast.makeText(associatedDownloader.context, "Invalid URL.", Toast.LENGTH_SHORT).show();
            }
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
