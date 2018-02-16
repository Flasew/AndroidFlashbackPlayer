package edu.ucsd.team6flashbackplayer;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;

/**
 * Created by Brian Frost on 2/15/2018.
 */

public class MyLocListener implements LocationListener {
    private Location curLoc;

    public void onLocationChanged(Location loc) {
        curLoc = loc;
    }

    public void onStatusChanged(String provider, int status, Bundle extras) {}

    public void onProviderEnabled(String provider) {}

    public void onProviderDisabled(String provider) {}

    public Location getCurrentLocation() {
        return curLoc;
    }
}
