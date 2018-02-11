package edu.ucsd.team6flashbackplayer;

public class SongPreference {

    public SongPreference() {}

    public static boolean like(Song song) {
        // not liked: like the song and untoggle dislike
        if (!song.isLiked()) {
            song.setLike(true);
            song.setDislike(false);
        }
        // other wise, the song is already liked so un-toggle the like.
        else {
            song.setLike(false);
        }
        return song.isLiked();
    }

    // similar
    public static boolean dislike(Song song){
        // not liked: like the song and untoggle dislike
        if (!song.isDisliked()) {
            song.setLike(false);
            song.setDislike(true);
        }
        // other wise, the song is already liked so un-toggle the like.
        else {
            song.setDislike(false);
        }
        return song.isDisliked();
    }

}
