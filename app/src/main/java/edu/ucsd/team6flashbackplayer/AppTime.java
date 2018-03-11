package edu.ucsd.team6flashbackplayer;

import android.support.annotation.NonNull;

import java.time.ZonedDateTime;

/**
 * Class for time mocking.
 * This class stores a pre-set time if a fixed time is set by the user. This time will be returned
 * as a ZonedDateTime object whenever a getInstance() call happens.
 * If the fixed time is not set, then this class returns a ZonedDateTime.now() object.
 */
public class AppTime {

    private static ZonedDateTime fixedTime = null; // stores a user set date time

    /**
     * Set a fixed time and use it for future calls
     * @param zdt fixed ZonedDateTime that use provided
     */
    public static void setUseFixedTime(@NonNull ZonedDateTime zdt) {
        fixedTime = zdt;
    }

    /**
     * Remove the set fixed ZonedDateTime and allow the class to return ZonedDateTime.now()
     */
    public static void unsetFixedTime() {
        fixedTime = null;
    }

    /**
     * Returns a time when the user asks for it. Either the fixed time if set, or the
     * real time
     * @return fixed time or real time.
     */
    public static ZonedDateTime getInstance() {
        if (fixedTime != null)
            return fixedTime;   // safe to do so since it's immutable
        else
            return ZonedDateTime.now();
    }


}
