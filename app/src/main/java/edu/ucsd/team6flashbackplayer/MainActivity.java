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
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends MusicPlayerActivity {

    private static final String TAG = "MainActivity";
    private static final int FBPLAYER_PERMISSIONS_REQUEST_LOCATION = 999;
    // protected static MyLocListener tracker;
    private ConstraintLayout currSong;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // set title of this activity
        setTitle(R.string.main_activity_title);

        // Check for/request location permission
        requestLocationPermission();

        // Start keeping track of location
        // tracker = new MyLocListener();
        // LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        // lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, tracker);

        setContentView(R.layout.activity_main);

        List<String> songPaths = new ArrayList<>();
        listAssetFiles("", songPaths);

        new SongList(getSongList(songPaths));
        new AlbumList(SongList.getSongs());

        // fake location and time for testing
        // for(Song s: SongList.getSongs()) {
        //    s.setLatestTime(ZonedDateTime.now());
        //    s.setLatestLoc(new LatLng(32.8812, -117.2374));
        // }

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

        currSong = findViewById(R.id.current_song);
        currSong.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startCurrSongActivity();
            }
        });

        final SharedPreferences sp = getSharedPreferences("mode", MODE_PRIVATE);
        final SharedPreferences.Editor editor = sp.edit();
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
        boolean flashBackMode = sp.getBoolean("mode", false);
        if (flashBackMode) {
            startCurrSongActivity();
        }
    }

    public void requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

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

                    Log.d(TAG(), "Location permission granted");

                }
            }
        }
    }

    @Override
    protected void onSongUpdate(int position) {
        TextView currPlayingName = currSong.findViewById(R.id.curr_playing_name);
        TextView currPlayingArtist = currSong.findViewById(R.id.curr_playing_artist);
        Song currSong = SongList.getSongs().get(position);
        String title = currSong.getTitle();
        String artist = currSong.getArtist();
        currPlayingName.setText((title == null) ? "---" : title);
        currPlayingArtist.setText((artist == null) ? "---" : artist);
    }

    @Override
    protected void onSongFinish() {
        TextView currPlayingName = currSong.findViewById(R.id.curr_playing_name);
        TextView currPlayingArtist = currSong.findViewById(R.id.curr_playing_artist);
        currPlayingName.setText(NO_INFO);
        currPlayingArtist.setText(NO_INFO);
    }

    public void startSongActivity() {
        Intent intent = new Intent(this, SongActivity.class);
        startActivity(intent);
    }

    public void startAlbumActivity() {
        Intent intent = new Intent(this, AlbumActivity.class);
        startActivity(intent);
    }

    private boolean listAssetFiles(String path, List<String> result) {
        Log.d(TAG, "In List assets\n");
        String [] list;
        try {
            list = getAssets().list(path);
            if (list.length > 0) {
                // This is a folder
                for (String file : list) {
                    String fname = (path.equals("")) ? path + file : path + "/" + file;
                    Log.d(TAG, fname+"\n");

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
                /*
                // Try to get the song information from Shared Preferences metadata
                SharedPreferences sharedPref = getSharedPreferences("metadata", MODE_PRIVATE);
                String jsonInfo = sharedPref.getString(toAdd.getId(), null);
                // Check if it exists or not - if not then we need to create it in the SharedPreferences
                if (jsonInfo == null) {
                    Log.d("null","Yes");
                    // Add the initial metadata of the song to the shared preferences for metadata
                    SharedPreferences.Editor editor = sharedPref.edit();
                    // The info is keyed on the ID of the song(path name) and the json string is created on construction
                    editor.putString(toAdd.getId(), toAdd.getJsonString());
                    editor.apply();
                }
                // Else get the data and save it to the Song's fields
                else {
                    Log.d("notnull", jsonInfo);
                    toAdd.jsonPopulate(jsonInfo);
                }
                */
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        return songList;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        //Toast.makeText(MainActivity.this,"asdf",Toast.LENGTH_SHORT).show();
        //stopService(new Intent(getApplicationContext(), MusicPlayerService.class));

    }

//    public boolean requestPermission() {
//
//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
//                != PackageManager.PERMISSION_GRANTED &&
//                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) !=
//                        PackageManager.PERMISSION_GRANTED) {
//
//            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 100);
//            return true;
//        }
//        return false;
//    }

}
