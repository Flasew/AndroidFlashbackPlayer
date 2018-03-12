package edu.ucsd.team6flashbackplayer;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.ImageView;


/**
 * Class for pause and play/skip control buttons.
 */
public class ControlButtons {

    public static final String CTRL_BROADCAST = "SongControlBroadcast";

    // control number
    public static final int CTRL_PAUSE_PLAY = 0;
    public static final int CTRL_SKIP = 1;

    private static final boolean STATUS_PAUSE = false;
    private static final boolean STATUS_PLAY = true;

    private ImageView pausePlayButton;    // pause and Play button
    private ImageView skipButton;         // skip button

    private boolean pausePlayStatus;          // status of pause or play

    // images
    private Drawable pauseImage;
    private Drawable playImage;
    private Drawable skipImage;

    private static LocalBroadcastManager localBroadcastManager;

    private Context context;

    private static final String TAG = ControlButtons.class.getName();

    public ControlButtons(Context c, ImageView pp, ImageView s, Drawable pausei, Drawable playi, Drawable skipi) {
        context = c;
        pausePlayButton = pp;
        skipButton = s;
        pauseImage = pausei;
        playImage = playi;
        skipImage = skipi;

        pausePlayButton.setClickable(true);
        skipButton.setClickable(true);

        // skip button will always be the same
        skipButton.setImageDrawable(skipImage);

        localBroadcastManager = LocalBroadcastManager.getInstance(c);
    }

    public void setPlay() {
        pausePlayStatus = STATUS_PLAY;
        pausePlayButton.setImageDrawable(playImage);
    }

    public void setPause() {
        pausePlayStatus = STATUS_PAUSE;
        pausePlayButton.setImageDrawable(pauseImage);
    }

    public void setButtonListeners() {
        pausePlayButton.setOnClickListener(v -> {
            broadcastPlayPause();
            flipPausePlayStatus();
        });
        skipButton.setOnClickListener(v -> broadcastSkip());
    }

    private void flipPausePlayStatus() {
        if (pausePlayStatus == STATUS_PAUSE) {
            setPlay();
        }
        else {
            setPause();
        }

    }

    public void unsetButtonListeners() {
        pausePlayButton.setOnClickListener(null);
        skipButton.setOnClickListener(null);
    }

    private void broadcastSkip() {
        Intent intent = new Intent(CTRL_BROADCAST);
        intent.putExtra(CTRL_BROADCAST, CTRL_SKIP);
        localBroadcastManager.sendBroadcast(intent);
    }

    private void broadcastPlayPause() {
        Intent intent = new Intent(CTRL_BROADCAST);
        intent.putExtra(CTRL_BROADCAST, CTRL_PAUSE_PLAY);
        localBroadcastManager.sendBroadcast(intent);
    }


}
