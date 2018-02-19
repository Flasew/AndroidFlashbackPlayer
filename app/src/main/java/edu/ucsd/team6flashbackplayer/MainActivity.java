package edu.ucsd.team6flashbackplayer;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.media.MediaMetadataRetriever;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * class MainActivity
 * This class corresponds to the main page of the activity. It serves as the entry point
 * of the entire application. Responsible for loading the songs, retrieving location and
 * time histories, and any permission issue.
 */
public class MainActivity extends MusicPlayerNavigateActivity {

    private static final String TAG = "MainActivity";       // debug tag
    private static final int FBPLAYER_PERMISSIONS_REQUEST_LOCATION = 999;  // location request code

    /**
     * On create of the main activity is called on application launch. This function will handle
     * load songs and ask for permission of location.
     * @param savedInstanceState savedInstanceState
     */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // set title and layout of this activity
        setTitle(R.string.main_activity_title);
        setContentView(R.layout.activity_main);

        // Check for/request location permission
        requestLocationPermission();

        List<String> songPaths = new ArrayList<>();
        listAssetFiles("", songPaths);

        SongList.initSongList(getSongList(songPaths));
        AlbumList.initAlbumList(SongList.getSongs());

        currSong = findViewById(R.id.current_song);
        currSong.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startCurrSongActivity();
            }
        });

        resetSongStatusBar();

        Button songButton = findViewById(R.id.main_songs);
        songButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startSongActivity();
            }
        });

        Button albumButton = findViewById(R.id.main_albums);
        albumButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startAlbumActivity();
            }
        });


        final SharedPreferences.Editor editor = fbModeSharedPreferences.edit();
        Button flashBackButton = findViewById(R.id.fb_button);

        flashBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editor.putBoolean("mode" , true);
                editor.apply();
                startCurrSongActivity();
            }
        });

        // lanuch fb mode if it was in it.
        boolean flashBackMode = fbModeSharedPreferences.getBoolean("mode", false);
        if (flashBackMode) {
            startCurrSongActivity();
        }
    }

    /**
     * Request the location permission if it's not granted.
     */
    public void requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Location permission not granted, acquiring...");

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                new AlertDialog.Builder(this)
                        .setTitle(R.string.loc_req_title)
                        .setMessage(R.string.loc_req_txt)
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(MainActivity.this,
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                        FBPLAYER_PERMISSIONS_REQUEST_LOCATION);
                            }
                        })
                        .create()
                        .show();


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        FBPLAYER_PERMISSIONS_REQUEST_LOCATION);
            }
        }
    }

    /**
     * Called when permission request is finished. In this case only for logging
     * permission issue.
     * @param requestCode a request code corresponding to a location
     * @param permissions permissions asked
     * @param grantResults permissions granted results
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case FBPLAYER_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    Log.d(TAG, "Location permission granted");

                }
            }
        }
    }

    /**
     * Launch the song list page, when the song button is clicked.
     */
    public void startSongActivity() {
        Intent intent = new Intent(this, SongActivity.class);
        startActivity(intent);
    }

    /**
     * Launch the Album list page, when the song button is clicked.
     */
    public void startAlbumActivity() {
        Intent intent = new Intent(this, AlbumActivity.class);
        startActivity(intent);
    }

    /**
     * Recursively get a list of .mp3 files' paths stored in the assets folder.
     * @param path root folder of the file listing
     * @param result list used to store the file paths
     * @return true if @path is a directory, false otherwise (used for recursion)
     */
    private boolean listAssetFiles(String path, List<String> result) {
        Log.d(TAG, "In List assets");
        String [] list;
        try {
            list = getAssets().list(path);
            if (list.length > 0) {
                // This is a folder
                for (String file : list) {
                    String fname = (path.equals("")) ? path + file : path + "/" + file;
                    Log.d(TAG, fname);

                    if (!listAssetFiles(fname, result))
                        return false;
                    else {
                        if (file.length() > 3 &&
                                file.substring(file.length() - 3).toLowerCase().equals("mp3")) {
                            result.add(fname);
                        }
                    }
                }
            }
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    /**
     * Get the list of song from the list of song paths by uusing MediaMetadataRetriever
     * For songs already have a history (i.e. sp found the history), populate such history
     * using the json parser.
     * Otherwise create a new entry in SP for this song.
     * @param songPaths list of path to all the songs
     * @return List of song
     */
    private List<Song> getSongList(List<String> songPaths) {

        List<Song> songList = new ArrayList<>();

        // load to song class with metadata
        try {
            MediaMetadataRetriever mmr = new MediaMetadataRetriever();
            for (String path: songPaths) {
                AssetFileDescriptor descriptor = getAssets().openFd(path);
                mmr.setDataSource(descriptor.getFileDescriptor(), descriptor.getStartOffset(), descriptor.getLength());
                descriptor.close();

                Song toAdd = new Song(
                        path,
                        mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE),
                        mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST),
                        mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM));

                songList.add(toAdd);

                // Try to get the song information from Shared Preferences metadata
                SharedPreferences sharedPref = getSharedPreferences("metadata", MODE_PRIVATE);
                String jsonInfo = sharedPref.getString(toAdd.getId(), null);
                // Check if it exists or not - if not then we need to create it in the SharedPreferences
                if (jsonInfo == null) {
                    Log.d(TAG, "SharedPref Exists: " + "Null");
                    // Add the initial metadata of the song to the shared preferences for metadata
                    SharedPreferences.Editor editor = sharedPref.edit();
                    // The info is keyed on the ID of the song(path name) and the json string is created on construction
                    editor.putString(toAdd.getId(), toAdd.getJsonString());
                    editor.apply();
                }
                // Else get the data and save it to the Song's fields
                else {
                    Log.d(TAG,"SharedPref Exists: " + "Not Null");
                    SongJsonParser.jsonPopulate(toAdd, jsonInfo);
                }

            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        return songList;
    }

    /**
     * Called when main activity exits.
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        // Commented out to keep functionality of music playing when exiting with back buttons
        // stopService(new Intent(getApplicationContext(), MusicPlayerService.class));
    }


}
