package edu.ucsd.team6flashbackplayer;

/**
 * Created by frankwang on 3/6/18.
 * Edited by alice 3/9/18
 */

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Global SongList synced to Firebase database
 */
public class FirebaseSongList {

    private static List<Song> firebaseSongList = new ArrayList<Song>();

    private static FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    private static DatabaseReference songsReference = firebaseDatabase.getReference("songs");

    /**
     * Adds the song to the local user list (called on download)
     * Meaning that the song will always never already exist in the list
     * @param song Song to add
     */
    public static void addSongToLocalList(Song song) {
        // If we are calling this we know that the song doesn't already exist in the local list
        SongList.getSongs().add(song);
    }

    /**
     * Adds song to Firebase database if the song does not already exist there
     * called when songs are downloaded specifically
     * Also adds song to the FirebaseSongList locally
     * @param song Song to add to Firebase (after turning to JSON)
     */
    public static void addSongToFirebase(Song song) {
        // Reference to the specific location /songs/song.id
        DatabaseReference childReference = songsReference.child(song.getId());

        // Before adding the song with Firebase and syncing it with our local copy, check if it exists
        childReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String songObject = dataSnapshot.getValue(String.class);

                // When no Users have used the app yet
                if (songObject == null) {
                    Log.d("FirebaseSongList","Song does not exist");

                    // This adds the song to the local Firebase list
                    firebaseSongList.add(song);
                    childReference.setValue(song.getJsonString());
                }
                else {
                    // If the user already exists don't push it to Firebase
                    Log.d("FirebaseSongList","Song already exists");
                    // Still add it to the local Firebase list (since this function is called on download)
                    firebaseSongList.add(song);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                //
            }
        });
    }

    /**
     * Populate the FirebaseSongList list of songs with info of songs from the Firebase database
     */
    public static void populateFromFirebase() {

        songsReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d("FirebaseSongList", "Loading in songs");
                ArrayList<String> songStringList = new ArrayList<>();

                for (DataSnapshot postSnapshot: dataSnapshot.getChildren()) {
                    String songString = (String) postSnapshot.getValue();
                    Log.d("FirebaseSongList", "Song loaded: " + songString);
                    songStringList.add(songString);
                }

                for (String songString : songStringList) {
                    // Parse into Song object from JSON string received from Firebase
                    // This is into a completely new Song object (no fields initialized)
                    Song newSong = SongJsonParser.jsonPopulateFromFirebase(songString);
                    // Add to the global Firebase list (this list)
                    firebaseSongList.add(newSong);
                }
                for (Song a : firebaseSongList) {
                    Log.d("Song", a.getId() + a.getLastPlayedUserUid());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                //
            }
        });
    }

    /**
     * when called, changes the preference saved in the Firebase database
     * for a particular song for the logged in user ONLY
     * @param song song to change the preference for
     */
    public static void changePreference(Song song) {
        // Get the current user to only update for logged in user
        User currentUser = User.getSelf();
        // Reference specifically to user and the song in the userPref list
        // /users/user.id/songListPref/song.id
        DatabaseReference userRefSong = firebaseDatabase.getReference("users")
                .child(currentUser.getId()).child("songListPref").child(song.getId());

        // Create an ArrayList with updated boolean values for liked and disliked
        ArrayList<Boolean> bList = new ArrayList<>();
        bList.add(song.isLiked());
        bList.add(song.isDisliked());
        // Set the songListPref for that specific song in the User "object" in Firebase
        userRefSong.setValue(bList);

        Log.d("Preferences changed for: ", song.getId() + currentUser.getFullName());
    }

    /**
     * Updates a particular song's time/loc history after it stops playing
     * This information is updated in the local lists here as well as pushed to Firebase database
     * @param s the song whose info will be updated here
     * @param time latest time that the song was played
     * @param loc latest location where song was played
     */
    public static void updateHistory(Song s, ZonedDateTime time, LatLng loc) {

        // First get the Song object in FirebaseSongList that contains the same s.id
        // We need to get the URL from this which is important to be STORED and maintained in firebase
        String id = s.getId();
        String url = "";
        for (Song song : firebaseSongList) {
            if(song.getId().equals(s.getId())) {
                url = song.getUrl();
                break;
            }
        }
        // Set the url to the one from Firebase
        s.setUrl(url);

        // Update the latest time, and location/location history if not null
        s.setLatestTime(time);

        if (loc != null) {
            s.getLocHist().add(loc);
            s.setLatestLoc(loc);
        }

        // Get the current user
        User currentUser = User.getSelf();
        String lastPlayedID = currentUser.getId();
        s.setLastPlayedUserUid(lastPlayedID);

        // Update the jsonString
        SongJsonParser.refreshJsonFirebase(s);

        // (Testing) firebase list [setting song fields like this updates in both lists..]
        for(Song a : firebaseSongList)
        {
            if(a.getId().equals(s.getId())) {
                Log.d("Updated id and time", a.getLastPlayedUserUid() + a.getLatestTime());
            }
        }

        // Now just need to push new changes to FirebaseDatabase
        DatabaseReference userRefSong = firebaseDatabase.getReference("songs").child(s.getId());

        userRefSong.setValue(s.getJsonString());
    }


    /**
     * Adds a song id to user play history in Firebase and the local User object/list
     * (called after song finishes playing)
     * @param s the Song that was just played and needs to be added to user play history
     */
    public static void updateUserHistory(Song s) {
        // Get the current user
        User currentUser = User.getSelf();

        // check if the s.id already exists within the user's play history (if so then return)
        if (currentUser.getSongListPlayed().contains(s.getId())) {
            return;
        }

        // Update the current user object (songListPlayed field)
        currentUser.getSongListPlayed().add(s.getId());

        // Path of /users/user.id/songListPlayed
        DatabaseReference listPlayedRef = firebaseDatabase.getReference("users")
                .child(currentUser.getId()).child("songListPlayed");

        // Update with the list we just modified
        listPlayedRef.setValue(currentUser.getSongListPlayed());
    }


    // Getter for songs
    public static List<Song> getSongs() {
        return firebaseSongList;
    }

}