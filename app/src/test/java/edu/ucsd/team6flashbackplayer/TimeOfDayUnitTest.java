package edu.ucsd.team6flashbackplayer;

/**
 * Created by frankwang on 2/18/18.
 */

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit test for Time of day method in the song class.
 */
public class TimeOfDayUnitTest {

    @Test
    public void testTOD5PMto5AM() {
        assertEquals(2, Song.timeOfDay(23));
    }

    @Test
    public void testTOD5AMto11AM() {
        assertEquals(0, Song.timeOfDay(6));
    }

    @Test
    public void testTOD11AMto5PM() {
        assertEquals(1, Song.timeOfDay(12));
    }

    @Test
    public void testTOD5AM() {
        assertEquals(0, Song.timeOfDay(5));
    }

    @Test
    public void testTOD11AM() {
        assertEquals(1, Song.timeOfDay(11));
    }

    @Test
    public void testTOD5PM() {
        assertEquals(2, Song.timeOfDay(17));
    }

}
