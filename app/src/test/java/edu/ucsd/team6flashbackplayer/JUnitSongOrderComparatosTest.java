package edu.ucsd.team6flashbackplayer;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

public class JUnitSongOrderComparatosTest {

    private Song songA = new Song("", "", "A", "A", "A", "");
    private Song songB = new Song("", "", "B", "B", "B", "");
    private Song songBDup = new Song("", "", "B", "B", "B", "");
    private Song songa = new Song("", "", "a", "A", "A", "");
    private Song songb = new Song("", "", "b", "B", "B", "");
    private Song songNull = new Song(null, null, null, null, null, null);

    @Test
    public void testTitleComparator() {
        SongOrderComparator c = new SongOrderByAlbumComparator();
        assertTrue(c.compare(songA, songB) < 0);
        assertTrue(c.compare(songa, songB) < 0);
        assertTrue(c.compare(songB, songA) > 0);
        assertEquals(0, c.compare(songA, songa));
        assertTrue( c.compare(songb, songA) > 0);
        assertEquals(0, c.compare(songB, songBDup));
        assertTrue(c.compare(songNull, songB) > 0);
        assertTrue( c.compare(songA, songNull) < 0);
        assertEquals(0, c.compare(songNull, songNull));
    }

    @Test
    public void testAlbumComparator() {
        SongOrderComparator c = new SongOrderByAlbumComparator();
        assertTrue(c.compare(songA, songB) < 0);
        assertTrue(c.compare(songa, songB) < 0);
        assertTrue(c.compare(songB, songA) > 0);
        assertEquals(0, c.compare(songA, songa));
        assertTrue( c.compare(songb, songA) > 0);
        assertEquals(0, c.compare(songB, songBDup));
        assertTrue(c.compare(songNull, songB) > 0);
        assertTrue( c.compare(songA, songNull) < 0);
        assertEquals(0, c.compare(songNull, songNull));
    }

    @Test
    public void testArtistComparator() {
        SongOrderComparator c = new SongOrderByArtistComparator();
        assertTrue(c.compare(songA, songB) < 0);
        assertTrue(c.compare(songa, songB) < 0);
        assertTrue(c.compare(songB, songA) > 0);
        assertEquals(0, c.compare(songA, songa));
        assertTrue( c.compare(songb, songA) > 0);
        assertEquals(0, c.compare(songB, songBDup));
        assertTrue(c.compare(songNull, songB) > 0);
        assertTrue( c.compare(songA, songNull) < 0);
        assertEquals(0, c.compare(songNull, songNull));
    }
}
