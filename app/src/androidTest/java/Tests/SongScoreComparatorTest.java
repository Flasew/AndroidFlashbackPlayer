package Tests;


import com.google.android.gms.maps.model.LatLng;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import edu.ucsd.team6flashbackplayer.Song;
import edu.ucsd.team6flashbackplayer.SongList;
import edu.ucsd.team6flashbackplayer.SongScoreCalculator;
import edu.ucsd.team6flashbackplayer.SongScoreComparator;

import static org.junit.Assert.assertEquals;

/**
 * Created by hrzhang on 2/18/2018
 */
public class SongScoreComparatorTest {

//    @Rule
//    public ActivityTestRule<MainActivity> mainActivity = new ActivityTestRule<MainActivity>(MainActivity.class);

    private SongScoreComparator cmp;
    private List<Song> songList = new ArrayList<Song>();

    @Before
    public void setup(){

        Song s1 = new Song("1","","","");
        Song s2 = new Song("2","","","");
        songList.add(s1);
        songList.add(s2);
        SongList.initSongList(songList);
        cmp = new SongScoreComparator( new LatLng( 0, 0 ), ZonedDateTime.parse("2007-12-03T10:15:30+01:00[Europe/Paris]"));
    }

    @After
    public void cleanup(){
        List<Song> songs = SongList.getSongs();
        songs.set(0,new Song("1","","",""));
        songs.set(1,new Song("1","","",""));
    }

    /**
     * Tests when s1 have score higher than the s2
     * Testing compare(s1,s2)
     * Should return -1
     * @throws Exception
     */
    @Test
    public void scoreTest1() throws Exception {
        List<Song> songs = SongList.getSongs();
        songs.get(0).getLocHist().add( new LatLng( 0, 0 ) );
        songs.get(1).getLocHist().add( new LatLng( 45, 45 ) );

        assertEquals( -1, cmp.compare(0,1) );
    }

    /**
     * Test when s2 have score higher than the s1
     * Testing compare(s1,s2)
     * Should return 1
     * @throws Exception
     */
    @Test
    public void scoreTest2() throws Exception {
        List<Song> songs = SongList.getSongs();
        songs.get(0).getLocHist().add( new LatLng( 45, 45 ) );
        songs.get(1).getLocHist().add( new LatLng( 0, 0 ) );
        assertEquals( 1,cmp.compare(0,1) );
    }

    /**
     * Test when s1 and s2 have same score, but s1 is liked
     * Testing compare(s1,s2)
     * Should return -1
     * @throws Exception
     */
    @Test
    public void scoreTest3() throws Exception {
        List<Song> songs = SongList.getSongs();
        songs.get(0).getLocHist().add( new LatLng( 0, 0 ) );
        songs.get(0).setLike(true);
        songs.get(1).getLocHist().add( new LatLng( 0, 0 ) );
        assertEquals(  -1,cmp.compare(0,1) );
    }

    /**
     * Test when s1 and s2 have same score, but s2 is liked
     * Testing compare(s1,s2)
     * Should return 1
     * @throws Exception
     */
    @Test
    public void scoreTest4() throws Exception {
        List<Song> songs = SongList.getSongs();
        songs.get(0).getLocHist().add( new LatLng( 0, 0 ) );

        songs.get(1).getLocHist().add( new LatLng( 0, 0 ) );
        songs.get(1).setLike(true);
        assertEquals( 1,cmp.compare(0,1) );
    }

    /**
     * Test when s1 and s2 have same score, same likedness, but s1 is more recently played
     * Testing compare(s1,s2)
     * Should return -1
     * @throws Exception
     */
    @Test
    public void scoreTest5() throws Exception {
        List<Song> songs = SongList.getSongs();
        songs.get(0).getLocHist().add( new LatLng( 0, 0 ) );
        songs.get(0).setLike(true);
        songs.get(0).setLatestTime(ZonedDateTime.parse("2007-12-03T10:15:31+01:00[Europe/Paris]") );

        songs.get(1).getLocHist().add( new LatLng( 0, 0 ) );
        songs.get(1).setLike(true);
        songs.get(1).setLatestTime(ZonedDateTime.parse("2007-12-03T10:15:30+01:00[Europe/Paris]") );


        assertEquals( -1,cmp.compare(0,1) );
    }

    /**
     * Test when s1 and s2 have same score, same likedness, but s2 is more recently played
     * Testing compare(s1,s2)
     * Should return 1
     * @throws Exception
     */
    @Test
    public void scoreTest6() throws Exception {
        List<Song> songs = SongList.getSongs();
        songs.get(0).getLocHist().add( new LatLng( 0, 0 ) );
        songs.get(0).setLike(true);
        songs.get(0).setLatestTime(ZonedDateTime.parse("2007-12-03T10:15:30+01:00[Europe/Paris]") );

        songs.get(1).getLocHist().add( new LatLng( 0, 0 ) );
        songs.get(1).setLike(true);
        songs.get(1).setLatestTime(ZonedDateTime.parse("2007-12-03T10:15:31+01:00[Europe/Paris]") );


        assertEquals( 1,cmp.compare(0,1) );
    }

    /**
     * Test when s1 and s2 have same score, same likedness, but s2 has null time element
     * Testing compare(s1,s2)
     * Should return -1
     * @throws Exception
     */
    @Test
    public void scoreTest7() throws Exception {
        List<Song> songs = SongList.getSongs();
        songs.get(0).getLocHist().add( new LatLng( 0, 0 ) );
        songs.get(0).setLike(true);
        songs.get(0).setLatestTime(ZonedDateTime.parse("2007-12-03T10:15:30+01:00[Europe/Paris]") );

        songs.get(1).getLocHist().add( new LatLng( 0, 0 ) );
        songs.get(1).setLike(true);


        assertEquals( -1,cmp.compare(0,1) );
    }

    /**
     * Test when s1 and s2 have same score, same likedness, but s1 has null time element
     * Testing compare(s1,s2)
     * Should return 1
     * @throws Exception
     */
    @Test
    public void scoreTest8() throws Exception {
        List<Song> songs = SongList.getSongs();
        songs.get(0).getLocHist().add( new LatLng( 0, 0 ) );
        songs.get(0).setLike(true);


        songs.get(1).getLocHist().add( new LatLng( 0, 0 ) );
        songs.get(1).setLike(true);
        songs.get(1).setLatestTime(ZonedDateTime.parse("2007-12-03T10:15:30+01:00[Europe/Paris]") );

        assertEquals( 1,cmp.compare(0,1) );
    }

    /**
     * Test when s1 and s2 have same score, same likedness, AND same time
     * Testing compare(s1,s2)
     * Should return 0
     * @throws Exception
     */
    @Test
    public void scoreTest9() throws Exception {
        List<Song> songs = SongList.getSongs();
        songs.get(0).getLocHist().add( new LatLng( 0, 0 ) );
        songs.get(0).setLike(true);
        songs.get(0).setLatestTime(ZonedDateTime.parse("2007-12-03T10:15:30+01:00[Europe/Paris]") );

        songs.get(1).getLocHist().add( new LatLng( 0, 0 ) );
        songs.get(1).setLike(true);
        songs.get(1).setLatestTime(ZonedDateTime.parse("2007-12-03T10:15:30+01:00[Europe/Paris]") );

        assertEquals( 0,cmp.compare(0,1) );
    }

    /**
     * Test when s1 and s2 have same score, same likedness, AND both have null time element
     * Testing compare(s1,s2)
     * Should return 0
     * @throws Exception
     */
    @Test
    public void scoreTest10() throws Exception {
        List<Song> songs = SongList.getSongs();
        songs.get(0).getLocHist().add( new LatLng( 0, 0 ) );
        songs.get(0).setLike(true);

        songs.get(1).getLocHist().add( new LatLng( 0, 0 ) );
        songs.get(1).setLike(true);

        assertEquals( 0,cmp.compare(0,1) );
    }
}