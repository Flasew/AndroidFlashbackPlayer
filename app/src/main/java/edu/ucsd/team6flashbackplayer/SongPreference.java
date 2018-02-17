package edu.ucsd.team6flashbackplayer;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

public class SongPreference {

    private static final String TAG = "SongPreference";

    public SongPreference() {}

    public static void like(Song song) {

        Log.d(TAG, "Song " + song.getTitle() + " like preference changed.");
        Log.d(TAG, "Initial status: Like = " + song.isLiked() + ", dislike = " + song.isDisliked());
        // not liked: like the song and untoggle dislike
        if (!song.isLiked()) {
            song.setLike(true);
            song.setDislike(false);
        }
        // other wise, the song is already liked so un-toggle the like.
        else {
            song.setLike(false);
        }
        Log.d(TAG, "Final status: Like = " + song.isLiked() + ", dislike = " + song.isDisliked());

    }

    // similar
    public static void dislike(Song song){

        Log.d(TAG, "Song " + song.getTitle() + " dislike preference changed.");
        Log.d(TAG, "Initial status: Like = " + song.isLiked() + ", dislike = " + song.isDisliked());
        // not liked: like the song and untoggle dislike
        if (!song.isDisliked()) {
            song.setLike(false);
            song.setDislike(true);
        }
        // other wise, the song is already liked so un-toggle the like.
        else {
            song.setDislike(false);
        }
        Log.d(TAG, "Final status: Like = " + song.isLiked() + ", dislike = " + song.isDisliked());
    }

}
