package edu.ucsd.team6flashbackplayer;

/**
 * Created by frankwang on 3/6/18.
 * Edited by alice 3/9/18
 */

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Global SongList synced to Firebase database
 */
public class FirebaseSongList {

    private static final String TAG = "FirebaseSongList";

    private static List<Song> firebaseSongList = new ArrayList<>();

    public static String firebaseURL = "https://cse-110-team-project-team-6.firebaseio.com/";
    private static FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance(firebaseURL);
    private static DatabaseReference songsReference = firebaseDatabase.getReference("songs");

    public static ChildEventListener songListener;

    // local broadcast manager
    protected static LocalBroadcastManager localBroadcastManager;

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
                    Log.d(TAG,"Song does not exist");

                    // This adds the song to the local Firebase list
                    firebaseSongList.add(song);
                    // Pushes to Firebase
                    childReference.setValue(song.getJsonString());
                }
                else {
                    // If the user already exists don't push it to Firebase (aka don't override)
                    Log.d(TAG,"Song already exists");
                    // Still add it to the local Firebase list (since this function is called on download)
                    firebaseSongList.add(song);
                }
                // Add the id of the song to the current user's songPref (to start to keep track of pref)
                User.addPrefToHash(song.getId());
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
    public static void populateFromFirebase(Context appContext) {

        songsReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                List<Song> localList = SongList.getSongs();

                Log.d(TAG, "Loading in songs");

                ArrayList<String> songStringList = new ArrayList<>();
                HashMap<String, Song> songMap = new HashMap<>();

                for (DataSnapshot postSnapshot: dataSnapshot.getChildren()) {
                    String songString = (String) postSnapshot.getValue();
                    Log.d(TAG, "Song loaded: " + songString);
                    songStringList.add(songString);
                }

                ArrayList<Song> tempList = new ArrayList<>();
                ArrayList<String> idTempList = new ArrayList<>();

                for (String songString : songStringList) {
                    // Parse into Song object from JSON string received from Firebase
                    Song newSong = SongJsonParser.jsonPopulateFromFirebase(songString);
                    // put in the map
                    songMap.put(newSong.getId(), newSong);
                }

                for (Song local : localList) {
                    // Get the data from the temporary Hash table of Song objects from Firebase
                    // for the ID of the current song in the localList
                    // All songs from local list will exist in Firebase - (assumption)
                    Song temp = songMap.get(local.getId());
                    local.setLatestTime(temp.getLatestTime());
                    local.setLatestLoc(temp.getLatestLoc());
                    local.setLocHist(temp.getLocHist());
                    local.setLastPlayedUserUid(temp.getLastPlayedUserUid());

                    // Add the Song that was just modified as well as the id to two temp lists
                    tempList.add(local);
                    idTempList.add(local.getId());
                }

                for (String key : songMap.keySet()) {
                    // If the key is not in the idTempList, that means it was not added to Firebase (temp) song list yet
                    if(!idTempList.contains(key)) {
                        tempList.add(songMap.get(key));
                    }
                    else {
                        // Do nothing , the Song with this key was already added to Firebase temp song list
                    }
                }

                firebaseSongList = tempList;
                for(Song a : FirebaseSongList.getSongs()) {
                    Log.d("FirebaseSongList", a.getId());
                }

                FirebaseSongList.createListener(appContext);

                /*
                for (Song a : firebaseSongList) {
                    Log.d("Song", a.getId() + a.getLastPlayedUserUid());
                }
                for (Song b : localList) {
                    Log.d("Song in local", b.getId() + b.getLastPlayedUserUid());
                }*/
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                throw databaseError.toException();
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
                .child(User.EncodeString(currentUser.getId())).child("songListPref").child(song.getId());

        // Create an ArrayList with updated boolean values for liked and disliked
        ArrayList<Boolean> bList = new ArrayList<>();
        bList.add(song.isLiked());
        bList.add(song.isDisliked());
        // Set the songListPref for that specific song in the User "object" in Firebase
        userRefSong.setValue(bList);

        Log.d(TAG,"Preferences changed for: " + song.getId() + " " + currentUser.getFullName());
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

        /* (Testing) setting song fields like this updates in both lists..
        for(Song a : firebaseSongList)
        {
            if(a.getId().equals(s.getId())) {
                Log.d("Updated id and time", a.getLastPlayedUserUid() + a.getLatestTime());
            }
        }

        for(Song b : SongList.getSongs())
        {
            if(b.getId().equals(s.getId())) {
                Log.d("Updated id and time", b.getLastPlayedUserUid() + b.getLatestTime());
            }
        }*/

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
                .child(User.EncodeString(currentUser.getId())).child("songListPlayed");

        // Update with the list we just modified
        listPlayedRef.setValue(currentUser.getSongListPlayed());
    }

    /**
     * For each song in the local list, updates the preferences based on the current user's
     * preferences that were loaded in through Firebase
     */
    public static void populatePreferences() {
        User curr = User.getSelf();
        List<Song> localList = SongList.getSongs();

        // Iterate through local list of the user
        for (Song s : localList) {
            Log.d("Setting pref", s.getId() + "," + s.isLiked() + "," + s.isDisliked() );
            // get the preferences associated with this song from the User's songListPref
            // and set the song's pref to those prefs that were saved before
            s.setLike(curr.getSongListPref().get(s.getId()).get(0));
            s.setDislike(curr.getSongListPref().get(s.getId()).get(1));
            Log.d("Setting pref after", s.getId() + ","  + s.isLiked() + "," + s.isDisliked() );
        }
        // Note there is no case for if a song doesn't match in local list but matches in Firebase
        // Because all the songs in songlistPref for a user will be the ones they have downloaded before
        // So it will have to be in Firebase but not vice versa
    }

    /**
     * Creates a listener for the /songs/ path in firebase and handles events when new songs are
     * added or when song info is modified
     * @param appContext the application context of the whole app (passed through MainActivity)
     */
    private static void createListener(Context appContext) {
        songListener = songsReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                // If a new song is added to Firebase database (meaning another user has downloaded a new song
                // while the app is running) - Add this to the Firebase song list only
                String songString = dataSnapshot.getValue(String.class);
                Song updatedSong = SongJsonParser.jsonPopulateFromFirebase(songString);

                // Check if it is not already in our Firebase song list (double adding might occur if
                // the current user downloads the song
                for (Song song : FirebaseSongList.getSongs()) {
                    if (updatedSong.getId().equals(song.getId())) {
                        return;
                    }
                }

                FirebaseSongList.getSongs().add(updatedSong);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                String songString = dataSnapshot.getValue(String.class);
                Song updatedSong = SongJsonParser.jsonPopulateFromFirebase(songString);

                // Check if the song that was updated existed in the local list
                // Aka already downloaded (firebase song list gets automatically updated from this)
                for (Song song : SongList.getSongs()) {
                    if (updatedSong.getId().equals(song.getId())) {
                        // Update the Latest time, location, location history, and last played id
                        // Because those are the fields that will/or could change if song info changes at all
                        song.setLatestTime(updatedSong.getLatestTime());
                        song.setLatestLoc(updatedSong.getLatestLoc());
                        song.setLocHist(updatedSong.getLocHist());
                        song.setLastPlayedUserUid(updatedSong.getLastPlayedUserUid());

                        Log.d(TAG, "Was updated "+ updatedSong.getId() + " " + updatedSong.getLastPlayedUserUid());

                        // Send a broadcast that the last played user id changed for a particular song
                        // This is only really relevant for a song that is already downloaded/can be played
                        localBroadcastManager = LocalBroadcastManager.getInstance(appContext);
                        Intent intent = new Intent("reqUpdate");
                        localBroadcastManager.sendBroadcast(intent);

                        return;
                    }
                }

                // If the song only existed in the Firebase song list aka wasn't downloaded yet
                // Update those fields as well
                for (Song song: FirebaseSongList.getSongs()) {
                    if (updatedSong.getId().equals(song.getId())) {
                        song.setLatestTime(updatedSong.getLatestTime());
                        song.setLatestLoc(updatedSong.getLatestLoc());
                        song.setLocHist(updatedSong.getLocHist());
                        song.setLastPlayedUserUid(updatedSong.getLastPlayedUserUid());

                        // Don't need to send a broadcast in this case
                        Log.d(TAG,"Was updated "+ updatedSong.getId() + " " + updatedSong.getLastPlayedUserUid());
                    }
                }
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                // This won't occur (except in the case of manual removal or error)
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                // This also won't occur
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                throw databaseError.toException();
            }
        });
    }

    // Getter for songs
    public static List<Song> getSongs() {
        return firebaseSongList;
    }

}
