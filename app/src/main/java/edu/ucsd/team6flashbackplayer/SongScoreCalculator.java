package edu.ucsd.team6flashbackplayer;

import android.location.Location;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import java.time.ZonedDateTime;
import java.util.logging.Logger;

/**
 * class SongScoreCalculator
 *
 * Calculate a song's score for the flashback mode based on it's latest location and time.
 */
public class SongScoreCalculator {

    static final double METER_TO_FEET = 3.2808399;  // conversion
    private static final String TAG = "SongScoreCalculator";
    private static int range = 1000;    // range of location considered to be scored.

    /**
     * Unused default ctor.
     */
    public SongScoreCalculator() {}

    /**
     * Calculate a song's location score
     * @param s song to be calculated
     * @param l location to be asserted
     * @return location score.
     */
    private static int locScore(Song s, LatLng l) {
        int score = 0;

        if (l != null) {
            float[] result = new float[3];
            for (LatLng loc : s.getLocHist()) {
                // calculate the distance
                Location.distanceBetween(loc.latitude, loc.longitude, l.latitude, l.longitude, result);
                // convert to feet
                Log.d(TAG, "Location between (" + loc.latitude + ", " +
                    loc.longitude + ") and (" + l.latitude + ", " + l.longitude +
                    " is " +result[0] * METER_TO_FEET + " feet.");
                if (result[0] * METER_TO_FEET <= range) {
                    score = 1;
                    break;
                }
            }
        }
        Log.d(TAG, "Song " + s.getTitle() + " gets an location score " + score);
        return score;
    }

    /**
     * Calculate a song's time of day score
     * @param s song to be calculated
     * @param t time to be asserted
     * @return Time of day score.
     */
    private static int weekScore(Song s, ZonedDateTime t) {
        int score = 0;
        ZonedDateTime sTime = s.getLatestTime();
        t = t.minusWeeks(1);
        if (sTime.getDayOfYear() > t.getDayOfYear()) {
            score = 1;
        }
        else if(sTime.getDayOfYear() == t.getDayOfYear()) {
            if(sTime.getHour() > t.getHour()){
                score = 1;
            }
            else if(sTime.getHour() == t.getHour()){
                if(sTime.getMinute() > t.getMinute()){
                    score = 1;
                }
                else if(sTime.getMinute() == t.getMinute()){
                    if(sTime.getSecond() > t.getSecond()){
                        score = 1;
                    }
                    else if(sTime.getSecond() == t.getSecond()){
                        if(sTime.getNano() > t.getNano()){
                            score = 1;
                        }
                    }
                }
            }
        }
        Log.d(TAG, "Song " + s.getTitle() + " gets a time of day score " + score);
        return score;
    }

    /**
     * Calculate a song's day of week
     * @param s song to be calculated
     * @param t time to be asserted
     * @return day of week score.
     */
    @Deprecated
    private static int dowScore(Song s, ZonedDateTime t) {
        return 1;
    }

    /**
     * Calculate a song's total score
     * @param s song to be calculated
     * @param l location to be asserted
     * @param t time to be asserted
     * @return total score.
     */
    public static int calcScore(Song s, LatLng l, ZonedDateTime t) {
        return locScore(s, l) + weekScore(s, t) + dowScore(s, t);
    }

}
