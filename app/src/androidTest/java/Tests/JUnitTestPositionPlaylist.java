package Tests;

import android.support.test.rule.ActivityTestRule;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import edu.ucsd.team6flashbackplayer.Album;
import edu.ucsd.team6flashbackplayer.MainActivity;
import edu.ucsd.team6flashbackplayer.PositionPlayListFactory;
import edu.ucsd.team6flashbackplayer.Song;
import edu.ucsd.team6flashbackplayer.SongList;

import static junit.framework.Assert.assertEquals;

/**
 * Created by Brian Frost on 2/18/2018.
 */

public class JUnitTestPositionPlaylist {
    Song a;
    Song b;
    Song c;
    Album album;
    ArrayList<Integer> positionList;
    ArrayList<Integer> mockList;
    List<Song> list;

//    @Rule
//    public ActivityTestRule<MainActivity> mainActivity = new ActivityTestRule<MainActivity>(MainActivity.class);

    @Before
    public void setup() {
        a = new Song("1", "Title1", "Artist1", "Album1");
        b = new Song("2", "Title2", "Artist2", "Album2");

        list = new ArrayList<>();
        list.add(a);
        list.add(b);

        SongList.initSongList(list);

        album = new Album("Name", SongList.getSongs());

        positionList = PositionPlayListFactory.makeList(album);
        mockList = new ArrayList<>();
        mockList.add(0);
        mockList.add(1);
    }

    @Test
    public void testPositions() {

        ArrayList<Integer> positions = positionList;

        for (int i = 0; i < mockList.size(); i++) {
            assertEquals(mockList.get(i), positions.get(i));
        }

    }

}
