package edu.ucsd.team6flashbackplayer;

import android.content.res.AssetManager;
import android.util.Log;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

/**
 * Created by frankwang on 3/6/18.
 * Edited by alice 3/7/18
 */

public class User {

    private static User self;

    private String fullName;
    private String alias;
    private String id;
    private ArrayList<String> songListPlayed;
    private HashMap<String, List<Boolean>> songListPref;

    /**
     * Default constructor for necessary for Firebase
     */
    public User() { }

    /**
     * Constructor with fields obtained from the User logged in
     *
     * @param name name of the User (given name)
     * @param id id associated with the User
     * @param assetManager AssetManager to read from files in assets
     */
    public User(String name, String id, AssetManager assetManager) {
        fullName = name;
        this.id = id;
        alias = generateAlias(assetManager);

        songListPlayed = new ArrayList<String>();

        songListPref = new HashMap<>();
        // This is purely used for testing purposes as of now.. will remove later
        String abc = "---";
        ArrayList<Boolean> newList = new ArrayList<Boolean>();
        newList.add(false);
        newList.add(false);
        songListPref.put(abc, newList);
    }

    // Public getters and setters for all the fields of the class - necessary to store User in Firebase
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getAlias() { return alias; }
    public void setAlias(String alias) { this.alias = alias; }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public ArrayList<String> getSongListPlayed() {
        return songListPlayed;
    }
    public void setSongListPlayed(ArrayList<String> songList) {
        this.songListPlayed = songList;
    }

    public HashMap<String, List<Boolean>> getSongListPref() { return songListPref; }
    public void setSongListPref(HashMap<String, List<Boolean>> songListPref) { this.songListPref = songListPref; }

    public static User getSelf() {
        return self;
    }

    public static void setSelf(User usr) {
        self = usr;
    }


    /**
     * Generate random alias for a user from colors and animals
     * @param assetManager AssetManager to read from assets folder of app
     * @return String that is the alias for a user
     */
    private String generateAlias(AssetManager assetManager) {
        ArrayList<String> colorsList = readWords(assetManager, "colors.txt");
        ArrayList<String> animalsList = readWords(assetManager, "animals.txt");

        // Generate a random number in the range of the list sizes and create alias based on those numbers
        Random rand = new Random();
        Integer randomColorIndex = rand.nextInt((colorsList.size()));
        Integer randomAnimalIndex = rand.nextInt((animalsList.size()));

        String alias = colorsList.get(randomColorIndex) + " " + animalsList.get(randomAnimalIndex);
        Log.d("Alias is", alias);

        /* Later if necessary... check/handle uniqueness
        DatabaseReference dr = FirebaseDatabase.getInstance().getReference("aliases").child(alias);
        dr.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String exists = dataSnapshot.getValue(String.class);

                if (exists == null) {

                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                //
            }
        });
        dr = dr.push();
        dr.setValue(alias); */

        return alias;
    }

    /**
     * Reads words from a file and returns them in a list (to be used to generate alias)
     * @param aM AssetManager used to read files from assets folder
     * @param fileName file to read words from
     * @return list of words
     */
    private ArrayList<String> readWords(AssetManager aM, String fileName) {
        // List of words to return
        ArrayList<String> list = new ArrayList<>();

        try {
            String line;
            // Try to read from the file specified
            InputStream stream  = aM.open(fileName);
            InputStreamReader isr = new InputStreamReader(stream);
            BufferedReader br = new BufferedReader(isr);
            Log.d("User", "Trying to read words from file");

            while((line= br.readLine()) !=null) {
                list.add(line);
            }
            stream.close();
        }
        catch (Exception e) {
            Log.d("User", "File to read from doesn't exist");
        }
        return list;
    }

    /**
     * Populates the User self field of this class from Firebase
     * or creates a new User and adds the User to Firebase and the Users list if first time signin
     *
     * @param acc The google account which is the User currently signed in
     * @param assetManager AssetManager to read in files from assets folders to generate alias if necessary
     */
    public static void loadUser(GoogleSignInAccount acc, AssetManager assetManager) {
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference databaseReference = firebaseDatabase.getReference("users").child(acc.getId());

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Try to get the User at the location marked by User's id
                User aUser = dataSnapshot.getValue(User.class);

                // If the User doesn't exist then create a new one and add it to lists
                if (aUser == null) {
                    String id = acc.getId();
                    // Create the User and add to Firebase and Users list
                    User newUser = new User(acc.getDisplayName(), id, assetManager);
                    User.setSelf(newUser);

                    // Add this user to Firebase
                    databaseReference.setValue(newUser);
                    // Also add this user to the global user list
                    Users.addUser(id, newUser);

                    Log.d("User", "The user is " + newUser.getId() + " " + newUser.getAlias());
                }
                // Otherwise set to the user we received from Firebase
                else {
                    self = aUser;
                    Log.d("User", "The user is " + aUser.getId() + " " + aUser.getAlias());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                //
            }
        });
    }
}
