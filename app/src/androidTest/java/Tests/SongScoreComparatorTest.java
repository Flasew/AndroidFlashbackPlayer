package Tests;


import com.google.android.gms.maps.model.LatLng;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import edu.ucsd.team6flashbackplayer.User;
import edu.ucsd.team6flashbackplayer.FirebaseSongList;
import edu.ucsd.team6flashbackplayer.Song;
import edu.ucsd.team6flashbackplayer.SongList;
import edu.ucsd.team6flashbackplayer.SongScoreCalculator;
import edu.ucsd.team6flashbackplayer.SongScoreComparator;
import edu.ucsd.team6flashbackplayer.Users;

import static org.junit.Assert.assertEquals;

/**
 * Created by hrzhang on 2/18/2018
 */
public class SongScoreComparatorTest {

//    @Rule
//    public ActivityTestRule<MainActivity> mainActivity = new ActivityTestRule<MainActivity>(MainActivity.class);

    private SongScoreComparator cmp;
    CharSequence time3,time2,time1;

    @Before
    public void setup(){

        Song s1 = new Song("1","","","");
        Song s2 = new Song("2","","","");
        Song s3 = new Song("3","","","");

        List<Song> songList = new ArrayList<>();
        songList.add(s1);
        songList.add(s2);
        songList.add(s3);
        FirebaseSongList.setFirebaseSongList(songList);

        User currentUser = new User();
        currentUser.setFullName("Current User");
        currentUser.setId("currentUser");
        User user1 = new User();
        user1.setFullName("user one");
        user1.setId("user1");
        User user2 = new User();
        user2.setFullName("user two");
        user2.setId("user2");
        User user3 = new User();
        user3.setFullName("user three");
        user3.setId("user3");

        Users.addUser("Current",currentUser);
        Users.addUser("one",user1);
        Users.addUser("two",user2);
        Users.addUser("three",user3);

        time1 = "2018-02-13T01:30:30+01:00[Europe/Paris]";
        time2 = "2018-02-01T01:30:30+01:00[Europe/Paris]";
        time3 = "2018-02-16T01:30:30+01:00[Europe/Paris]";

        ArrayList<String> list1 = new ArrayList<>();
        ArrayList<String> list2 = new ArrayList<>();
        ArrayList<String> list3 = new ArrayList<>();
        s1.setId("one");
        list1.add("one");
        s2.setId("two");
        list2.add("two");
        s3.setId("three");
        list3.add("three");

        user1.setSongListPlayed(list1);
        user2.setSongListPlayed(list2);
        user3.setSongListPlayed(list3);

        HashMap<String,String> currFriends = new HashMap<>();
        currFriends.put(user1.getId(),user1.getFullName());
        currFriends.put(user2.getId(),user2.getFullName());
        currentUser.setFriendsMap(currFriends);

        cmp = new SongScoreComparator( new LatLng( 0, 0 ), ZonedDateTime.parse("2007-12-03T10:15:30+01:00[Europe/Paris]"));
    }

    @After
    public void cleanup(){
        List<Song> songs = FirebaseSongList.getSongs();
        songs.set(0,new Song("1","","",""));
        songs.set(1,new Song("2","","",""));
        songs.set(2,new Song("3","","",""));
    }

    /**
     * Tests when s1 have score higher than the s2
     * Testing compare(s1,s2)
     * Should return -1
     * @throws Exception
     */
    @Test
    public void scoreTest1() throws Exception {
        List<Song> songs = FirebaseSongList.getSongs();
        songs.get(0).getLocHist().add( new LatLng( 0, 0 ) );
        songs.get(1).getLocHist().add( new LatLng( 45, 45 ) );
        songs.get(0).setLatestTime(ZonedDateTime.parse(time1));
        songs.get(1).setLatestTime(ZonedDateTime.parse(time2));

        assertEquals( -1, cmp.compare(0,1) );
    }

    /**
     * Test when s2 have same score as s1, but played nearby
     * Testing compare(s1,s2)
     * Should return 1
     * @throws Exception
     */
    @Test
    public void scoreTest2() throws Exception {
        List<Song> songs = FirebaseSongList.getSongs();
        songs.get(0).getLocHist().add( new LatLng( 45, 45 ) );
        songs.get(1).getLocHist().add( new LatLng( 0, 0 ) );
        songs.get(0).setLatestTime(ZonedDateTime.parse(time1));
        songs.get(1).setLatestTime(ZonedDateTime.parse(time2));

        assertEquals( 1,cmp.compare(0,1) );
    }

    /**
     * Test when s2 has a higher score than s1
     * Testing compare(s1,s2)
     * Should return 1
     * @throws Exception
     */
    @Test
    public void scoreTest3() throws Exception {
        List<Song> songs = FirebaseSongList.getSongs();
        songs.get(0).getLocHist().add( new LatLng( 0, 0 ) );
        songs.get(1).getLocHist().add( new LatLng( 0, 0 ) );
        songs.get(0).setLatestTime(ZonedDateTime.parse(time2));
        songs.get(1).setLatestTime(ZonedDateTime.parse(time1));
        assertEquals(  1,cmp.compare(0,1) );
    }

    /**
     * Test when s1 have same score as s2, but played nearby
     * Testing compare(s1,s2)
     * Should return -1
     * @throws Exception
     */
    @Test
    public void scoreTest4() throws Exception {
        List<Song> songs = FirebaseSongList.getSongs();
        songs.get(1).getLocHist().add( new LatLng( 45, 45 ) );
        songs.get(0).getLocHist().add( new LatLng( 0, 0 ) );
        songs.get(1).setLatestTime(ZonedDateTime.parse(time1));
        songs.get(0).setLatestTime(ZonedDateTime.parse(time2));

        assertEquals( -1,cmp.compare(0,1) );
    }

    /**
     * Test when s1 and s3 have same score, but different timePlayed
     * Testing compare(s1,s3)
     * Should return -1
     * @throws Exception
     */
    @Test
    public void scoreTest5() throws Exception {
        List<Song> songs = FirebaseSongList.getSongs();
        songs.get(0).getLocHist().add( new LatLng( 0, 0 ) );
        songs.get(2).getLocHist().add( new LatLng( 0, 0 ) );
        songs.get(0).setLatestTime(ZonedDateTime.parse(time1));
        songs.get(2).setLatestTime(ZonedDateTime.parse(time2));

        assertEquals( -1,cmp.compare(0,2) );
    }

    /**
     * Test when s1 and s3 have same score, but different time played
     * Testing compare(s1,s3)
     * Should return 1
     * @throws Exception
     */
    @Test
    public void scoreTest6() throws Exception {
        List<Song> songs = FirebaseSongList.getSongs();
        songs.get(2).getLocHist().add( new LatLng( 0, 0 ) );
        songs.get(0).getLocHist().add( new LatLng( 0, 0 ) );
        songs.get(2).setLatestTime(ZonedDateTime.parse(time1));
        songs.get(0).setLatestTime(ZonedDateTime.parse(time2));

        assertEquals( 1,cmp.compare(0,2) );
    }

    /**
     * Test when s1 and s2 have same score - s2 is played before s1
     * Testing compare(s1,s2)
     * Should return 1
     * @throws Exception
     */
    @Test
    public void scoreTest7() throws Exception {
        List<Song> songs = FirebaseSongList.getSongs();
        songs.get(0).getLocHist().add( new LatLng( 0, 0 ) );
        songs.get(1).getLocHist().add( new LatLng( 0, 0 ) );
        songs.get(0).setLatestTime(ZonedDateTime.parse(time1));
        songs.get(1).setLatestTime(ZonedDateTime.parse(time3));

        assertEquals( 1,cmp.compare(0,1) );
    }

    /**
     * Test when s1 and s2 have same score - s1 was played before s2
     * Testing compare(s1,s2)
     * Should return -1
     * @throws Exception
     */
    @Test
    public void scoreTest8() throws Exception {
        List<Song> songs = FirebaseSongList.getSongs();
        songs.get(0).getLocHist().add( new LatLng( 0, 0 ) );
        songs.get(1).getLocHist().add( new LatLng( 0, 0 ) );
        songs.get(0).setLatestTime(ZonedDateTime.parse(time3));
        songs.get(1).setLatestTime(ZonedDateTime.parse(time1));

        assertEquals( -1,cmp.compare(0,1) );
    }

}