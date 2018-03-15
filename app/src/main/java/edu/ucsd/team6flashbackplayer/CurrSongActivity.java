package edu.ucsd.team6flashbackplayer;

import android.Manifest;
import android.app.AlarmManager;
import android.app.DialogFragment;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import java.lang.ref.WeakReference;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static edu.ucsd.team6flashbackplayer.AppTime.UPDATE_TIME;

/**
 * Class CurrSongActivity
 * This class correspond to the currently playing song page, which is displayed
 * when the user select on the currently playing status bar from any navigation page.
 * It also shows up when the user enters the flashback mode.
 * In non-flashback mode, the user would the song's name, artist and playing history,
 * could like and dislike the song and navigate to the previous activity using the
 * back button.
 * In flashback mode, the back button is disabled, and an extra button will be shown
 * to allow user to review the current flashback playing list.
 *
 * This page constantly listen to the location change of user, which is needed to
 * update the flashback mode playlist. When flashback mode is entered, it also
 * listen to system time change (using the AlarmManager API) so the list is updated
 * on appropriate time.
 */
public class CurrSongActivity extends MusicPlayerActivity implements LocationListener, DateTimeSetterDialogFragment.DateTimeSetterClosedListener {

    static final String PLAYLIST_REQUEST = "playlistRequest";

    private static final String TAG = "CurrSongActivity";   // debug tag

    // Intent label for the intents passed to the AlarmReceiver indicating
    // which time it arrived
    private static final String FB_LIST_UPDATE_TIME_INDEX = "FBUpdateTimeIndex";
    // time for update the "time of day" period. 0 handles day change.
    private boolean flashBackMode;          // if FB is enabled

    // UI elements
    private Button flashBackButton;
    private TextView timeClockView;
    private TextView timeDateView;
    private TextView locationTextView;
    private TextView songTitleView;
    private TextView songArtistView;
    private TextView songAlbumView;
    private TextView lastUserView;
    private PreferenceButtons preferenceButtons;

    private ControlButtons controlButtons;

    // location manager to listen for location update & receiver
    private LocationManager locationManager;

    // Alarm manager to listen for time/day update & pending intents for cancel
    private AlarmManager alarmManager;
    private PendingIntent[] alarmPendingIntents;
    // trigger time of time updates
    private long[] updateTriggerMilliTime = new long[UPDATE_TIME.length];

    // location update frequency
    private final int LOC_UPDATE_MIN_TIME = 1000;       // milliseconds
    private final int LOC_UPDATE_MIN_DIST = 50;         // meters

    // location cache
    private LatLng lastLatLngCache;

    // receiver for friend change
    private BroadcastReceiver friendChangeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            startMusicPlayerServiceFBMode(true);
        }
    };

    private BroadcastReceiver fakeAlarmReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            startMusicPlayerServiceFBMode(true);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_curr_song);

        // set title of this activity
        setTitle(R.string.curr_song_activity_title);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // BCM
        localBroadcastManager = LocalBroadcastManager.getInstance(this);

        // Location manager
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        // AlarmManager
        alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        AlarmReceiver.setAssociatedActivity(this);

        // UI elements
        timeClockView = findViewById(R.id.time_clock_txt);
        timeDateView = findViewById(R.id.time_date_txt);
        locationTextView = findViewById(R.id.location_txt);
        songTitleView = findViewById(R.id.song_name);
        songArtistView = findViewById(R.id.song_artist);
        songAlbumView = findViewById(R.id.song_album);
        lastUserView = findViewById(R.id.user_txt);
        preferenceButtons = new PreferenceButtons(
                (ImageButton) findViewById(R.id.like_button),
                (ImageButton) findViewById(R.id.dislike_button),
                this
        );

        preferenceButtons.redrawButtons();
        PreferenceButtons.setLocalBroadcastManager(this);
        flashBackButton = findViewById(R.id.fb_button);
        setControlButtonsUI();

        final SharedPreferences.Editor editor = fbModeSharedPreferences.edit();
        flashBackMode = fbModeSharedPreferences.getBoolean("mode", false);

        // flashback mode button listener. Depending on the current state, enter or
        // exit the flashback mode.
        flashBackButton.setOnClickListener(v -> {
                Log.d(TAG, "FB mode before pressing: " + flashBackMode);
                flashBackMode = !flashBackMode;
                editor.putBoolean(FLASHBACK_SHAREDPREFERENCE_NAME, flashBackMode);
                editor.apply();
                if (flashBackMode)
                    enableFBMode();
                else
                    disableFBMode();
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

        Log.d(TAG, "FB mode on entering CurrSongActivity: " + flashBackMode);

        // enable FB mode if it should be in.
        // Otherwise just give a correct button background for not-in-fb.
        if (flashBackMode) {
            enableFBMode();
        } else {
            flashBackButton.setBackground(getDrawable(R.drawable.fb_disabled));
        }

    }

    private void setControlButtonsUI() {
        controlButtons = new ControlButtons(this,
                findViewById(R.id.pause_play),
                findViewById(R.id.skip),
                getDrawable(R.drawable.ic_pause_blue_24dp),
                getDrawable(R.drawable.ic_play_arrow_blue_24dp),
                getDrawable(R.drawable.ic_skip_next_blue_24dp));
    }

    /**
     * Disabled the back button when FB mode is enabled.
     */
    @Override
    public void onBackPressed() {
        if (!flashBackMode) {
            super.onBackPressed();
        }
    }

    /**
     * Create the menu of the app
     * @param menu menu object
     * @return ignored
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_playing, menu);
        return true;
    }

    /**
     * Handles menu item click. In this case both are for download.
     * @param item item clicked
     * @return result of handle
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.pick_fixed_time) {
            DialogFragment dateTimeSetterDialogFragment = new DateTimeSetterDialogFragment();
            dateTimeSetterDialogFragment.show(getFragmentManager(), getResources().getString(R.string.pick_time));
        }
        else if (id == R.id.use_sys_time){
            AppTime.unsetFixedTime();
            setVibeAlarmReceiver();
        }

        else if (id == R.id.show_playlist) {
            startFBListActivity();
        }


        return super.onOptionsItemSelected(item);
    }

    /**
     * Called when a song update broadcast is received. Re-populate the UI field.
     * @param position Position of the new song in the global playlist.
     */
    @Override
    protected void onSongUpdate(int position, boolean status) {

        final Song currSong = SongList.getSongs().get(position);
        Log.d(TAG, "Updating UI information to song " + currSong.getTitle());
        Log.d(TAG, "The id of the song is " + currSong.getId());
        lastUserView.setText(User.displayString(currSong.getLastPlayedUserUid()));

        songTitleView.setText(currSong.getTitle());
        songTitleView.setSelected(true);
        songArtistView.setText(currSong.getArtist());
        songArtistView.setSelected(true);
        songAlbumView.setText(currSong.getAlbum());
        songAlbumView.setSelected(true);
        locationTextView.setText(getStrAddress(currSong.getLatestLoc()));
        locationTextView.setSelected(true);
        timeDateView.setText(getStrDate(currSong.getLatestTime()));
        timeDateView.setSelected(true);
        timeClockView.setText(getStrClock(currSong.getLatestTime()));
        timeClockView.setSelected(true);
        preferenceButtons.setSong(currSong);
        preferenceButtons.setButtonListeners();
        preferenceButtons.redrawButtons();

        if (status) {
            controlButtons.setPause();
        }
        else {
            controlButtons.setPlay();
        }

        controlButtons.setButtonListeners();
    }

    /**
     * Called when all songs passed to the musicservice are finished.
     * Reset all the UI fields.
     */
    @Override
    protected void onAllSongsFinish() {

        Log.d(TAG, "Updating UI information, all songs finished.");

        songTitleView.setText(NO_INFO);
        songArtistView.setText(NO_INFO);
        locationTextView.setText(NO_INFO);
        timeDateView.setText(NO_INFO);
        timeClockView.setText(NO_INFO);
        lastUserView.setText(NO_INFO);
        songAlbumView.setText(NO_INFO);
        preferenceButtons.setSong(null);
        preferenceButtons.removeButtonListeners();
        preferenceButtons.redrawButtons();

        controlButtons.setPlay();
        controlButtons.unsetButtonListeners();

    }

    /**
     * Unregister the receivers on exit this page.
     * This include the location receiver and alarm receiver.
     */
    @Override
    public void onDestroy() {
        Log.d(TAG, "Removing all receivers.");

        super.onDestroy();
        AlarmReceiver.removeAssociatedActivity();
        locationManager.removeUpdates(this);

        if (alarmPendingIntents != null) {
            for (int i = 0; i < alarmPendingIntents.length; i++) {
                if (alarmPendingIntents[i] != null) {
                    alarmManager.cancel(alarmPendingIntents[i]);
                    alarmPendingIntents[i] = null;
                }
            }
        }
        alarmPendingIntents = null;
    }

    //------------------------LOCATION LISTENER METHODS-----------------------
    /**
     * Location listener method.
     * cache the latest location to the lastLatLngCache field.
     * If in FB mode, send a new playlist to the service.
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

    /**
     * Unused.
     * @param provider
     * @param status
     * @param extras
     */
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    /**
     * Unused.
     * @param provider
     */
    @Override
    public void onProviderEnabled(String provider) {

    }

    /**
     * Unused.
     * @param provider
     */
    @Override
    public void onProviderDisabled(String provider) {

    }

    //--------------------------END LOCATION LISTENER METHOD-----------------------

    /**
     * Start the FBListActivity which is a listview of current playlist of the flashback mode.
     */
    private void startFBListActivity() {
        Intent intent = new Intent(CurrSongActivity.this, PlayListActivity.class);
        startActivity(intent);
    }

    /**
     * Enters the flashback mode.
     * Registers the time-change listeners.
     * Acquire a FB playlist via PositionPlayListFactory and pass it to the MusicService
     * (delegated to startMusicPlayerServiceFBMode).
     */
    private void enableFBMode() {

        Log.d(TAG, "Enabling Flashback mode... ");
        // set up listeners on location and time update

        // time update: use repeat alarm
        getUpdateTimeMills();
        alarmPendingIntents = new PendingIntent[updateTriggerMilliTime.length];

        if (AppTime.usingFixedTime()) {
            registerFakeAlarmReceivers();
        }
        else {
            registerAlarmReceivers();
        }

        // register friend listener
        localBroadcastManager.registerReceiver(friendChangeReceiver,
                new IntentFilter(Users.BROADCAST_FRIEND_CHANGE)
        );

        // redraw the buttons
        flashBackButton.setBackground(getDrawable(R.drawable.fb_enabled));

        startMusicPlayerServiceFBMode(false);
    }

    /**
     * register the fake alarm receiver to receive update on mock times
     */
    private void registerFakeAlarmReceivers() {
        localBroadcastManager.registerReceiver(fakeAlarmReceiver,
                new IntentFilter(AppTime.BROADCAST_FAKE_TIME_UPDATE)
        );
    }

    /**
     * register the real alarm receiver to receive update on real times.
     */
    private void registerAlarmReceivers() {
        // Okay I tried setRepeat or set but none of them Fucking works. I have to
        // use this setExact.
        for (int i = 0; i < updateTriggerMilliTime.length; i++) {
            Intent timeIntent = new Intent(CurrSongActivity.this, AlarmReceiver.class);
            timeIntent.putExtra(FB_LIST_UPDATE_TIME_INDEX, i);
            alarmPendingIntents[i] = PendingIntent.getBroadcast(CurrSongActivity.this,
                    i, timeIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            Log.d(TAG, "Pending Intent added: " + alarmPendingIntents[i]);
            alarmManager.setExact(AlarmManager.RTC_WAKEUP,
                    updateTriggerMilliTime[i],
                    alarmPendingIntents[i]);
        }
    }

    /**
     * Exit the flashback mode.
     * Unregister the time change listeners, and send an empty list to the player service
     * with the update parameter to true so the player finish the current song and stops.
     */
    private void disableFBMode() {
        // unregister all listeners
        Log.d(TAG, "Disabling Flashback mode... ");
        flashBackMode = false;

        unregisterAlarmReceivers();
        unregisterFakeAlarmReceivers();

        localBroadcastManager.unregisterReceiver(friendChangeReceiver);

        // redraw button
        flashBackButton.setBackground(getDrawable(R.drawable.fb_disabled));

        // pass empty playlist to signal no song should be played
        ArrayList<Integer> stoplist = new ArrayList<>();
        Intent playerIntent = new Intent(CurrSongActivity.this, MusicPlayerService.class);
        playerIntent.putIntegerArrayListExtra(PositionPlayListFactory.POS_LIST_INTENT, stoplist);
        playerIntent.putExtra(MusicPlayerActivity.START_MUSICSERVICE_KEEP_CURRPLAY, true);

        startService(playerIntent);

    }

    /**
     * Remove the fake alarm receiver (broadcast receiver)
     */
    private void unregisterFakeAlarmReceivers() {
        try {
            localBroadcastManager.unregisterReceiver(fakeAlarmReceiver);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Remove the real alarm receiver
     */
    private void unregisterAlarmReceivers() {
        // unregister time listener
        if (alarmPendingIntents != null) {
            for (int i = 0; i < alarmPendingIntents.length; i++) {
                if (alarmPendingIntents[i] != null) {
                    alarmManager.cancel(alarmPendingIntents[i]);
                    Log.d(TAG, "Pending Intent removed: " + alarmPendingIntents[i]);
                    alarmPendingIntents[i] = null;
                }
            }
            alarmPendingIntents = null;
        }
    }

    /**
     * call start service of music service and pass in a flashback playlist.
     * Use a parameter to indicate if the list is updated or a fresh start.
     * In the second case, we wait for the current song to finish and
     * play the next song.
     * @param update if this start is an update of the list. If it is, this info
     *               will be passed to the player service and the currently playing song
     *               (if there's any) will finish before the new song in this list starts.
     */
    private void startMusicPlayerServiceFBMode(boolean update) {

        try {
            Intent playerIntent = new Intent(CurrSongActivity.this, MusicPlayerService.class);
            ArrayList<Integer> positionList = PositionPlayListFactory.makeList(lastLatLngCache, AppTime.getInstance());
            playerIntent.putIntegerArrayListExtra(PositionPlayListFactory.POS_LIST_INTENT, positionList);
            playerIntent.putExtra(MainActivity.START_MUSICSERVICE_KEEP_CURRPLAY, update);
            playerIntent.putExtra(MusicPlayerActivity.START_MUSICSERVICE_VIBE_MODE, true);

            startService(playerIntent);

            // send a toast for updated list
            if (update) {
                Toast.makeText(this, "Playlist updated", Toast.LENGTH_SHORT).show();
                broadcastPlaylistRequest();
            }

            Log.d(TAG, "Flashback mode service started.");
        }
        catch (Exception e) {
            Log.d(TAG, "Exception caught when starting FB mode, list didn't update.");
            e.printStackTrace();
        }

    }

    /**
     * Get a string address from a LatLng. THe returned string is detailed address and city
     * This is done by using the Geocoder class.
     * @param ll latitude and longitude.
     * @return A string address corresponding to the latlng. if not available, will return
     *         the NO_INFO string.
     */
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
                Log.d(TAG, "String address: address = " + address + ", city = " + city + ", country = " + country);
                return address + ", " + city;
            } catch (Exception e) {
                // things like overtime, etc.
                // consider run this func. asynchronously.
                e.printStackTrace();
            }
        }
        return NO_INFO;
    }

    /**
     * Get a date in format of "DD-MM-YYYY, DayOfWeek" or NO_INFO if no info is available
     * @param zdt zoneddatetime instance for the time to be formatted
     * @return "DD-MM-YYYY, DayOfWeek" or NO_INFO if no info is available
     */
    private String getStrDate(ZonedDateTime zdt) {
        if (zdt == null)
            return NO_INFO;

        return zdt.format(
                DateTimeFormatter.ofPattern("dd-MM-yyyy, EEEE")
        );
    }

    /**
     * Get a date in format of "hh:mm am/pm" or NO_INFO if no info is available
     * @param zdt zoneddatetime instance for the time to be formatted
     * @return "hh:mm am/pm" DayOfWeek" or NO_INFO if no info is available
     */
    private String getStrClock(ZonedDateTime zdt) {
        if (zdt == null)
            return NO_INFO;

        return zdt.format(
                DateTimeFormatter.ofPattern("hh:mm a")
        );
    }

    private void broadcastPlaylistRequest() {
        Intent intent = new Intent(PLAYLIST_REQUEST);
        localBroadcastManager.sendBroadcast(intent);
        Log.d(TAG, "Broadcast playlist request");
    }

    /**
     * populate the array of millisecond times corresponding to the initial update times.
     */
    private void getUpdateTimeMills() {

        // convert each time to a calender-produced millisecond. needed for the AlarmManager
        for (int i = 0; i < UPDATE_TIME.length; i++) {
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, UPDATE_TIME[i]);
            calendar.set(Calendar.MINUTE, 0);   // assumed all update take place at xx:00:00...
            calendar.set(Calendar.SECOND, 0);

            // avoid passed time
            if (calendar.getTime().compareTo(new Date()) < 0)
                calendar.add(Calendar.DAY_OF_MONTH, 1);

            updateTriggerMilliTime[i] = calendar.getTimeInMillis();
            Log.d(TAG, "getTimeInMillis returned: " + updateTriggerMilliTime[i]);
        }
    }

    /**
     * When an app time is picked, reset the receiver of time update to proper real/fake receiver.
     */
    @Override
    public void onDialogClosed() {
        Log.d(TAG, "Picker dialog destroyed");
        setVibeAlarmReceiver();
    }

    private void setVibeAlarmReceiver() {
        if (flashBackMode) {
            if (AppTime.usingFixedTime()) {
                unregisterAlarmReceivers();
                registerFakeAlarmReceivers();
            }
            else {
                unregisterFakeAlarmReceivers();
                registerAlarmReceivers();
            }
        }
    }


    /**
     * AlarmReceiver class that handles the time update. Required for proper unregistering.
     */
    public static class AlarmReceiver extends BroadcastReceiver {

        // activity associated with this receiver.
        private static WeakReference<CurrSongActivity> associatedActivity;

        /**
         * Constructor. Takes an CurrSongActivity as it's associatedactivity.
         * @param a a CurrSongActivity
         */
        public AlarmReceiver(CurrSongActivity a) {
            associatedActivity = new WeakReference<>(a);
        }

        /**
         * Set the CurrSongActivity (context) associated with the receiver.
         * Since AlarmReceiver is a static class, this method is needed to update
         * the associated activity.
         * @param a activity to be associated with
         */
        public static void setAssociatedActivity(CurrSongActivity a) {
            associatedActivity = new WeakReference<>(a);
        }

        /**
         * Remove the associatedActivity.
         * Call this on unregister to guarantee things are cleaned up.
         */
        public static void removeAssociatedActivity() {

            associatedActivity = null;
        }

        /**
         * No-arg ctor incase needed.
         */
        public AlarmReceiver() {
        }

        String TAG = "AlarmReceiver";

        /**
         * Call back function when a specific time is reached.
         * Update the FB playlist.
         * @param context Context that this request if from
         * @param intent intent with information of which scheduled time is arrived.
         */
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "Update flashback list triggered from AlarmReceiver.");
            if (associatedActivity != null && associatedActivity.get() != null) {
                try {
                    int i = intent.getExtras().getInt(FB_LIST_UPDATE_TIME_INDEX, -1);

                    // valid case
                    if (i != -1) {

                        // remove the current alarm (which is passed) and schedule for a day later.
                        associatedActivity.get().alarmManager.cancel(associatedActivity.get().alarmPendingIntents[i]);
                        associatedActivity.get().updateTriggerMilliTime[i] += AlarmManager.INTERVAL_DAY;
                        Intent timeIntent = new Intent(associatedActivity.get(), AlarmReceiver.class);
                        timeIntent.putExtra(FB_LIST_UPDATE_TIME_INDEX, i);
                        associatedActivity.get().alarmPendingIntents[i] = PendingIntent.getBroadcast(associatedActivity.get(),
                                i, timeIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                        Log.d(TAG, "Pending Intent added: " + associatedActivity.get().alarmPendingIntents[i]);
                        associatedActivity.get().alarmManager.setExact(AlarmManager.RTC_WAKEUP,
                                associatedActivity.get().updateTriggerMilliTime[i],
                                associatedActivity.get().alarmPendingIntents[i]);
                        associatedActivity.get().startMusicPlayerServiceFBMode(true);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }


}
