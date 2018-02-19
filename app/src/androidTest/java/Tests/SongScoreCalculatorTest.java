package Tests;

import com.google.android.gms.maps.model.LatLng;

import org.junit.Assert;
import org.junit.Test;

import java.time.ZonedDateTime;
import java.util.HashSet;

import edu.ucsd.team6flashbackplayer.Song;
import edu.ucsd.team6flashbackplayer.SongScoreCalculator;

import static org.junit.Assert.assertEquals;

public class SongScoreCalculatorTest {

    @Test
    public void calcScore_isCorrect() throws Exception {
        // dummy song objects
        Song song1 = new Song("", "", "", "");
        Song song2 = new Song("", "", "", "");
        Song song3 = new Song("", "", "", "");
        Song song4 = new Song("", "", "", "");

        // dummy current location
        LatLng curLoc = new LatLng(0, 0);
        CharSequence curTime = "2018-02-18T01:30:30+01:00[Europe/Paris]";

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

        // time of day
        boolean[] time_hist_0 = {false, false, false};
        boolean[] time_hist_1 = {false, false, true};

        // day of week
        boolean[] day_hist_0 = {false, false, false, false, false, false, false, false};
        boolean[] day_hist_1 = {false, true, true, false, true, true, false, true};

        song1.setLocHist(locs1);
        song1.setTimeHist(time_hist_0);
        song1.setTimeHist(time_hist_0);

        song2.setLocHist(locs0);
        song2.setTimeHist(time_hist_1);
        song2.setDayHist(day_hist_0);

        song3.setLocHist(locs0);
        song3.setTimeHist(time_hist_0);
        song3.setDayHist(day_hist_1);

        song4.setLocHist(locs1);
        song4.setTimeHist(time_hist_1);
        song4.setDayHist(day_hist_1);

        Assert.assertEquals(SongScoreCalculator.calcScore(song1, curLoc,
                ZonedDateTime.parse(curTime)), 1);
        assertEquals(SongScoreCalculator.calcScore(song2, curLoc,
                ZonedDateTime.parse(curTime)), 1);
        assertEquals(SongScoreCalculator.calcScore(song3, curLoc,
                ZonedDateTime.parse(curTime)), 1);
        assertEquals(SongScoreCalculator.calcScore(song4, curLoc,
                ZonedDateTime.parse(curTime)), 3);
    }
}