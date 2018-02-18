package edu.ucsd.team6flashbackplayer;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

public class MusicPlayerService extends Service implements MediaPlayer.OnCompletionListener,
        MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener,
        MediaPlayer.OnInfoListener, LocationListener {

    final static String BROADCAST_SONG_CHANGE = "uiUpdate";
    private final static String TAG = "MusicPlayerService";
    private final IBinder iBinder = new LocalBinder();  // unused
    private List<Song> songs = SongList.getSongs();     // global song list
    private int counter = 0;                        // current song position counter
    private MediaPlayer mediaPlayer;                // media player that plays the song
    private static List<Integer> positionList;      // list of songs to be played

    private LocalBroadcastManager localBroadcastManager;
    private boolean locationListenerRegistered = false;
    private BroadcastReceiver songUpdateRequestReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            broadcastSongChange();
        }
    };
    private boolean songUpdateReceiverRegistered = false;


    private BroadcastReceiver songDislikedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            onSongDisliked(intent.getIntExtra(PreferenceButtons.PREF_DISLIKED_BROADCAST, -1));
        }
    };
    private boolean songDislikeReceiverRegistered = false;

    private LocationManager locationManager;
    private static Location currLoc;            // current location updated with location
    private LatLng songLatLngCache;             // cache the location of a song on start playing
    private ZonedDateTime songDateTimeCache;    // cache the time of a song on start playing

    // notification
    private NotificationManager notificationManager;
    private static final String NOTIFICATION_CHANNEL_ID = "musicPlayerChannel";
    private static final String NOTIFICATION_CHANNEL_NAME = "MusicPlayer";
    private static final int FOREGROUND_ID = 1;

    public MusicPlayerService() {

    }

    /* ---------------------OVERRIDE SERVICE------------------------- */

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {


        // put the activity in foreground so it won't die
        Intent notificationIntent = new Intent(this, MusicPlayerActivity.class);
        PendingIntent pendingIntent =
                PendingIntent.getActivity(this, 0, notificationIntent, 0);

        if (notificationManager == null)
            notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID,
                    NOTIFICATION_CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("Flashback Music Player");
            channel.setSound(null, null);
            notificationManager.createNotificationChannel(channel);
        }


        Notification notification =
                new Notification.Builder(this, NOTIFICATION_CHANNEL_ID)
                        .setContentTitle(getText(R.string.notification_title))
                        .setContentText(getText(R.string.notification_message))
                        .setSmallIcon(R.drawable.ic_player_icon)
                        .setContentIntent(pendingIntent)
                        .build();

        startForeground(FOREGROUND_ID, notification);

        if (localBroadcastManager == null)
            localBroadcastManager = LocalBroadcastManager.getInstance(this);

        if (locationManager == null)
            locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);

        if (!locationListenerRegistered) {
            // If permission is granted, use the location.
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {

                currLoc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                // Request location updates:
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0,
                        this);
            }
            locationListenerRegistered = true;
        }

        registerBroadcastReceivers();

        // get the playlist. A playlist passed in from StartService will ALWAYS
        // REPLACE the current playlist, and the current playlist will stop immediately.
        try {
            Bundle extras = intent.getExtras();
            ArrayList<Integer> inList = extras.getIntegerArrayList(PositionPlayList.POS_LIST_INTENT);
            boolean keepCurrSong = extras.getBoolean(MusicPlayerActivity.START_MUSICSERVICE_KEEP_CURRPLAY, false);

            if (inList != null) {
                initMediaPlayer();
                if (!keepCurrSong || !mediaPlayer.isPlaying()) {

                    counter = 0;
                    positionList = inList;
                    if (inList.size() == 0) {
                        stopMedia();
                        broadcastSongChange();
                        stopForeground(STOP_FOREGROUND_REMOVE); stopSelf();
                    }
                    else
                        prepSongAsync();
                }
                else {
                    inList.add(0, positionList.get(counter));
                    counter = 0;
                    positionList = inList;
                }
            }


        } catch (NullPointerException | IndexOutOfBoundsException e) {
            stopMedia();
            broadcastSongChange();
            stopForeground(STOP_FOREGROUND_REMOVE); stopSelf(); 
        }


        return super.onStartCommand(intent, flags, startId);
    }


    @Override
    public void onDestroy() {
        Log.d(TAG, "On Destroy...");

        if (mediaPlayer != null) {
            stopMedia();
            mediaPlayer.release();
            mediaPlayer = null;
        }

        if (locationListenerRegistered) {
            locationManager.removeUpdates(this);
            locationListenerRegistered = false;
        }

        if (localBroadcastManager != null) {
            unregisterBroadcastReceivers();
        }
        
        super.onDestroy();
    }

    /* ---------------------OVERRIDE LOCATIONLISTENER------------------------ */
    /**
     * Upate the current location.
     * @param location
     */
    @Override
    public void onLocationChanged(Location location) {
        currLoc = location;
        Log.d(TAG, "Location updated, Lat: " + location.getLatitude() + ", Lng: " + location.getLongitude() +
            ", this instance: " + MusicPlayerService.this);
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

    /* ---------------------OVERRIDE MEDIAPLAYER.ON???------------------------- */

    /**
     * Callback function when prepareAsync() finish
     * @param mp media player associated with the player.
     */
    @Override
    public void onPrepared(MediaPlayer mp) {
        playMedia();

        // Get Android's current location
        try {
            songLatLngCache = new LatLng(currLoc.getLatitude(), currLoc.getLongitude());
            Log.d(TAG, "LatLng cache updated: " + songLatLngCache.latitude + ", " + songLatLngCache.longitude);
        }
        // location unavailable, put null
        catch (NullPointerException e) {
            songDateTimeCache = null;
            Log.d(TAG, "Location cache updated: Null.");
        }

        songDateTimeCache = ZonedDateTime.now();

        // update UI by broadcast
        broadcastSongChange();
    }

    /**
     * Callback function when a song completes
     * @param mp media player associated with the song.
     */
    @Override
    public void onCompletion(MediaPlayer mp) {
        //Invoked when playback of a media source has completed

        if (positionList.size() == 0) {
            stopForeground(STOP_FOREGROUND_REMOVE); stopSelf(); 
            broadcastSongChange();
        }
        // The Song whose info we need to update
        Song toUpdate = songs.get(positionList.get(counter));
        // Update the most recently played song's latest location, datetime

        // Update by calling a separate method
        updateLocTime(toUpdate, songDateTimeCache, songLatLngCache);
        /*SharedPreferences sp = getSharedPreferences("metadata", MODE_PRIVATE);
        int trackNum = mp.getSelectedTrack(MEDIA_TRACK_TYPE_AUDIO);
        String a = sp.getString(songs.get(trackNum).getId(),null);
        Log.d("meta", a);*/

        nextSong();

    }

    //Handle errors
    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        switch (what) {
            case MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK:
                Log.d(TAG, "MEDIA ERROR NOT VALID FOR PROGRESSIVE PLAYBACK " + extra);
                break;
            case MediaPlayer.MEDIA_ERROR_SERVER_DIED:
                Log.d(TAG, "MEDIA ERROR SERVER DIED " + extra);
                break;
            case MediaPlayer.MEDIA_ERROR_UNKNOWN:
                Log.d(TAG, "MEDIA ERROR UNKNOWN " + extra);
                break;
            default:
                Log.d(TAG, "MEDIA ERROR");
        }
        return false;
    }

    @Override
    public boolean onInfo(MediaPlayer mp, int what, int extra) {
        //Invoked to communicate some info.
        return false;
    }

    /* ---------------------MEDIA PLAYER CONTROL------------------------- */

    private void initMediaPlayer() {
        if (mediaPlayer == null)
            mediaPlayer = new MediaPlayer();
        //Set up MediaPlayer event listeners
        mediaPlayer.setOnCompletionListener(this);
        mediaPlayer.setOnErrorListener(this);
        mediaPlayer.setOnPreparedListener(this);
        mediaPlayer.setOnInfoListener(this);

    }

    // prepare the mediaPlayer to play song at current counter position
    // ignore the songs that are disliked.
    private void prepSongAsync() {
        mediaPlayer.reset();

        try {
            // Set the data source to the mediaFile location
            Song currSong = songs.get(positionList.get(counter));

            while (currSong.isDisliked()) {
                counter += 1;
                currSong = songs.get(positionList.get(counter));
            }

            AssetFileDescriptor afd = getAssets().openFd(currSong.getId());
            mediaPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
        } catch (IOException e) {
            e.printStackTrace();
            stopForeground(STOP_FOREGROUND_REMOVE); stopSelf(); 
        } catch (ArrayIndexOutOfBoundsException e) {
            broadcastSongChange();
            stopForeground(STOP_FOREGROUND_REMOVE); stopSelf();
        }
        mediaPlayer.prepareAsync();
    }

    private void playMedia() {
        if (!mediaPlayer.isPlaying()) {
            mediaPlayer.start();
        }
    }

    /**
     * play the next song of this list.
     * If there's no more songs, stop the service and broadcast -1 to
     * indicate all songs are finished.
     */
    private void nextSong() {
        stopMedia();
        counter += 1;

        if (counter < positionList.size()) {
            prepSongAsync();
        }
        else {
            stopForeground(STOP_FOREGROUND_REMOVE); stopSelf(); 
            broadcastSongChange();
        }
    }

    private void stopMedia() {
        if (mediaPlayer == null) return;
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
        }
    }


    /* ---------------------BROADCAST RELATIVE------------------------ */


    // broadcast song change, latest position.
    public void broadcastSongChange() {
        int index;
        try {
            index = positionList.get(counter);

        }
        catch (IndexOutOfBoundsException | NullPointerException e) {
            index = -1;
        }
        Intent intent = new Intent(BROADCAST_SONG_CHANGE);
        intent.putExtra(BROADCAST_SONG_CHANGE, index);
        localBroadcastManager.sendBroadcast(intent);
        Log.d(TAG, "Broadcast song change, position " + index + ", this instance" + this);
    }

    /**
     * Callback function for when a song is disliked.
     * Check if the disliked song is currently playing. If it is, goes to the next song.
     */
    private void onSongDisliked(int position) {
        // if the media player is not playing, we don't have to worry.
        // if we somehow passed in an invalid position, also ignore it.
        if (mediaPlayer == null || !mediaPlayer.isPlaying() || position == -1)
            return;

        // otherwise check if the disliked song is currently playing
        if (position == positionList.get(counter)) {
            nextSong();
        }

    }

    /**
     * Register the broadcast receiver callbacks for the service.
     * Currently includes songUpdateRequest and songDisliked
     */
    private void registerBroadcastReceivers() {
        if (!songUpdateReceiverRegistered) {
            localBroadcastManager.registerReceiver(songUpdateRequestReceiver,
                    new IntentFilter(MusicPlayerActivity.BROADCAST_REQUEST_SONG_UPDATE)
            );
            songUpdateReceiverRegistered = true;
        }

        if (!songDislikeReceiverRegistered) {
            localBroadcastManager.registerReceiver(songDislikedReceiver,
                    new IntentFilter(PreferenceButtons.PREF_DISLIKED_BROADCAST));
            songDislikeReceiverRegistered = true;
        }
    }

    /**
     * Unregister all the broadcast receivers
     */
    private void unregisterBroadcastReceivers() {
        localBroadcastManager.unregisterReceiver(songUpdateRequestReceiver);
        localBroadcastManager.unregisterReceiver(songDislikedReceiver);
    }

    /* ---------------------UPDATE SONG SP------------------------ */

    public void updateLocTime(Song song, ZonedDateTime time, LatLng loc) {
        SharedPreferences sp = getSharedPreferences("metadata", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        String json = sp.getString(song.getId(),null);
        Log.d("meta old", json);
        song.updateLocTime(time,loc);
        String newJson = song.jsonParse();

        editor.putString(song.getId(), newJson);
        editor.apply();
        Log.d("meta new", newJson);
    }

    /* ---------------------BINDER STUFF------------------------ */



    @Override
    public IBinder onBind(Intent intent) {
        return iBinder;
    }

    public class LocalBinder extends Binder {
        public MusicPlayerService getService() {
            return MusicPlayerService.this;
        }
    }


}
