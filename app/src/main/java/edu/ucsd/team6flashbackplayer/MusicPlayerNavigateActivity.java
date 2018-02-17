package edu.ucsd.team6flashbackplayer;

/**
 * Created by frankwang on 2/17/18.
 */

import android.content.Intent;
import android.support.constraint.ConstraintLayout;
import android.widget.TextView;

/**
 * Class that include the common feature of the navigation pages,
 * including an FB-Button, a "currently playing" status bar.
 */
public abstract class MusicPlayerNavigateActivity extends MusicPlayerActivity {
    protected ConstraintLayout currSong;  // current song status bar

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
    protected void onSongUpdate(int position) {
        TextView currPlayingName = currSong.findViewById(R.id.curr_playing_name);
        TextView currPlayingArtist = currSong.findViewById(R.id.curr_playing_artist);
        Song currSong = SongList.getSongs().get(position);
        String title = currSong.getTitle();
        String artist = currSong.getArtist();
        currPlayingName.setText(title);
        currPlayingArtist.setText(artist);
    }

    /**
     * call back function when all songs finish playing.
     */
    @Override
    protected void onSongFinish() {
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
    }

    // start the current song activity page. Will not be called CurrSongActivity itself
    protected void startCurrSongActivity() {
        Intent intent = new Intent(this, CurrSongActivity.class);
        startActivity(intent);
    }

}
