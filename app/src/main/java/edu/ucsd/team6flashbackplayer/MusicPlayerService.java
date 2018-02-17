package edu.ucsd.team6flashbackplayer;

import android.Manifest;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;

import static android.media.MediaPlayer.TrackInfo.MEDIA_TRACK_TYPE_AUDIO;

public class MusicPlayerService extends Service implements MediaPlayer.OnCompletionListener,
        MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener,
        MediaPlayer.OnInfoListener {

    final static String BROADCAST_SONG_CHANGE = "uiUpdate";
    private final static String TAG = "MusicPlayerService";
    private final IBinder iBinder = new LocalBinder();  // unused
    private List<Song> songs = SongList.getSongs();     // global song list
    private int counter = 0;                        // current song position counter
    private MediaPlayer mediaPlayer;                // media player that plays the song
    private static List<Integer> positionList;      // list of songs to be played

    private LocalBroadcastManager localBroadcastManager;
    private BroadcastReceiver songUpdateRequestReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            broadcastSongChange();
        }
    };
    private LocationManager locationManager;
    private static Location currLoc;            // current location updated with location
    private LatLng songLatLngCache;             // cache the location of a song on start playing
    private ZonedDateTime songDateTimeCache;    // cache the time of a song on start playing

    public MusicPlayerService() {

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (localBroadcastManager == null)
            localBroadcastManager = LocalBroadcastManager.getInstance(this);

        if (locationManager == null)
            locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);


        // If permission is granted, use the location.
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {

            currLoc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

            // Request location updates:
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0,
                    new LocationListener() {

                        /**
                         * Upate the current location.
                         * @param location
                         */
                        @Override
                        public void onLocationChanged(Location location) {
                            currLoc = location;
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
                    });
        }

        // get the playlist
        try {
            positionList = intent.getExtras().getIntegerArrayList("posList");
        } catch (NullPointerException | IndexOutOfBoundsException e) {
            stopMedia();
            broadcastSongChange();
            stopSelf();
        }

        if (positionList != null && positionList.size() != 0) {
            counter = 0;
            initMediaPlayer();
        } else if (positionList.size() == 0) {
            stopMedia();
            broadcastSongChange();
            stopSelf();
        }

        localBroadcastManager.registerReceiver(songUpdateRequestReceiver,
                new IntentFilter(MusicPlayerActivity.BROADCAST_REQUEST_SONG_UPDATE)
        );

        return super.onStartCommand(intent, flags, startId);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            stopMedia();
            mediaPlayer.release();
        }
    }

    private void initMediaPlayer() {
        if (mediaPlayer == null)
            mediaPlayer = new MediaPlayer();
        //Set up MediaPlayer event listeners
        mediaPlayer.setOnCompletionListener(this);
        mediaPlayer.setOnErrorListener(this);
        mediaPlayer.setOnPreparedListener(this);
        mediaPlayer.setOnInfoListener(this);

        prepSongAsync();
    }

    // prepare the mediaPlayer to play song at current counter position
    private void prepSongAsync() {
        mediaPlayer.reset();
        try {
            // Set the data source to the mediaFile location
            AssetFileDescriptor afd = getAssets().openFd((songs.get(positionList.get(counter)).getId()));
            mediaPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
        } catch (IOException e) {
            e.printStackTrace();
            stopSelf();
        } catch (ArrayIndexOutOfBoundsException e) {
            stopSelf();
        }
        mediaPlayer.prepareAsync();
    }

    private void playMedia() {
        if (!mediaPlayer.isPlaying()) {
            mediaPlayer.start();
        }
    }

    private void stopMedia() {
        if (mediaPlayer == null) return;
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
        }
    }


    // broadcast song change, latest position.
    public void broadcastSongChange() {
        int index;
        try {
            index = positionList.get(counter);
            Log.d(TAG, "Broadcast song change, position " + index);
        }
        catch (IndexOutOfBoundsException | NullPointerException e) {
            index = -1;
        }
        Intent intent = new Intent(BROADCAST_SONG_CHANGE);
        intent.putExtra(BROADCAST_SONG_CHANGE, index);
        localBroadcastManager.sendBroadcast(intent);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return iBinder;
    }

    /**
     * Callback function when a song completes
     * @param mp media player associated with the song.
     */
    @Override
    public void onCompletion(MediaPlayer mp) {
        //Invoked when playback of a media source has completed

        if (positionList.size() == 0) {
            stopSelf();
            broadcastSongChange();
        }
        // The Song whose info we need to update
        Song toUpdate = songs.get(positionList.get(counter));
        // Update the most recently played song's latest location, datetime
        songs.get(positionList.get(counter)).setLatestLoc(songLatLngCache);
        songs.get(positionList.get(counter)).setLatestTime(songDateTimeCache);

        stopMedia();
        counter += 1;

        // Update by calling a separate method
        updateLocTime(toUpdate, songDateTimeCache, songLatLngCache);
        /*SharedPreferences sp = getSharedPreferences("metadata", MODE_PRIVATE);
        int trackNum = mp.getSelectedTrack(MEDIA_TRACK_TYPE_AUDIO);
        String a = sp.getString(songs.get(trackNum).getId(),null);
        Log.d("meta", a);*/

        if (counter < positionList.size()) {
            prepSongAsync();
        }
        else {
            stopSelf();
            broadcastSongChange();
        }
    }

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

    @Override
    public boolean onInfo(MediaPlayer mp, int what, int extra) {
        //Invoked to communicate some info.
        return false;
    }

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
        }
        return false;
    }

    public class LocalBinder extends Binder {
        public MusicPlayerService getService() {
            return MusicPlayerService.this;
        }
    }


}
