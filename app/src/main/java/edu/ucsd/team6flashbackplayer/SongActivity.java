package edu.ucsd.team6flashbackplayer;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.LogPrinter;
import android.widget.ListView;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SongActivity extends AppCompatActivity {

    private static List<Song> songList;
    private ListView songView;
    private static final String TAG = "SongActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_song);

        songView = findViewById(R.id.song_list);
        songList = getSongList();

//        AsyncSongLoader songLoader = new AsyncSongLoader();
//        songLoader.execute();
        SongAdapter songAdt = new SongAdapter(this, songList);
        songView.setAdapter(songAdt);

    }

    private boolean listAssetFiles(String path, List<String> result) {

        String [] list;
        try {
            list = getAssets().list(path);
            if (list.length > 0) {
                // This is a folder
                for (String file : list) {
                    if (!listAssetFiles(path + "/" + file, result))
                        return false;
                    else {
                        if (file.length() > 3 &&
                                file.substring(file.length() - 3).toLowerCase().equals("mp3")) {
                            result.add(file);
                        }
                    }
                }
            }
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    private List<Song> getSongList() {

        List<Song> songList = new ArrayList<>();

        // get the filenames first
        List<String> songPath = new ArrayList<>();
        listAssetFiles("", songPath);

        for(String s: songPath) {
            Log.v(TAG, s+"\n");
        }

        // load to song class with metadata
        try {
            MediaMetadataRetriever mmr = new MediaMetadataRetriever();
            for (String path: songPath) {
                AssetFileDescriptor descriptor = getAssets().openFd(path);
                mmr.setDataSource(descriptor.getFileDescriptor(), descriptor.getStartOffset(), descriptor.getLength());
                descriptor.close();

                songList.add(new Song(
                        path,
                        mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE),
                        mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST),
                        mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM)
                ));
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        return songList;
    }

//    private class AsyncSongLoader extends AsyncTask<Void, Void, ArrayList<Song>> {
//
//        ProgressDialog progressDialog;
//        ContentResolver cResolver;
//        final String TAG = "Async Loader";
//
//        @Override
//        protected Void doInBackground(Void... params) {
//
//            try {
//                song_list = getSongList();
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//
//        }
//
//        @Override
//        protected void onPostExecute(ArrayList<Song> result) {
//            song_list = result;
//            progressDialog.dismiss();
//
//        }
//
//        @Override
//        protected void onPreExecute() {
//            progressDialog = ProgressDialog.show(SongActivity.this,
//                    "ProgressDialog",
//                    "Loading songs...");
//        }
//
//        // load the song list. Many code fetched from
//        // https://android.googlesource.com/platform/development/+/master/samples/RandomMusicPlayer/
//        // src/com/example/android/musicplayer/MusicRetriever.java
//        public List<Song> getSongList() {
//
//            List<Song> songList = new ArrayList<>();
//
//            Uri uri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
//            Log.i(TAG, "Querying media...");
//            Log.i(TAG, "URI: " + uri.toString());
//            // Perform a query on the content resolver. The URI we're passing specifies that we
//            // want to query for all audio media on external storage (e.g. SD card)
//            Cursor cur = cResolver.query(uri, null,
//                    MediaStore.Audio.Media.IS_MUSIC + " = 1", null, null);
//            Log.i(TAG, "Query finished. " + (cur == null ? "Returned NULL." : "Returned a cursor."));
//            if (cur == null) {
//                // Query failed...
//                Log.e(TAG, "Failed to retrieve music: cursor is null :-(");
//                return;
//            }
//            if (!cur.moveToFirst()) {
//                // Nothing to query. There is no music on the device. How boring.
//                Log.e(TAG, "Failed to move cursor to first row (no query results).");
//                return;
//            }
//            Log.i(TAG, "Listing...");
//            // retrieve the indices of the columns where the ID, title, etc. of the song are
//            int artistColumn = cur.getColumnIndex(MediaStore.Audio.Media.ARTIST);
//            int titleColumn = cur.getColumnIndex(MediaStore.Audio.Media.TITLE);
//            int albumColumn = cur.getColumnIndex(MediaStore.Audio.Media.ALBUM);
//            int durationColumn = cur.getColumnIndex(MediaStore.Audio.Media.DURATION);
//            int idColumn = cur.getColumnIndex(MediaStore.Audio.Media._ID);
//            Log.i(TAG, "Title column index: " + String.valueOf(titleColumn));
//            Log.i(TAG, "ID column index: " + String.valueOf(titleColumn));
//            // add each song to mItems
//            do {
//                Log.i(TAG, "ID: " + cur.getString(idColumn) + " Title: " + cur.getString(titleColumn));
//                songList.add(new Song(
//                        cur.getLong(idColumn),
//                        cur.getString(titleColumn),
//                        cur.getString(artistColumn),
//                        cur.getString(albumColumn)));
//            } while (cur.moveToNext());
//            Log.i(TAG, "Done querying media. MusicRetriever is ready.");
//            return songList;
//        }
//    }
}
