package edu.ucsd.team6flashbackplayer;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

/**
 * Created by frankwang on 2/13/18.
 */

/**
 * Base activity class for Most MusicPlayer activities. Commonly contains a songUpdateBroadCastReceiver
 * for receiving song updates and broadcasts a request update request when starts.
 * Other functions can be added as needed (e.g. Service Binding)
 */
public abstract class MusicPlayerActivity extends AppCompatActivity {

    // global strings
    // if the service should finish the current song for a new start command.
    static final String MUSIC_DIR = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).getAbsolutePath();
    static final String START_MUSICSERVICE_VIBE_MODE = "vibeModeStart";
    static final String START_MUSICSERVICE_KEEP_CURRPLAY = "cutPlaying";
    static final String BROADCAST_REQUEST_SONG_UPDATE = "reqUpdate";
    static final String FLASHBACK_SHAREDPREFERENCE_NAME = "mode";
    static final String NO_INFO = "---";

    // Log tag
    protected String TAG = "MusicPlayerActivity";

    // broadcast receiver for currently playing song updated
    protected BroadcastReceiver songUpdateBroadCastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "Broadcast received");
            int pos = intent.getIntExtra(MusicPlayerService.BROADCAST_SONG_CHANGE_POSITION, -1);
            boolean status = intent.getBooleanExtra(MusicPlayerService.BROADCAST_SONG_CHANGE_STATUS, false);
            if (pos != -1) {
                onSongUpdate(pos, status);
            }
            else {
                onAllSongsFinish();
            }
        }
    };

    // local broadcast manager
    protected LocalBroadcastManager localBroadcastManager;

    // SP for flashback mode
    protected SharedPreferences fbModeSharedPreferences;

    /**
     * Base onCreate method. Get the local broadcast listener and FB-mode SP.
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        localBroadcastManager = LocalBroadcastManager.getInstance(this);
        fbModeSharedPreferences = getSharedPreferences(FLASHBACK_SHAREDPREFERENCE_NAME, MODE_PRIVATE);

    }

    /**
     * Base on start method. Register the songUpdateBroadCastReceiver
     */
    @Override
    protected void onStart() {
        super.onStart();
        localBroadcastManager.registerReceiver(songUpdateBroadCastReceiver,
                new IntentFilter(MusicPlayerService.BROADCAST_SONG_CHANGE)
        );
    }

    /**
     * Request a song information update on resume of the activites.
     */
    @Override
    protected void onResume() {
        super.onResume();
        broadcastRequestSongUpdate();
    }

    /**
     * On activity stop, unregister the update broadcast receiver.
     */
    @Override
    protected void onStop() {
        localBroadcastManager.unregisterReceiver(songUpdateBroadCastReceiver);
        super.onStop();
    }

    /**
     * Broadcast a request to update currently playing song information.
     * This is needed for update of UI on activity switch
     */
    protected void broadcastRequestSongUpdate() {
        Log.d(TAG, "Requested song info update.");
        Intent intent = new Intent(BROADCAST_REQUEST_SONG_UPDATE);
        localBroadcastManager.sendBroadcast(intent);
    }

    /**
     * When a song is updated, onSongUpdate should update all the relevant UI.
     * need to be implemented in specific class since there's view change
     * @param position position of the song in the global song list.
     * @param status is the song playing
     */
    protected abstract void onSongUpdate(int position, boolean status);

    /**
     * When all songs finished playing in this positionlist, -1 will be broadcasted
     * and this method will be called to update the UI.
     */
    protected abstract void onAllSongsFinish();

}
