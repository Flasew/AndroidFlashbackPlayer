package edu.ucsd.team6flashbackplayer;

import android.content.Context;
import android.content.Intent;
import android.nfc.Tag;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by frankwang on 3/6/18.
 * Edited by alice 3/7/18
 */

/* class Users
*  Stores a list of all Users that have used this app before, loaded on MainActivity load
*  This is synced with Firebase Database
* */
public class Users {
    private static final String TAG = "Users";
    private static HashMap<String, User> users = new HashMap<>();

    private static FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance(FirebaseSongList.firebaseURL);
    private static DatabaseReference databaseReference = firebaseDatabase.getReference("users");

    public static ChildEventListener userListener;
    // local broadcast manager
    protected static LocalBroadcastManager localBroadcastManager;
    static final String BROADCAST_FRIEND_CHANGE = "friendInfoChanged";

    /**
     * Loads the entire list of Users from Firebase database into users
     * hashed on String(id) and User object
     * This can be used to determine Friends info. But not used to load User on app start (because of async issues)
     */
    public static void loadUsers(Context appContext) {

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d(TAG, "Loading in");
                GenericTypeIndicator<HashMap<String, User>> userGTypeInd = new GenericTypeIndicator<HashMap<String, User>>() {};
                Map<String, User> userHashMap = dataSnapshot.getValue(userGTypeInd);

                // When no Users have used the app yet
                if (userHashMap == null) {
                    Log.d(TAG,"don't exist");
                    users = new HashMap<>();
                }
                else {
                    // This is for the sake of casting the result (Value) to User object
                    HashMap<String, User> userMap = new HashMap<>();
                    for (String key : userHashMap.keySet()) {
                        User usr = (User) userHashMap.get(key);
                        // Decode the key which is the email but with ,
                        String newKey = User.DecodeString(key);
                        userMap.put(newKey,usr);
                        Log.d(TAG,"Original key, Info " + key + " " + usr.getFullName() + " " + usr.getAlias() + usr.getId());
                    }

                    // Set the field to this hashmap we obtained from Firebase
                    users = userMap;
                }
                // Put the listener on /users/ path
                Users.createUserListener(appContext);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                //
            }
        });
    }

    /**
     * (To be used for Friends) (Not to load in User on app start) - async issues
     *
     * @param uid The ID of the user we want from the global list of users
     * @return The user that has the uid passed in as a parameter
     */
    public static User getUser(String uid) {
        return users.get(uid);
    }

    /**
     * Returns the HashMap of Users and their associated IDs (encoded)
     * @return HashMap of Users and their IDs
     */
    public static HashMap<String,User> getUsers() {
        return users;
    }

    /**
     * Adds the user to the users hashmap
     * @param id id of user
     * @param user User object associated with id
     */
    public static void addUser(String id, User user) {
        users.put(id, user);
    }



    /**
     * Creates a listener for the /users/ path in firebase and handles events when new users are
     * added or when user info is updated
     * @param appContext the application context of the whole app (passed through MainActivity)
     */
    private static void createUserListener(Context appContext) {
        // Set listener on the /users/ path
        userListener = databaseReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                // When a user is added it should be added to the Users list
                User user = dataSnapshot.getValue(User.class);
                // Check if the id already exists in the existing hashMap
                if (Users.getUsers().containsKey(user.getId())) {
                    // then do nothing
                    return;
                }
                else {
                    Users.addUser(user.getId(), user);
                    Log.d(TAG, "New user added: " + user.getId());
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                // Check if noone is logged in
                if (User.getSelf() == null) {
                    Log.d(TAG, "Not logged in");
                    return;
                }
                // When some user's info changes, update the Users list

                // Also send a broadcast only if the user's info that changed is a
                // Friend of the current user and the songListPlayed field changed
                User updatedUser = dataSnapshot.getValue(User.class);
                String updatedUserId = updatedUser.getId();

                User originalUser = Users.getUser(updatedUserId);

                if (User.getSelf().getFriendsMap().containsKey(User.EncodeString(updatedUserId))) {
                    // Check if the songListPlayed is different (because preferences could change too)
                    // It's okay to use .equals here because order will be maintained if a song was not
                    // add to the list (since songs are never deleted from the songListPlayed list too)
                    if (originalUser.getSongListPlayed().equals(updatedUser.getSongListPlayed())) {
                        // don't do anything
                    }
                    else {
                        // Send a broadcast that the Friend's songs played list has changed
                        localBroadcastManager = LocalBroadcastManager.getInstance(appContext);
                        Intent intent = new Intent(BROADCAST_FRIEND_CHANGE);
                        localBroadcastManager.sendBroadcast(intent);
                        Log.d(TAG, "This friend's songs changed: " + updatedUserId);
                    }
                }
                // In the Users hashmap, set the value of the updatedUserId key to the NEW user info
                // It's okay to set even if preferences change
                // The new value replaces the old one on the same key
                Users.getUsers().put(updatedUserId, updatedUser);
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                // This should not occur
                Log.d(TAG, "User was deleted");
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                // This should not occur
                Log.d(TAG, "User was moved");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                throw databaseError.toException();
            }
        });
    }

}
