package edu.ucsd.team6flashbackplayer;

import android.Manifest;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.media.MediaMetadataRetriever;
import android.os.AsyncTask;
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
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.tasks.Task;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.people.v1.People;
import com.google.api.services.people.v1.PeopleScopes;
import com.google.api.services.people.v1.model.EmailAddress;
import com.google.api.services.people.v1.model.ListConnectionsResponse;
import com.google.api.services.people.v1.model.Person;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;

import java.io.FileInputStream;
import java.io.InputStream;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
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
    private GoogleSignInOptions gso;
    // GoogleSignIn relevant information.
    private GoogleSignInClient mGoogleSignInClient;
    private GoogleSignInAccount account;
    private String serverAuthCode;
    private AsyncSetupAccount apf;    // used to prevent mem leak.

    private List<Person> friends;   // list of people fetched from google account,
                                    // might be unnecessary as a field
    private HashMap<String, String> friendsMap = new HashMap<>();

    AssetManager assetManager;

    /**
     * On create of the main activity is called on application launch. This function will handle
     * load songs and ask for permission of location.
     * @param savedInstanceState savedInstanceState
     */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        assetManager = getAssets(); // for generating alias in User

        // set title and layout of this activity
        setTitle(R.string.main_activity_title);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Trying doing this before Firebase init
        initSongAndAlbumList();

        // Initialize the list of Users from Firebase
        Users.loadUsers(this.getApplicationContext());
        // Intialize the songs from Firebase
        FirebaseSongList.populateFromFirebase(this.getApplicationContext());

        // Check for/request location permission
        requestAllPermission();

        currSong = findViewById(R.id.current_song);
        currSong.setOnClickListener(v -> startCurrSongActivity());

        setControlButtonsUI();

        Button songButton = findViewById(R.id.main_songs);
        songButton.setOnClickListener(v -> startSongActivity());

        Button albumButton = findViewById(R.id.main_albums);
        albumButton.setOnClickListener(v -> startAlbumActivity());

        // sign in this thing...
        // google sign in.
        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestServerAuthCode(getString(R.string.client_id))
                .requestEmail()
                .requestScopes(new Scope(Scopes.PROFILE),
                        new Scope(PeopleScopes.CONTACTS_READONLY),
                        new Scope(PeopleScopes.USER_EMAILS_READ),
                        new Scope(PeopleScopes.USERINFO_PROFILE))
                .requestProfile()
                .build();
        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // Check for existing Google Sign In account, if the user is already signed in
        // the GoogleSignInAccount will be non-null.
        trySilentSignIn();

        final SharedPreferences.Editor editor = fbModeSharedPreferences.edit();
        Button flashBackButton = findViewById(R.id.fb_button);

        flashBackButton.setOnClickListener(v -> {
            editor.putBoolean("mode", true);
            editor.apply();
            startCurrSongActivity();
        });

        // lanuch fb mode if it was in it.
        boolean flashBackMode = fbModeSharedPreferences.getBoolean("mode", false);
        if (flashBackMode) {
            startCurrSongActivity();
        }
    }

    private synchronized void trySilentSignIn() {
        Task<GoogleSignInAccount> task = mGoogleSignInClient.silentSignIn();

        if (task.isSuccessful()) {
            // There's immediate result available.
            Log.d(TAG, "Silent sign in succeeded");
            account = task.getResult();
            executeAccountUpdateAsync();
        } else {
            // There's no immediate result ready
            Log.d(TAG, "Silent sign in taking long...");
            task.addOnCompleteListener(t -> {
                try {
                    account = t.getResult(ApiException.class);
                    Log.d(TAG, "Silent sign in successful...");
                } catch (ApiException apiException) {
                    // You can get from apiException.getStatusCode() the detailed error code
                    // e.g. GoogleSignInStatusCodes.SIGN_IN_REQUIRED means user needs to take
                    // explicit action to finish sign-in;
                    // Please refer to GoogleSignInStatusCodes Javadoc for details
                    account = null;
                    Log.d(TAG, "Silent sign failed.");
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                    account = null;
                }
                finally {
                    executeAccountUpdateAsync();
                }
            });
        }
    }

    /**
     * Called when main activity exits.
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (apf != null) {
            apf.cancel(true);
            apf = null;
        }
        // Remove the child event listener for the sake of no memory leaks
        DatabaseReference dR = FirebaseDatabase.getInstance().getReference().child("songs");
        dR.removeEventListener(FirebaseSongList.songListener);
        dR.removeEventListener(Users.userListener);
    }


    /**
     * Create the menu of the app
     * @param menu menu object
     * @return ignored
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    /**
     * Handles menu item click. In this case both are for download.
     * @param item item clicked
     * @return result of handle
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        // enable a user time or set back to the system time.
        if (id == R.id.pick_fixed_time) {
            DialogFragment downloadDialog = new DateTimeSetterDialogFragment();
            downloadDialog.show(getFragmentManager(), getResources().getString(R.string.pick_time));
        }

        else if (id == R.id.use_sys_time){
            AppTime.unsetFixedTime();
        }

        return super.onOptionsItemSelected(item);
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

    //--------------------------------------GOOGLE SIGNIN------------------------------------------
    /**
     * Set up the up based on account. If the account is null ust update ui, otherwise also
     * populate the users.
     */
    private void setupWithAccount() {

        Log.d(TAG, "Account is null: " + (account == null) );
        if (account != null) {
            Log.d(TAG, "Account name: " + account.getDisplayName());
            Log.d(TAG, "Account email: " + account.getEmail());
        }

        if (account != null) {

            serverAuthCode = account.getServerAuthCode();
            Log.d(TAG, "Server Auth code is: " + (serverAuthCode != null ? serverAuthCode : "null"));

            try {
                People peopleService = setupPeople(MainActivity.this, serverAuthCode);
                ListConnectionsResponse response = peopleService.people().connections()
                        .list("people/me")
                        .setRequestMaskIncludeField("person.names,person.emailAddresses")
                        .execute();

                List<Person> connections = response.getConnections();

                // populate the global user list here
                if (connections != null) {
                    for (Person p : connections) {
                        if (!p.isEmpty()) {
                            List<EmailAddress> emails = p.getEmailAddresses();
                            if (emails != null) {
                                Log.d(TAG, "Friend " + p.getNames().get(0).getDisplayName() + " has email: " + emails.get(0).getValue());
                            }
                            friendsMap.put(User.EncodeString(emails.get(0).getValue()),p.getNames().get(0).getDisplayName());
                        }
                    }
                }

            }
            catch (IOException e) {
                // overtime or something. Handle properly.
                e.printStackTrace();
            }
        }
    }

    /**
     * Start the async account update thing.
     */
    private void executeAccountUpdateAsync() {
        apf = new AsyncSetupAccount(this);
        apf.execute();
    }
    /**
     * Google signin
     */
    private void signIn() {
        Log.d(TAG, "Starting sign in... ");
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    /**
     * handle google sign in result
     * @param completedTask
     */
    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {

            account = completedTask.getResult(ApiException.class);
            // Signed in successfully, show authenticated UI.
            apf = new AsyncSetupAccount(this);
            apf.execute();


        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w(TAG, "signInResult:failed code=" + e.getStatusCode());
            account = null;
            apf = new AsyncSetupAccount(this);
            apf.execute();
        }
    }

    /* This doesn't work because of the nature of Firebase asynchronous calls
    private void setGlobalUser(String id) {
        // First try to get the User from the global list
        User tryToGet = Users.getUser(id);

        // If the User doesn't exist in the global list
        if (tryToGet == null) {
            Log.d(TAG, "User does not exist " + id);
            // Create based on the account logged in
            User newUser = new User(account.getDisplayName(), id, assetManager);
            User.setSelf(newUser);
            Log.d(TAG, "Logged in user " + newUser.getId());

            // Add this user to Firebase
            userRef.child(id).setValue(newUser);
            // Also add this user to the global song list
            Users.addUser(id, newUser);
        }
        // else set current User to the User obtained from the Users list (in Firebase)
        else {
            Log.d(TAG, "User already existed in Firebase " + tryToGet.getId());
            User.setSelf(tryToGet);
        }
    } */

    /**
     * Update the UI of the sign in button region. If not signed in, display a sign in button;
     * otherwise display the welcome message.
     */
    private void updateSignInUI() {
        SignInButton signInButton = findViewById(R.id.sign_in_button);
        TextView welcomeText = findViewById(R.id.signed_in_text);

        Log.d(TAG, "Updateing sign in ui, account is null: " + (account == null));
        // if the user already signed in, display the welcome message.
        if (account != null) {
            // load in User object for the global User (from Firebase)
            User.loadUser(account, assetManager, friendsMap);
            Log.d(TAG, account.getDisplayName());
            welcomeText.setText(String.format(
                    getResources().getString(R.string.welcome_info),
                    account.getDisplayName()));
            welcomeText.setVisibility(View.VISIBLE);
            signInButton.setVisibility(View.GONE);
        }

        // otherwise show the sign in button
        else {
            signInButton.setVisibility(View.VISIBLE);
            signInButton.setOnClickListener(v -> signIn());
            welcomeText.setVisibility(View.GONE);
            signInButton.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Set up the people API. Tutorial obtained from
     * https://developers.google.com/people/v1/getting-started and
     * http://blog.iamsuleiman.com/people-api-android-tutorial-2/
     * TODO: consider put this part in async task.
     * @param context context used to setup people api
     * @param serverAuthCode server auth code of the account
     * @return people object set up
     * @throws IOException
     */
    public People setupPeople(Context context, String serverAuthCode) throws IOException {
        HttpTransport httpTransport = new NetHttpTransport();
        JacksonFactory jsonFactory = JacksonFactory.getDefaultInstance();

        // Redirect URL for web based applications.
        // Can be empty too.
        String redirectUrl = "urn:ietf:wg:oauth:2.0:oob";

        // STEP 1
        GoogleTokenResponse tokenResponse = new GoogleAuthorizationCodeTokenRequest(
                httpTransport,
                jsonFactory,
                context.getString(R.string.client_id),
                context.getString(R.string.client_sec),
                serverAuthCode,
                redirectUrl).execute();

        // STEP 2
        GoogleCredential credential = new GoogleCredential.Builder()
                .setClientSecrets(context.getString(R.string.client_id),
                        context.getString(R.string.client_sec))
                .setTransport(httpTransport)
                .setJsonFactory(jsonFactory)
                .build();

        credential.setFromTokenResponse(tokenResponse);

        // STEP 3 get the people object
        return new People.Builder(httpTransport, jsonFactory, credential).build();
    }


    //--------------------------------------PERMISSION------------------------------------------
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
                        .setPositiveButton(R.string.ok, (dialogInterface, i) -> {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(MainActivity.this,
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                                            Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                        FBPLAYER_PERMISSIONS_REQUEST_ALL);
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

    //---------------------------------------INIT SONG/ALBUMS---------------------------------------
    /**
     * Recursively list of MP3 files from a directory (path), and store the path string in the
     * result argument.
     * @param path root path
     * @param result output parameter of the strings
     * @param ids ouput for the ids generated from hashing files
     * @return true if @path is a directory, false otherwise (used for recursion)
     */
    private boolean listMp3Files(String path, List<String> result, List<String> ids) {

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

                    if (!listMp3Files(fname, result, ids))
                        return false;
                    else {
                        if (fname.length() > 3 &&
                                fname.substring(fname.length() - 3).toLowerCase().equals("mp3")) {

                            result.add(fname.replaceAll("^" + MUSIC_DIR + "/", ""));
                            ids.add(getMd5OfFile(fname));

                            Log.d("File Id", getMd5OfFile(fname));

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
     * Generate a MD5 Hash value based on the file located at a given file path
     * Adapted from https://stackoverflow.com/questions/13152736/
     * @param filePath the location of the file to get a hash value for
     * @return the String that is the MD5 hash
     */
    private static String getMd5OfFile(String filePath)
    {
        String returnVal = "";
        try
        {
            InputStream input   = new FileInputStream(filePath);
            byte[]        buffer  = new byte[1024];
            MessageDigest md5Hash = MessageDigest.getInstance("MD5");
            int           numRead = 0;
            while (numRead != -1)
            {
                numRead = input.read(buffer);
                if (numRead > 0)
                {
                    md5Hash.update(buffer, 0, numRead);
                }
            }
            input.close();

            byte [] md5Bytes = md5Hash.digest();
            for (int i=0; i < md5Bytes.length; i++)
            {
                returnVal += Integer.toString( ( md5Bytes[i] & 0xff ) + 0x100, 16).substring( 1 );
            }
        }
        catch(Throwable t) {t.printStackTrace();}
        return returnVal.toUpperCase();
    }

    /**
     * Get the list of song from the list of song paths by uusing MediaMetadataRetriever
     * For songs already have a history (i.e. sp found the history), populate such history
     * using the json parser.
     * Otherwise create a new entry in SP for this song.
     * @param songPaths list of path to all the songs
     * @param songIDs list of song ids corresponding to all song paths in songPaths
     * @return List of song
     */
    private List<Song> getSongList(List<String> songPaths, List<String> songIDs) {

        List<Song> songList = new ArrayList<>();
        Iterator<String> idIter = songIDs.iterator();

        // load to song class with metadata
        try {
            MediaMetadataRetriever mmr = new MediaMetadataRetriever();
            for (String path: songPaths) {
                Log.d(TAG, "Processing " + MUSIC_DIR + "/" + path);
                mmr.setDataSource(MUSIC_DIR + "/" + path);

                Song toAdd = new Song(
                        "",
                        path,
                        mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE),
                        mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST),
                        mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM),
                        idIter.next());

                songList.add(toAdd);

                /*
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
                }*/

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
        List<String> songIDs = new ArrayList<>();
        listMp3Files(Environment
                .getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).toString(), songPaths, songIDs);

        List<Song> songs = getSongList(songPaths, songIDs);
        SongList.initSongList(songs);
        AlbumList.initAlbumList(SongList.getSongs());
    }

    //---------------------------------------OTHER SHIT---------------------------------------

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
     * Main would do noting (no UI update) after file download.
     */
    @Override
    protected void onFileDownloaded() {

    }

    private static class AsyncSetupAccount extends AsyncTask<Void, Void, Void> {

        private WeakReference<MainActivity> associatedMainActivity;
        public AsyncSetupAccount(MainActivity m) {
            associatedMainActivity = new WeakReference<>(m);
        }

        @Override
        protected Void doInBackground(Void... ignored) {
            associatedMainActivity.get().setupWithAccount();
            return null;
        }

        @Override
        protected void onPostExecute(Void ignored) {
            Log.d(TAG, "Account async task finished...");
            associatedMainActivity.get().updateSignInUI();
        }

        @Override
        protected void onPreExecute() {
            Log.d(TAG, "Account async task started...");
        }

        @Override
        protected void onProgressUpdate(Void... ignored) {

        }
    }


}
