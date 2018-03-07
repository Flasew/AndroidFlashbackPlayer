package edu.ucsd.team6flashbackplayer;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.MediaMetadataRetriever;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

import java.io.File;
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
    private static final int FBPLAYER_PERMISSIONS_REQUEST_EXT_STORE = 998; // external storage
    private static final int FBPLAYER_PERMISSIONS_REQUEST_ALL = 997; // external storage

    // sign in result id code
    private static final int RC_SIGN_IN = 9000;

    // google sign in options used for sign in.
    private GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build();
    // GoogleSignIn relevant information.
    private GoogleSignInClient mGoogleSignInClient;
    private GoogleSignInAccount account;

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

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Check for/request location permission
        requestAllPermission();

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

        // google sign in.
        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // Check for existing Google Sign In account, if the user is already signed in
        // the GoogleSignInAccount will be non-null.
        account = GoogleSignIn.getLastSignedInAccount(this);

        updateUI(account);

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
     * Called when main activity exits.
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        // Commented out to keep functionality of music playing when exiting with back buttons
        // stopService(new Intent(getApplicationContext(), MusicPlayerService.class));
    }

    /**
     * Google signin
     */
    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    /**
     * Handle activity's result, for google sign in result.
     * @param requestCode result request code, tied to activity
     * @param resultCode
     * @param data
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    /**
     * handle google sign in result
     * @param completedTask
     */
    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);

            // Signed in successfully, show authenticated UI.
            updateUI(account);
        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w(TAG, "signInResult:failed code=" + e.getStatusCode());
            updateUI(null);
        }
    }

    /**
     * Update the UI of the sign in button reigon. If not signed in, display a sign in button;
     * otherwise display the welcome message.
     * @param account google account object
     */
    private void updateUI(GoogleSignInAccount account) {
        SignInButton signInButton = findViewById(R.id.sign_in_button);
        TextView welcomeText = findViewById(R.id.signed_in_text);

        // if the user already signed in, display the welcome message.
        if (account != null) {
            welcomeText.setText(String.format(
                    getResources().getString(R.string.welcome_info),
                    account.getDisplayName()));
            welcomeText.setVisibility(View.VISIBLE);
            signInButton.setVisibility(View.GONE);
        }

        // otherwise show the sign in button
        else {
            signInButton.setVisibility(View.VISIBLE);
            signInButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    signIn();
                }
            });
            welcomeText.setVisibility(View.GONE);
            signInButton.setVisibility(View.VISIBLE);
        }
    }


    /**
     * Request the permission if it's not granted.
     */
    public void requestAllPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION +
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "permission not granted, acquiring...");

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION) ||
                    ActivityCompat.shouldShowRequestPermissionRationale(this,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                new AlertDialog.Builder(this)
                        .setTitle(R.string.perm_req_title)
                        .setMessage(R.string.perm_req_txt)
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(MainActivity.this,
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                                            Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                        FBPLAYER_PERMISSIONS_REQUEST_ALL);
                            }
                        })
                        .create()
                        .show();


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        FBPLAYER_PERMISSIONS_REQUEST_ALL);
            }
        }
        else {
            initSongAndAlbumList();
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

            case FBPLAYER_PERMISSIONS_REQUEST_ALL: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    Log.d(TAG, "All permission granted");

                    initSongAndAlbumList();
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
     * Recursively list of MP3 files from a directory (path), and store the path string in the
     * result argument.
     * @param path root path
     * @param result output parameter of the strings
     * @return true if @path is a directory, false otherwise (used for recursion)
     */
    private boolean listMp3Files(String path, List<String> result) {

        Log.d(TAG, "In List File, absolute path " + path);
        File f = new File(path);
        Log.d(TAG, "In List File, is dir " + f.isDirectory());
        Log.d(TAG, "In List File, is file " + f.isFile());

        String [] list;

        try {
            list = f.list();
            Log.d(TAG, "In List File, list is null: " + (list == null));

            if (list != null) {
                // This is a folder
                for (String file : list) {
                    String fname = (path + "/" + file);
                    Log.d(TAG, fname);

                    if (!listMp3Files(file, result))
                        return false;
                    else {
                        if (fname.length() > 3 &&
                                fname.substring(fname.length() - 3).toLowerCase().equals("mp3")) {
                            result.add(fname.replaceAll("^" + MUSIC_DIR, ""));
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
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
                Log.d(TAG, "Processing " + MUSIC_DIR + "/" + path);
                mmr.setDataSource(MUSIC_DIR + "/" + path);

                Song toAdd = new Song(
                        path,
                        mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE),
                        mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST),
                        mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM));

                songList.add(toAdd);

                // Try to get the song information from Shared Preferences metadata
                SharedPreferences sharedPref = getSharedPreferences("metadata", MODE_PRIVATE);
                String jsonInfo = sharedPref.getString(toAdd.getPath(), null);
                // Check if it exists or not - if not then we need to create it in the SharedPreferences
                if (jsonInfo == null) {
                    Log.d(TAG, "SharedPref Exists: " + "Null");
                    // Add the initial metadata of the song to the shared preferences for metadata
                    SharedPreferences.Editor editor = sharedPref.edit();
                    // The info is keyed on the ID of the song(path name) and the json string is created on construction
                    editor.putString(toAdd.getPath(), toAdd.getJsonString());
                    editor.apply();
                }
                // Else get the data and save it to the Song's fields
                else {
                    Log.d(TAG,"SharedPref Exists: " + "Not Null");
                    SongJsonParser.jsonPopulate(toAdd, jsonInfo);
                }

            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return songList;
    }

    /**
     * Populate the global song and album list.
     */
    private void initSongAndAlbumList() {
        List<String> songPaths = new ArrayList<>();
        listMp3Files(Environment
                .getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).toString(), songPaths);

        SongList.initSongList(getSongList(songPaths));
        AlbumList.initAlbumList(SongList.getSongs());
    }

    /**
     * Main would do noting (no UI update) after file download.
     */
    @Override
    protected void onFileDownloaded() {

    }


}
