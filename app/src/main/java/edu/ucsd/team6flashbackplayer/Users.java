package edu.ucsd.team6flashbackplayer;

import android.util.Log;

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
    private static HashMap<String, User> users = new HashMap<>();

    private static FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    private static DatabaseReference databaseReference = firebaseDatabase.getReference("users");


    /**
     * Loads the entire list of Users from Firebase database into users
     * hashed on String(id) and User object
     * This can be used to determine Friends info. But not used to load User on app start (because of async issues)
     */
    public static void loadUsers() {

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d("Users", "Loading in");
                GenericTypeIndicator<HashMap<String, User>> userGTypeInd = new GenericTypeIndicator<HashMap<String, User>>() {};
                Map<String, User> userHashMap = dataSnapshot.getValue(userGTypeInd);

                // When no Users have used the app yet
                if (userHashMap == null) {
                    Log.d("Users","don't exist");
                    users = new HashMap<>();
                }
                else {
                    // This is for the sake of casting the result (Value) to User object
                    HashMap<String, User> userMap = new HashMap<>();
                    for (String key : userHashMap.keySet()) {
                        User usr = (User) userHashMap.get(key);
                        userMap.put(key,usr);
                        Log.d("Key, Name", key + " " + usr.getFullName() + usr.getAlias());
                    }

                    // Set the field to this hashmap we obtained from Firebase
                    users = userMap;
                }
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
     * Adds the user who was never logged in before to the users hashmap
     * Only used when loading a user in from app start/login
     * @param id id of user
     * @param user User object associated with id
     */
    public static void addUser(String id, User user) {
        users.put(id, user);
    }
}
