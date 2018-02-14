package edu.ucsd.team6flashbackplayer;

import android.location.Location;
import com.google.android.gms.maps.model.LatLng;
import java.time.ZonedDateTime;

public class SongScoreCalculator {

    // dummy constructor
    public SongScoreCalculator() {}

    // calculate score
    public static int locScore(Song s, LatLng l) {
        float[] result = new float[3];
        for (LatLng loc: s.getLocHist()) {
            // calculate the distance
            Location.distanceBetween(loc.latitude, loc.longitude, l.latitude, l.longitude, result);
            // convert to feet
            if (result[0] * 3.2808399 <= 1000)
                return 1;
        }
        return 0;
    }

    public static int todScore(Song s, ZonedDateTime t) {
        if (s.getTimeHist()[timeOfDay(t.getHour())])
            return 1;
        return 0;
    }

    public static int dowScore(Song s, ZonedDateTime t) {
        if (s.getDayHist()[t.getDayOfWeek().getValue()])
            return 1;
        return 0;
    }

    // perhaps an aggregated function?
    public static int calcScore(Song s, LatLng l, ZonedDateTime t) {
        return locScore(s, l) + todScore(s, t) + dowScore(s, t);
    }

    private static int timeOfDay(int h) {
        if (5 <= h && h < 11)
            return -1;
        if (11 <= h && h < 17)
            return 0;
        return 1;
    }
}
