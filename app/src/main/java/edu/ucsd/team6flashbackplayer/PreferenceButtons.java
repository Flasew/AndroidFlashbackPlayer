package edu.ucsd.team6flashbackplayer;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.support.v4.content.LocalBroadcastManager;
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

    // drawables for button icon
    private Drawable heartRed;
    private Drawable heartGrey;
    private Drawable brokenHeartRed;
    private Drawable brokenHeartGrey;

    private static LocalBroadcastManager localBroadcastManager;  // broadcast for like and dislike

    // gets the context of the activities where the buttons are located to listen for values
    private Context buttonContext; // needed for use of SharedPreferences outside of Activities

    private static final String TAG = "PreferenceButtons";


    /**
     * Constructor of PrefButtons. Associate a button set with a song and a context.
     * constructor should handle initializing UI and set listener
     * @param s song that this button set will modify
     * @param like like button
     * @param dislike dislike button
     * @param context context for reagent functions.
     */
    public PreferenceButtons(Song s, ImageButton like, ImageButton dislike, Context context) {
        song = s;
        likeButton = like;
        dislikeButton = dislike;
        buttonContext = context;

        acquireDrawables();

        Log.d(TAG, "Buttons constructed for " + song.getTitle());
    }

    /**
     * Constructor without a song. Mainly for buttons that just hangs there with no song playing
     * @param like like button
     * @param dislike dislike button
     * @param context context for reagent functions.
     */
    public PreferenceButtons(ImageButton like, ImageButton dislike, Context context) {
        likeButton = like;
        dislikeButton = dislike;
        buttonContext = context;
        acquireDrawables();
        Log.d(TAG, "Buttons constructed, no song.");

    }

    /**
     * Set a song that the button should modify
     * @param s song that this button set will modify
     */
    public void setSong(Song s) {
        song = s;
    }

    /**
     * Redraw the button UIs.
     */
    public void redrawButtons() {
        if (song == null) {
            likeButton.setBackground(heartGrey);
            dislikeButton.setBackground(brokenHeartGrey);
            Log.d(TAG, "Button UI Reset");
        }
        else {
            likeButton.setBackground(song.isLiked()? heartRed : heartGrey);
            dislikeButton.setBackground(song.isDisliked()? brokenHeartRed : brokenHeartGrey);
            Log.d(TAG, "Button UI set on song " + song.getTitle());
            Log.d(TAG, "Song status: Like = " + song.isLiked() + ", dislike = " + song.isDisliked());
        }

    }

    /**
     * Remove the button listeners.
     */
    public void removeButtonListeners() {
        likeButton.setOnClickListener(null);
        dislikeButton.setOnClickListener(null);
        Log.d(TAG, "Button listener removed.");
    }

    /**
     * Set the button listeners.
     */
    public void setButtonListeners() {

        if (song == null) {
            return;
        }

        likeButton.setOnClickListener(v -> {
                // Actual setting of class values is done here
                SongPreference.like(song);
                // Updated SharedPreferences to account for Like change
                // updateSharedPreferences();

                // Update Firebase to account for Like change
                FirebaseSongList.changePreference(song);

                redrawButtons();
        });

        // broadcast a song is disliked
        dislikeButton.setOnClickListener(v -> {
                SongPreference.dislike(song);
                if (song.isDisliked()) {
                    Intent intent = new Intent(PREF_DISLIKED_BROADCAST);
                    intent.putExtra(PREF_DISLIKED_BROADCAST, SongList.getSongs().indexOf(song));
                    localBroadcastManager.sendBroadcast(intent);
                }
                // Updated SharedPreferences to account for Dislike change
                // updateSharedPreferences();

                // Update Firebase to account for Dislike change
                FirebaseSongList.changePreference(song);
                redrawButtons();
        });
        Log.d(TAG, "Button listener set for " + song.getTitle());
    }


    /**
     * Set the local broadcast manager from a context. This is required
     * for the dislike broadcast to function.
     */
    public static void setLocalBroadcastManager(Context c) {
        localBroadcastManager = LocalBroadcastManager.getInstance(c);
    }


    /**
     * Called when like/dislike buttons are clicked and updates the shared preferences of the
     * song where the buttons were clicked to the proper values (based on which buttons)
     */
    public void updateSharedPreferences() {
        SharedPreferences sp = buttonContext.getSharedPreferences("metadata",MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        // refresh the json object with new values for the Song object
        SongJsonParser.refreshJson(song);
        editor.putString(song.getPath(),song.getJsonString());
        editor.apply();
    }

    /**
     * Load the drawable resources from current context.
     */
    private void acquireDrawables() {
        if (heartGrey == null)
            heartGrey = buttonContext.getDrawable(R.drawable.ic_heart_grey);
        if (heartRed == null)
            heartRed = buttonContext.getDrawable(R.drawable.ic_heart_red);
        if (brokenHeartGrey == null)
            brokenHeartGrey = buttonContext.getDrawable(R.drawable.ic_broken_heart_grey);
        if (brokenHeartRed == null)
            brokenHeartRed = buttonContext.getDrawable(R.drawable.ic_broken_heart_red);
    }

}
