package Tests;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import edu.ucsd.team6flashbackplayer.Song;
import edu.ucsd.team6flashbackplayer.User;
import edu.ucsd.team6flashbackplayer.Users;

/**
 * Created by alice on 3/16/18.
 */

/**
 * Unit testing for User Story 5 - currently playing page last played display string!
 */
public class JUnitCurrPlayingId {

    Song song1;
    User user1;
    User user2;
    User user3;

    @Before
    public void initialize() {
        // Song 1 init
        //(String url, String path, String title, String artist, String album, String id)
        song1 = new Song("test.com", "/test", "Billie Jean", "MJ", "Thriller","12345");
        // User 1 is friends with user 2 but not 3

        user1 = new User();
        user1.setFullName("user one");
        user1.setId("user1");
        user1.setAlias("Pink Panther");
        HashMap<String, String> user1Map = new HashMap<>();
        // The key is the id of the user and the value is the name
        user1Map.put("user2", "user two");
        user1.setFriendsMap(user1Map);

        user2 = new User();
        user2.setFullName("user two");
        user2.setId("user2");
        user2.setAlias("Green Goat");

        user3 = new User();
        user3.setFullName("user three");
        user3.setId("user3");
        user3.setAlias("Blue Turtle");


        User.setSelf(user1);
        Users.addUser("user1", user1);
        Users.addUser("user2", user2);
        Users.addUser("user3", user3);
    }

    /**
     * If the song was last played by oneself, should show "You"
     */
    @Test
    public void testDisplayStringSelf() {
        // The song was last played by the user themselves
        song1.setLastPlayedUserUid("user1");

        Assert.assertEquals(song1.getLastPlayedUserUid(),User.getSelf().getId());
        Assert.assertEquals("You",User.displayString(song1.getLastPlayedUserUid()));
    }

    /**
     * If the song was last played by a friend, should show friend's name
     */
    @Test
    public void testDisplayStringFriend() {
        // The song was last played by a friend
        song1.setLastPlayedUserUid("user2");
        Assert.assertEquals("user two",User.displayString(song1.getLastPlayedUserUid()));
    }

    /**
     * If the song was last played by not a friend, show alias
     */
    @Test
    public void testDisplayStringUnknown() {
        // The song was last played by the user themselves
        song1.setLastPlayedUserUid("user3");
        Assert.assertEquals("Blue Turtle",User.displayString(song1.getLastPlayedUserUid()));
    }

    /**
     * If the song was last played by noone show default
     */
    @Test
    public void testDisplayStringNone() {
        // Song never played before
        Assert.assertEquals("---",User.displayString(song1.getLastPlayedUserUid()));
    }
}
