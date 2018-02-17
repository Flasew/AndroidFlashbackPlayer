package edu.ucsd.team6flashbackplayer;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Button;

/**
 * Created by frankwang on 2/13/18.
 */


/**
 * Base activity class for all MusicPlayer classes. Commonly contains a songUpdateBroadCastReceiver
 * for receiving song updates and broadcasts a request update request when starts.
 * Other functions can be added as needed (e.g. Service Binding)
 */
public abstract class MusicPlayerActivity extends AppCompatActivity {

    public static final String BROADCAST_REQUEST_SONG_UPDATE = "reqUpdate";
    public static final String FLASHBACK_SHAREDPREFERENCE_NAME = "mode";
    public static final String NO_INFO = "---";

    // Log tag
    protected String TAG = "MusicPlayerActivity";
    protected Button fbButton;      // flashback mode button

    // broadcast receiver for currently playing song updated
    protected BroadcastReceiver songUpdateBroadCastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "Broadcast received");
            int pos = intent.getIntExtra(MusicPlayerService.BROADCAST_SONG_CHANGE, -1);
            if (pos != -1) {
                onSongUpdate(pos);
            }
            else {
                onSongFinish();
            }
        }
    };

    // local broadcast manager
    protected LocalBroadcastManager localBroadcastManager;

    // SP for flashback mode
    protected SharedPreferences fbModeSharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        localBroadcastManager = LocalBroadcastManager.getInstance(this);
        fbModeSharedPreferences = getSharedPreferences(FLASHBACK_SHAREDPREFERENCE_NAME, MODE_PRIVATE);

    }

    @Override
    protected void onStart() {
        super.onStart();
        localBroadcastManager.registerReceiver(songUpdateBroadCastReceiver,
                new IntentFilter(MusicPlayerService.BROADCAST_SONG_CHANGE)
        );
        // broadcast locally to request song information
        // broadcastRequestSongUpdate();

    }

    @Override
    protected void onResume() {
        super.onResume();
        broadcastRequestSongUpdate();
        Log.d(TAG, "On resume called");
    }

    @Override
    protected void onStop() {
        localBroadcastManager.unregisterReceiver(songUpdateBroadCastReceiver);
        super.onStop();
    }

    protected void broadcastRequestSongUpdate() {
        Log.d(TAG, "Requested song info update.");
        Intent intent = new Intent(BROADCAST_REQUEST_SONG_UPDATE);
        localBroadcastManager.sendBroadcast(intent);
    }

    // When a song is updated, onSongUpdate should update all the relevant UI.
    // need to be implemented in specific class since there's view change
    protected abstract void onSongUpdate(int position);

    // When all songs finished playing in this positionlist, -1 will be broadcasted
    // and this method will be called to update the UI.
    protected abstract void onSongFinish();

}
