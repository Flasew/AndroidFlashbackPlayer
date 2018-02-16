package edu.ucsd.team6flashbackplayer;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.AppCompatImageButton;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

/**
 * Created by frankwang on 2/13/18.
 */

/**
 * A class represents a group of like and dislike buttons for a song.
 */
public class PreferenceButtons {

    public static final String PREF_DISLIKED_BROADCAST = "SongLikedBroadCast";

    private Song song;              // song corresponding to this  button group
    private ImageButton likeButton; // the like button
    private ImageButton dislikeButton;  // this dislike button
    private LocalBroadcastManager localBroadcastManager;    // broadcast for like and dislike

    private static final String TAG = "PreferenceButtons";

    // constructor should handle initializing UI and set listener
    public PreferenceButtons(Song s, ImageButton like, ImageButton dislike) {
        song = s;
        likeButton = like;
        dislikeButton = dislike;
        Log.d(TAG, "Buttons constructed for " + song.getTitle());
    }

    // Constructor without a song. Mainly for buttons that just hangs there with no song playing
    public PreferenceButtons(ImageButton like, ImageButton dislike) {
        likeButton = like;
        dislikeButton = dislike;
        Log.d(TAG, "Buttons constructed no song.");
    }

    // In list view, these button are get re-used, so we need to be able to change songs.
    public void setSong(Song s) {
        song = s;
    }

    // redraw the button UIs
    public void redrawButtons() {
        if (song == null) {
            likeButton.setBackgroundColor(Color.GRAY);
            dislikeButton.setBackgroundColor(Color.GRAY);
            Log.d(TAG, "Button UI Reset");
        }
        else {
            likeButton.setBackgroundColor(song.isLiked()? Color.GREEN : Color.GRAY);
            dislikeButton.setBackgroundColor(song.isDisliked()? Color.RED : Color.GRAY);
            Log.d(TAG, "Button UI set on song " + song.getTitle());
            Log.d(TAG, "Song status: Like = " + song.isLiked() + ", dislike = " + song.isDisliked());
        }

    }

    // remove the button listners
    public void removeButtonListeners() {
        likeButton.setOnClickListener(null);
        dislikeButton.setOnClickListener(null);
        Log.d(TAG, "Button listener removed.");
    }

    // set the button listeners
    public void setButtonListeners() {

        if (song == null) {
            return;
        }

        likeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SongPreference.like(song);
                redrawButtons();
            }
        });

        dislikeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SongPreference.dislike(song);
                redrawButtons();
            }
        });
        Log.d(TAG, "Button listener set for " + song.getTitle());
    }

}
