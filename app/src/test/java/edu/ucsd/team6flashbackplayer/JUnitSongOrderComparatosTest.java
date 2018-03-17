package edu.ucsd.team6flashbackplayer;

import org.junit.Test;
import static org.junit.Assert.*;

import java.time.ZonedDateTime;

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

    @Test
    public void testFavoriteComparator() {
        SongOrderComparator c = new SongOrderByFavoriteStatusComparator();
        SongPreference.like(songA);
        SongPreference.like(songa);
        SongPreference.dislike(songB);
        SongPreference.dislike(songb);

        assertEquals(0, c.compare(songA, songa));
        assertEquals(0, c.compare(songb, songB));
        assertEquals(0, c.compare(songNull, songBDup));

        assertTrue(c.compare(songA, songB) < 0);
        assertTrue(c.compare(songA, songNull) < 0);
        assertTrue(c.compare(songNull, songb) < 0);
        assertTrue(c.compare(songNull, songa) > 0);
        assertTrue(c.compare(songB, songa) > 0);
        assertTrue(c.compare(songB, songNull) > 0);

    }

    @Test
    public void testLatestComparator() {
        SongOrderComparator c = new SongOrderByLastPlayedComparator();

        CharSequence time1 = "2018-02-01T01:30:30+01:00[Europe/Paris]";
        CharSequence time2 = "2018-02-12T01:30:30+01:00[Europe/Paris]";
        CharSequence time3 = "2018-02-13T01:30:30+01:00[Europe/Paris]";

        songA.setLatestTime(ZonedDateTime.parse((time1)));
        songa.setLatestTime(ZonedDateTime.parse((time2)));
        songB.setLatestTime(ZonedDateTime.parse((time3)));
        songb.setLatestTime(ZonedDateTime.parse((time3)));

        assertEquals(0, c.compare(songb, songB));

        assertTrue(c.compare(songa, songB) > 0);
        assertTrue(c.compare(songb, songa) < 0);

        assertTrue(c.compare(songNull, songa) > 0);
        assertTrue(c.compare(songa, songNull) < 0);

        assertEquals(0, c.compare(songNull, songNull));


    }
}
