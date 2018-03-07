package edu.ucsd.team6flashbackplayer;

/**
 * Created by frankwang on 3/6/18.
 */

public class User {

    private static User self;

    public User(){ // place holder
    }

    /**
     * Effectively make self user final. Set the user of this app.
     * @param uid user id.
     */
    public static void setSelfUser(String uid) {
        if (self == null) {
            self = Users.getUser(uid);
        }
    }

    public static User getSelf() {
        return self;
    }

    public static User makeUser() {
        // TODO
        return new User();
    }

}
