package edu.ucsd.team6flashbackplayer;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.AppCompatImageButton;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

import static android.content.Context.MODE_PRIVATE;

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
    // gets the context of the activities where the buttons are located to listen for values
    private Context buttonContext; // needed for use of SharedPreferences outside of Activities


    private static final String TAG = "PreferenceButtons";

    // constructor should handle initializing UI and set listener
    public PreferenceButtons(Song s, ImageButton like, ImageButton dislike, Context context) {
        song = s;
        likeButton = like;
        dislikeButton = dislike;
        buttonContext = context;
        Log.d(TAG, "Buttons constructed for " + song.getTitle());
    }

    // Constructor without a song. Mainly for buttons that just hangs there with no song playing
    public PreferenceButtons(ImageButton like, ImageButton dislike, Context context) {
        likeButton = like;
        dislikeButton = dislike;
        buttonContext = context;
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
                // Actual setting of class values is done here
                SongPreference.like(song);
                // Updated SharedPreferences to account for Like change
                updateSharedPreferences();
                //Log.d("LIKE", song.getJsonString());
                redrawButtons();
            }
        });

        dislikeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SongPreference.dislike(song);
                // Updated SharedPreferences to account for Dislike change
                updateSharedPreferences();

                redrawButtons();
            }
        });
        Log.d(TAG, "Button listener set for " + song.getTitle());
    }

    /**
     * Called when like/dislike buttons are clicked and updates the shared preferences of the
     * song where the buttons were clicked to the proper values (based on which buttons)
     */
    public void updateSharedPreferences() {
        SharedPreferences sp = buttonContext.getSharedPreferences("metadata",MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        // refresh the json object with new values for the Song object
        song.refreshJson();
        editor.putString(song.getId(),song.getJsonString());
        editor.apply();
    }

}
