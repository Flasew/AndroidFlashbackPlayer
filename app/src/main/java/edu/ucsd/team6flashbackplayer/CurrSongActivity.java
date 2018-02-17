package edu.ucsd.team6flashbackplayer;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class CurrSongActivity extends MusicPlayerActivity implements LocationListener {

    private static final String TAG = "CurrSongActivity";
    // time for update the "time of day" period. 0 handles day change.
    private final int[] UPDATE_TIME = {0, 5, 11, 17};
    private boolean flashBackMode;          // if FB is enabled

    private ArrayList<Integer> positionList;

    // UI elements
    private Button flashBackButton;
    private TextView timeClockView;
    private TextView timeDateView;
    private TextView locationTextView;
    private TextView songTitleView;
    private TextView songArtistView;
    private PreferenceButtons preferenceButtons;
    private Button playListButton;

    // location manager to listen for location update & receiver
    private LocationManager locationManager;

    // Alarm manager to listen for time/day update & pending intents for cancel
    private AlarmManager alarmManager;
    private PendingIntent[] alarmPendingIntents;

    // location update frequency
    private final int LOC_UPDATE_MIN_TIME = 1000;       // milliseconds
    private final int LOC_UPDATE_MIN_DIST = 50;         // meters

    // location cache
    private LatLng lastLatLngCache;

    public static Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        CurrSongActivity.context = getApplicationContext();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_curr_song);

        // set title of this activity
        setTitle(R.string.curr_song_activity_title);

        // BCM
        localBroadcastManager = LocalBroadcastManager.getInstance(this);

        // Location manager
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        // AlarmManager
        alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        // UI elements
        timeClockView = findViewById(R.id.time_clock_txt);
        timeDateView = findViewById(R.id.time_date_txt);
        locationTextView = findViewById(R.id.location_txt);
        songTitleView = findViewById(R.id.song_name);
        songArtistView = findViewById(R.id.song_artist);
        preferenceButtons = new PreferenceButtons(
                (ImageButton) findViewById(R.id.like_button),
                (ImageButton) findViewById(R.id.dislike_button),
                context
        );
        preferenceButtons.redrawButtons();
        PreferenceButtons.setLocalBroadcastManager(this);
        flashBackButton = findViewById(R.id.fb_button);
        playListButton = findViewById(R.id.show_playlist);

        // register list for
        playListButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startFBListActivity();
            }
        });


        final SharedPreferences.Editor editor = fbModeSharedPreferences.edit();
        flashBackMode = fbModeSharedPreferences.getBoolean("mode", false);

        flashBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "fbmode before pressing: " + flashBackMode);
                flashBackMode = !flashBackMode;
                editor.putBoolean("mode", flashBackMode);
                editor.apply();
                if (flashBackMode)
                    enableFBMode();
                else
                    disableFBMode();
            }
        });

        // register location change listener but disable it first
        // setup for location change listener
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            // Request location updates:
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                    LOC_UPDATE_MIN_TIME, LOC_UPDATE_MIN_DIST,
                    this);
        }


        Log.d(TAG, "fbmode on enter: " + flashBackMode);
        if (flashBackMode) {
            enableFBMode();
        } else {
            flashBackButton.setBackground(getDrawable(R.drawable.fb_disabled));
        }

    }

    @Override
    public void onBackPressed() {
        if (!flashBackMode) {
            super.onBackPressed();
        }
    }

    // update the page's content on song playing update
    @Override
    protected void onSongUpdate(int position) {

        final Song currSong = SongList.getSongs().get(position);

        songTitleView.setText(currSong.getTitle());
        songTitleView.setSelected(true);
        songArtistView.setText(currSong.getArtist());
        songArtistView.setSelected(true);
        locationTextView.setText(getStrAddress(currSong.getLatestLoc()));
        locationTextView.setSelected(true);
        timeDateView.setText(getStrDate(currSong.getLatestTime()));
        timeDateView.setSelected(true);
        timeClockView.setText(getStrClock(currSong.getLatestTime()));
        timeClockView.setSelected(true);
        preferenceButtons.setSong(currSong);
        preferenceButtons.setButtonListeners();
        preferenceButtons.redrawButtons();
    }

    @Override
    protected void onSongFinish() {

        songTitleView.setText(NO_INFO);
        songArtistView.setText(NO_INFO);
        locationTextView.setText(NO_INFO);
        timeDateView.setText(NO_INFO);
        timeClockView.setText(NO_INFO);
        preferenceButtons.setSong(null);
        preferenceButtons.removeButtonListeners();
        preferenceButtons.redrawButtons();

    }

    /**
     * Unregister the receivers
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        locationManager.removeUpdates(this);

        if (alarmPendingIntents != null) {
            for (int i = 0; i < alarmPendingIntents.length; i++) {
                if (alarmPendingIntents[i] != null) {
                    alarmManager.cancel(alarmPendingIntents[i]);
                    alarmPendingIntents[i] = null;
                }
            }
        }
    }


    /**
     * give a new playlist to music service
     *
     * @param location new location
     */
    @Override
    public void onLocationChanged(Location location) {


        LatLng latlng = lastLatLngCache;
        try {
            lastLatLngCache = new LatLng(location.getLatitude(), location.getLongitude());
        }
        // if somehow new location is null, keep the old one.
        catch (NullPointerException e) {
            lastLatLngCache = latlng;
        }

        if (flashBackMode)
            startMusicPlayerServiceFBMode(true);

        Log.d(TAG, "Location updated, Lat: " + location.getLatitude() + "Lng: " + location.getLongitude());


    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    private void startFBListActivity() {
        Intent intent = new Intent(CurrSongActivity.this, FBPlayListActivity.class);
        intent.putIntegerArrayListExtra(FBPlayListActivity.FB_POS_LIST, positionList);
        startActivity(intent);
    }

    // enters the flashback mode
    private void enableFBMode() {
        // set up listeners on location and time update
        flashBackMode = true;

        // time update: use repeat alarm
        long[] updateMillTime = getUpdateTimeMills();
        alarmPendingIntents = new PendingIntent[updateMillTime.length];

        for (int i = 0; i < updateMillTime.length; i++) {
            Intent timeIntent = new Intent(CurrSongActivity.this, AlarmReceiver.class);
            alarmPendingIntents[i] = PendingIntent.getBroadcast(CurrSongActivity.this,
                    i, timeIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, updateMillTime[i],
                    AlarmManager.INTERVAL_DAY, alarmPendingIntents[i]);
        }

        // redraw the buttons
        flashBackButton.setBackground(getDrawable(R.drawable.fb_enabled));
        playListButton.setVisibility(View.VISIBLE);

        // force a location update to enter the flachback mode play list
        startMusicPlayerServiceFBMode(false);
    }

    private void disableFBMode() {
        // unregister all listeners
        flashBackMode = false;

        // unregister time listener
        for (int i = 0; i < alarmPendingIntents.length; i++) {
            alarmManager.cancel(alarmPendingIntents[i]);
            alarmPendingIntents[i] = null;
        }
        alarmPendingIntents = null;

        // redraw button
        flashBackButton.setBackground(getDrawable(R.drawable.fb_disabled));
        playListButton.setVisibility(View.GONE);

        // pass empty playlist to signal no song should be played
        ArrayList<Integer> stoplist = new ArrayList<>();
        Intent playerIntent = new Intent(CurrSongActivity.this, MusicPlayerService.class);
        playerIntent.putIntegerArrayListExtra(PositionPlayList.POS_LIST_INTENT, stoplist);
        playerIntent.putExtra(MusicPlayerActivity.START_MUSICSERVICE_KEEP_CURRPLAY, true);
        startService(playerIntent);

    }

    /**
     * call start service of music service and pass in a flashback playlist.
     * Use a parameter to indicate if the list is updated or a fresh start.
     * In the second case, we wait for the current song to finish and
     * play the next song.
     * @param update
     */
    private void startMusicPlayerServiceFBMode(boolean update) {
        PositionPlayList ppl = new PositionPlayList(lastLatLngCache, ZonedDateTime.now());
        Intent playerIntent = new Intent(CurrSongActivity.this, MusicPlayerService.class);
        positionList = ppl.getPositionList();
        playerIntent.putIntegerArrayListExtra(PositionPlayList.POS_LIST_INTENT, positionList);
        playerIntent.putExtra(MainActivity.START_MUSICSERVICE_KEEP_CURRPLAY, update);
        startService(playerIntent);
        
        // send a toast for updated list
        if (update)
            Toast.makeText(this, "Playlist updated", Toast.LENGTH_SHORT).show();

        Log.d(TAG, "Flashback mode service started.");
    }

    // get the string location from a longitude and latitude
    private String getStrAddress(LatLng ll) {
        if (ll == null)
            return NO_INFO;

        double lng = ll.longitude;
        double lat = ll.latitude;

        if (lat != 0 && lng != 0) {
            try {
                Geocoder geocoder = new Geocoder(this);
                List<Address> addresses = geocoder.getFromLocation(lat, lng, 1);
                String address = addresses.get(0).getAddressLine(0);
                String city = addresses.get(0).getAddressLine(1);
                String country = addresses.get(0).getAddressLine(2);
                Log.d(TAG, "address = " + address + ", city = " + city + ", country = " + country);
                return address + ", " + city;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return NO_INFO;
    }

    // get a date in format of "DD-MM-YYYY, DayOfWeek" or "---" if no info is available
    private String getStrDate(ZonedDateTime zdt) {
        if (zdt == null)
            return NO_INFO;

        return zdt.format(
                DateTimeFormatter.ofPattern("dd-MM-yyyy, EEEE")
        );
    }

    // get a time in the format of "HH:MM AM/PM" or "---" if no info is available
    private String getStrClock(ZonedDateTime zdt) {
        if (zdt == null)
            return NO_INFO;

        return zdt.format(
                DateTimeFormatter.ofPattern("hh:mm a")
        );
    }

    // get an array of millisecond times corresponding to the update times.
    private long[] getUpdateTimeMills() {
        long[] result = new long[UPDATE_TIME.length];

        // convert each time to a calender-produced millisecond. needed for the AlarmManager
        for (int i = 0; i < UPDATE_TIME.length; i++) {
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, UPDATE_TIME[i]);
            calendar.set(Calendar.MINUTE, 0);   // assumed all update take place at xx:00:00...
            calendar.set(Calendar.SECOND, 0);

            // avoid passed time
            if (calendar.getTime().compareTo(new Date()) < 0)
                calendar.add(Calendar.DAY_OF_MONTH, 1);

            result[i] = calendar.getTimeInMillis();
        }
        return result;
    }

    // AlarmReceiver class that handles the time update. Required.
    public class AlarmReceiver extends BroadcastReceiver {

        // when receive an update, start a new fb playlist
        @Override
        public void onReceive(Context context, Intent intent) {
            startMusicPlayerServiceFBMode(true);
        }
    }
}
