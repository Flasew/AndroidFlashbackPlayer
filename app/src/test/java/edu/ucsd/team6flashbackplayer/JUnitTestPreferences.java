package edu.ucsd.team6flashbackplayer;


import org.junit.Before;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;

/**
 * Created by Brian Frost on 2/18/2018.
 */

public class JUnitTestPreferences {
    Song s;
    SongPreference sp;

//    @Rule
//    public ActivityTestRule<MainActivity> mainActivity = new ActivityTestRule<MainActivity>(MainActivity.class);

    @Before
    public void setup() {
        s = new Song("10", "title", "artist", "album");
        sp = new SongPreference();
    }

    @Test
    public void testLike() {
        sp.like(s);
        assertEquals(s.isLiked(), true);
        assertEquals(s.isDisliked(), false);
    }

    @Test
    public void testDislike() {
        sp.dislike(s);
        assertEquals(s.isDisliked(), true);
        assertEquals(s.isLiked(), false);
    }
}
