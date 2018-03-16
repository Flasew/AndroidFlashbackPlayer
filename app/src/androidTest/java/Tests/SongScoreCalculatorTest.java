package Tests;

import com.google.android.gms.maps.model.LatLng;

import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Array;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import edu.ucsd.team6flashbackplayer.Song;
import edu.ucsd.team6flashbackplayer.SongScoreCalculator;
import edu.ucsd.team6flashbackplayer.User;
import edu.ucsd.team6flashbackplayer.Users;

import static org.junit.Assert.assertEquals;

public class SongScoreCalculatorTest {

    @Test
    public void calcScore_isCorrect() throws Exception {
        // dummy song objects
        Song song1 = new Song("", "", "", "");
        Song song2 = new Song("", "", "", "");
        Song song3 = new Song("", "", "", "");
        Song song4 = new Song("", "", "", "");

        //creating users
        User currentUser = new User();
        currentUser.setFullName("Current User");
        currentUser.setId("currentUser");
        User.setSelf(currentUser);

        User user1 = new User();
        user1.setFullName("user one");
        user1.setId("user1");
        User user2 = new User();
        user2.setFullName("user two");
        user2.setId("user2");
        User user3 = new User();
        user3.setFullName("user three");
        user3.setId("user3");
        User user4 = new User();
        user4.setFullName("user four");
        user4.setId("user4");

        Users.addUser("currentUser", currentUser);
        Users.addUser("user1" , user1);
        Users.addUser("user2" , user2);
        Users.addUser("user3" , user3);
        Users.addUser("user4" , user4);

        // dummy current location
        LatLng curLoc = new LatLng(0, 0);

        // dummy times
        CharSequence curTime = "2018-02-18T01:30:30+01:00[Europe/Paris]";
        CharSequence time1 = "2018-02-13T01:30:30+01:00[Europe/Paris]";
        CharSequence time2 = "2018-02-01T01:30:30+01:00[Europe/Paris]";
        CharSequence time3 = "2018-02-16T01:30:30+01:00[Europe/Paris]";
        CharSequence time4 = "2018-02-12T01:30:30+01:00[Europe/Paris]";

        // locations
        LatLng loc1 = new LatLng(20, 20);
        LatLng loc2 = new LatLng(45, 45);
        LatLng loc3 = new LatLng(0, 0);

        // hashset of locations
        HashSet<LatLng> locs0 = new HashSet<>();
        locs0.add(loc1);
        locs0.add(loc2);

        HashSet<LatLng> locs1 = new HashSet<>();
        locs1.add(loc2);
        locs1.add(loc3);

        // setting time data for songs
        song1.setLocHist(locs1);
        song1.setLatestTime(ZonedDateTime.parse((time1)));
        song2.setLocHist(locs0);
        song2.setLatestTime(ZonedDateTime.parse((time2)));
        song3.setLocHist(locs0);
        song3.setLatestTime(ZonedDateTime.parse((time3)));
        song4.setLocHist(locs1);
        song4.setLatestTime(ZonedDateTime.parse((time4)));

        ArrayList<String> list1 = new ArrayList<>();
        ArrayList<String> list2 = new ArrayList<>();
        ArrayList<String> list3 = new ArrayList<>();
        ArrayList<String> list4 = new ArrayList<>();

        //Setting IDs for songs
        song1.setId("one");
        list1.add("one");
        song2.setId("two");
        list2.add("two");
        song3.setId("three");
        list3.add("three");
        song4.setId("four");
        list4.add("four");

        // setting songs list for users
        user1.setSongListPlayed(list1);
        user2.setSongListPlayed(list2);
        user3.setSongListPlayed(list3);
        user4.setSongListPlayed(list4);

        // setting friends map for current user
        HashMap<String,String> currFriends = new HashMap<>();
        currFriends.put(user1.getId(),user1.getFullName());
        currFriends.put(user2.getId(),user2.getFullName());
        currFriends.put(user3.getId(),user3.getFullName());
        currentUser.setFriendsMap(currFriends);

        // checking calcScore method
        Assert.assertEquals(SongScoreCalculator.calcScore(song1, curLoc,
                ZonedDateTime.parse(curTime)), 3);
        assertEquals(SongScoreCalculator.calcScore(song2, curLoc,
                ZonedDateTime.parse(curTime)), 1);
        assertEquals(SongScoreCalculator.calcScore(song3, curLoc,
                ZonedDateTime.parse(curTime)), 2);
        assertEquals(SongScoreCalculator.calcScore(song4, curLoc,
                ZonedDateTime.parse(curTime)), 2);

        // checking getLocScore method
        Assert.assertEquals(SongScoreCalculator.getLocScore(song1, curLoc), 1);
        Assert.assertEquals(SongScoreCalculator.getLocScore(song2, curLoc), 0);
        Assert.assertEquals(SongScoreCalculator.getLocScore(song3, curLoc), 0);
        Assert.assertEquals(SongScoreCalculator.getLocScore(song4, curLoc), 1);

        // checking getWeekScore method
        Assert.assertEquals(SongScoreCalculator.getWeekScore(song1, ZonedDateTime.parse(curTime))
                , 1);
        Assert.assertEquals(SongScoreCalculator.getWeekScore(song2, ZonedDateTime.parse(curTime))
                , 0);
        Assert.assertEquals(SongScoreCalculator.getWeekScore(song3, ZonedDateTime.parse(curTime))
                , 1);
        Assert.assertEquals(SongScoreCalculator.getWeekScore(song4, ZonedDateTime.parse(curTime))
                , 1);
    }
}