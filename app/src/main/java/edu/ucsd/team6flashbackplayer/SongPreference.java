package edu.ucsd.team6flashbackplayer;

import android.util.Log;

/**
 * class SongPreference
 * Used to like and dislike songs.
 */
public class SongPreference {

    private static final String TAG = "SongPreference";

    /**
     * default ctor. Unused for now since all methods are static.
     */
    public SongPreference() {}

    /**
     * When user select like of a song. depending on the current like/dislike status, this operation
     * will like the song (if the song is not-liked) and clear the dislike status,
     * or it will un-like the song if it's liked.
     * @param song song to be modified.
     */
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

    /**
     * When user select dislike of a song. depending on the current like/dislike status, this operation
     * will dislike the song (if the song is not-disliked) and clear the like status,
     * or it will un-dislike the song if it's disliked.
     * @param song song to be modified.
     */
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
