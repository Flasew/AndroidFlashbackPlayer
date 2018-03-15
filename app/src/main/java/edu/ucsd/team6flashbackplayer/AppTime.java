package edu.ucsd.team6flashbackplayer;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.time.ZonedDateTime;


/**
 * Class for time mocking.
 * This class stores a pre-set time if a fixed time is set by the user. This time will be returned
 * as a ZonedDateTime object whenever a getInstance() call happens.
 * If the fixed time is not set, then this class returns a ZonedDateTime.now() object.
 */
public class AppTime {

    private static final String TAG = AppTime.class.getName();

    static final String BROADCAST_FAKE_TIME_UPDATE = "fakeTimeUpdate";
    static final int[] UPDATE_TIME = {0, 5, 11, 17};

    private static ZonedDateTime fixedTime = null; // stores a user set date time
    private static LocalBroadcastManager localBroadcastManager;

    public static void setupBroadcastManager(Context c) {
        if (localBroadcastManager == null) {
            localBroadcastManager = LocalBroadcastManager.getInstance(c.getApplicationContext());
            Log.d(TAG, "local BM " + localBroadcastManager);
        }
    }

    /**
     * Set a fixed time and use it for future calls
     * @param zdt fixed ZonedDateTime that use provided
     */
    public static void setUseFixedTime(@NonNull ZonedDateTime zdt) {
        if (fixedTime != null) {
            // check if there should be a "update for time"
            if (zdt.getYear() > fixedTime.getYear() || zdt.getDayOfYear() > fixedTime.getDayOfYear())
                broadcastFakeTimeChange();

            else {
                for (int uTime: UPDATE_TIME) {
                    if (zdt.getHour() >= uTime && fixedTime.getHour() < uTime) {
                       broadcastFakeTimeChange();
                       break;
                    }
                }
            }
        }
        fixedTime = zdt;
    }

    /**
     * broadcast a fake app time update to trigger vibe mode list update when mocking time.
     */
    private static void broadcastFakeTimeChange() {
        Log.d(TAG, "Attempting to send broadcast of fake time change...");
        Intent intent = new Intent(BROADCAST_FAKE_TIME_UPDATE);
        if (localBroadcastManager != null) {
            localBroadcastManager.sendBroadcast(intent);
            Log.d(TAG, "Fake time update send");
        }
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

    /**
     * if the app is using fixed time
     * @return
     */
    public static boolean usingFixedTime() {
        return fixedTime != null;
    }


}
