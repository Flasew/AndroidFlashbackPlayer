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

/**
 * Created by frankwang on 2/13/18.
 */


/**
 * Base activity class for all MusicPlayer classes. Commonly contains a songUpdateBroadCastReceiver
 * for receiving song updates and broadcasts a request update request when starts.
 * Other functions can be added as needed (e.g. Service Binding)
 */
public abstract class MusicPlayerActivity extends AppCompatActivity {

    static final String BROADCAST_REQUEST_SONG_UPDATE = "reqUpdate";
    static final String FLASHBACK_SHAREDPREFERENCE_NAME = "mode";

    // Log tag
    protected String TAG = "MusicPlayerActivity";

    // broadcast receiver for currently playing song updated
    protected BroadcastReceiver songUpdateBroadCastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.v(TAG(), "Broadcast received");
            int pos = intent.getIntExtra(MusicPlayerService.BROADCAST_SONG_CHANGE, -1);
            if (pos != -1) {
                onSongUpdate(pos);
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
        broadcastRequestSongUpdate();

    }

    @Override
    protected void onStop() {
        localBroadcastManager.unregisterReceiver(songUpdateBroadCastReceiver);
        super.onStop();
    }

    protected void broadcastRequestSongUpdate() {
        Log.v(TAG(), "Requested song info update.");
        Intent intent = new Intent(BROADCAST_REQUEST_SONG_UPDATE);
        localBroadcastManager.sendBroadcast(intent);
    }

    // start the current song activity page. Will not be called CurrSongActivity itself
    protected void startCurrSongActivity() {
        Intent intent = new Intent(this, CurrSongActivity.class);
        startActivity(intent);
    }

    // get the correct String for log
    protected String TAG() {
        return TAG;
    }

    // need to be implemented in specific class since there's view change
    protected abstract void onSongUpdate(int position);

}
