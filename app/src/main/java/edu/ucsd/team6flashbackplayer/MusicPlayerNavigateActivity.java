package edu.ucsd.team6flashbackplayer;

/**
 * Created by frankwang on 2/17/18.
 */

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.constraint.ConstraintLayout;
import android.util.Log;
import android.widget.TextView;

/**
 * Class that include the common feature of the navigation pages,
 * including a "currently playing" status bar.
 */
public abstract class MusicPlayerNavigateActivity extends MusicPlayerActivity {
    protected ConstraintLayout currSong;  // current song status bar
    protected ControlButtons controlButtons;       // control button for skip and pause

    // broadcast receiver for currently playing song updated
    protected BroadcastReceiver fileDownloadedBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "File downloaded roadcast received");
            onFileDownloaded();
        }
    };

    /**
     * Register broadcast receiver on start
     */
    @Override
    protected void onStart() {
        super.onStart();
        localBroadcastManager.registerReceiver(fileDownloadedBroadcastReceiver,
                new IntentFilter(DownloadedFileHandlerStrategy.BROADCAST_FILE_HANDLED)
        );
    }


    /**
     * Unregister the bcast receiver on stop.
     */
    @Override
    protected void onStop() {
        super.onStop();
        localBroadcastManager.unregisterReceiver(fileDownloadedBroadcastReceiver);
    }

    /**
     * Reset the status bar on resume
     */
    @Override
    protected void onResume() {
        super.onResume();
        resetSongStatusBar();
    }
    /**
     * Callback function when a song in the playlist is updated.
     * @param position position of the new song in the global playlist.
     */
    @Override
    protected void onSongUpdate(int position, boolean status) {
        TextView currPlayingName = currSong.findViewById(R.id.curr_playing_name);
        TextView currPlayingArtist = currSong.findViewById(R.id.curr_playing_artist);
        Song currSong;
        try {
            currSong = SongList.getSongs().get(position);
        }
        catch (IndexOutOfBoundsException e) {
            try {
                currSong = FirebaseSongList.getSongs().get(position);
            }
            catch (IndexOutOfBoundsException e1) {
                return;
            }
        }
        String title = currSong.getTitle();
        String artist = currSong.getArtist();
        currPlayingName.setText(title);
        currPlayingArtist.setText(artist);

        if (status) {
            controlButtons.setPause();
        }
        else {
            controlButtons.setPlay();
        }

        controlButtons.setButtonListeners();
    }

    /**
     * call back function when all songs finish playing.
     */
    @Override
    protected void onAllSongsFinish() {
        resetSongStatusBar();
    }

    /**
     * Reset the currSong status bar, remove all displayed contents and change them to noinfo.
     */
    protected void resetSongStatusBar() {
        TextView currPlayingName = currSong.findViewById(R.id.curr_playing_name);
        TextView currPlayingArtist = currSong.findViewById(R.id.curr_playing_artist);
        currPlayingName.setText(NO_INFO);
        currPlayingArtist.setText(NO_INFO);
        controlButtons.setPlay();
        controlButtons.unsetButtonListeners();
    }

    /**
     * Set the control buttons UI
     */
    protected void setControlButtonsUI() {
        controlButtons = new ControlButtons(this,
                findViewById(R.id.pause_play),
                findViewById(R.id.skip),
                getDrawable(R.drawable.ic_pause_black_24dp),
                getDrawable(R.drawable.ic_play_arrow_black_24dp),
                getDrawable(R.drawable.ic_skip_next_black_24dp));
    }

    /**
     * start the current song activity page when the user tap on the song status bar.
     */
    protected void startCurrSongActivity() {
        Intent intent = new Intent(this, CurrSongActivity.class);
        startActivity(intent);
    }

    protected abstract void onFileDownloaded();

}
