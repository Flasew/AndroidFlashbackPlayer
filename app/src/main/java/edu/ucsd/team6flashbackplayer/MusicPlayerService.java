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

/**
 * class MusicPlayerService
 * This class is an android service, and handles music play back for the player program.
 * In general, the music player service is always started by startService(intent) where
 * the intent contains a integer arraylist corresponding to the position of the songs in
 * the global list. The intent should also consist a field indicating if the currently playing
 * song should just end for the new list or wait until the current song finish.
 * MusicPlayerService also listens to the location change of the user when music is playing
 * in order to correctly record the location history.
 * This service is always started as a foreground service so it's unlikely to be killed by
 * the operating system.
 */
public class MusicPlayerService extends Service implements MediaPlayer.OnCompletionListener,
        MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener,
        MediaPlayer.OnInfoListener, LocationListener {

    // label of broadcasting song change.
    static String BROADCAST_SONG_CHANGE = "uiUpdate";
    static String BROADCAST_SONG_CHANGE_POSITION = "mediaPlayerPosition";
    static String BROADCAST_SONG_CHANGE_STATUS = "mediaPlayerStatus";

    static String BROADCAST_SONG_LIST = "currPositionPlayList";

    private final static String TAG = "MusicPlayerService";

    private final IBinder iBinder = new LocalBinder();  // unused

    private int counter = 0;                        // current song position counter
    private ArrayList<Integer> positionList;             // list of songs to be played

    private MediaPlayer mediaPlayer;                // media player that plays the song

    // broadcast
    private LocalBroadcastManager localBroadcastManager;

    // when a song update is requested, broad cast a song update.
    private BroadcastReceiver songUpdateRequestReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            broadcastSongChange();
        }
    };
    private boolean songUpdateReceiverRegistered = false;

    // when a song is disliked, the current playlist should be modified.
    private BroadcastReceiver songDislikedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            onSongDisliked(intent.getIntExtra(PreferenceButtons.PREF_DISLIKED_BROADCAST, -1));
        }
    };
    private boolean songDislikeReceiverRegistered = false;

    // location
    private LocationManager locationManager;
    private Location currLoc;            // current location updated with location
    private LatLng songLatLngCache;             // cache the location of a song on start playing
    private ZonedDateTime songDateTimeCache;    // cache the time of a song on start playing
    private boolean locationListenerRegistered = false;

    // notification
    private NotificationManager notificationManager;
    private static final String NOTIFICATION_CHANNEL_ID = "musicPlayerChannel";
    private static final String NOTIFICATION_CHANNEL_NAME = "MusicPlayer";
    private static final int FOREGROUND_ID = 1;
    
    // control
    private BroadcastReceiver songContolReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
              onSongControl(intent.getIntExtra(ControlButtons.CTRL_BROADCAST, -1)) ;   
        }
    };
    private boolean songControlReceiverRegistered = false;

    // song list
    private BroadcastReceiver songListRequestReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            broadcastSongList();
        }
    };
    private boolean songListRequestReceiverRegistered = false;

    // firebase download
    private boolean useFirebaseList = false;
    private BroadcastReceiver songDownloadHandledReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            onSongDownloadedHandled();
        }
    };

    private boolean songDownladedHandledReceiverRegistered = false;

    public MusicPlayerService() {

    }

    /* ---------------------OVERRIDE SERVICE------------------------- */

    /**
     * onStartCommand is always called when a new playlist passes in. If there's already a
     * service instance running, a new one will NOT be created. The intent contains the
     * position list information of the songs to be played.
     * @param intent intent containing an arraylist of positions and a boolean variable of if the
     *               song should be updated
     * @param flags  Unused.
     * @param startId Start id of this start. unused.
     * @return
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.d(TAG, "StartID: " + startId);

        // put the activity in foreground so it won't die
        Intent notificationIntent = new Intent(this, MusicPlayerActivity.class);
        PendingIntent pendingIntent =
                PendingIntent.getActivity(this, 0, notificationIntent, 0);

        if (notificationManager == null)
            notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Android O requires a notification channel.
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID,
                    NOTIFICATION_CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("Flashback Music Player");
            channel.setSound(null, null);
            notificationManager.createNotificationChannel(channel);
        }

        // notification channel is needed for a foreground service
        Notification notification =
                new Notification.Builder(this, NOTIFICATION_CHANNEL_ID)
                        .setContentTitle(getText(R.string.notification_title))
                        .setContentText(getText(R.string.notification_message))
                        .setSmallIcon(R.drawable.ic_player_icon)
                        .setContentIntent(pendingIntent)
                        .build();

        startForeground(FOREGROUND_ID, notification);
        Log.d(TAG, "Service brought to foreground.");

        // acquire the managers if needed.
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
        initMediaPlayer();

        // get the playlist. Depending on the current player status and the intent specifying update or not,
        // The player will either cut the current playlist immediately or wait for the current song to
        // finish and start the new list.
        try {
            Bundle extras = intent.getExtras();
            ArrayList<Integer> inList = extras.getIntegerArrayList(PositionPlayListFactory.POS_LIST_INTENT);
            boolean keepCurrSong = extras.getBoolean(MusicPlayerActivity.START_MUSICSERVICE_KEEP_CURRPLAY, false);
            useFirebaseList = extras.getBoolean(MusicPlayerActivity.START_MUSICSERVICE_VIBE_MODE, false);

            if (inList != null) {
                Log.d(TAG, "Incoming list is not null, playing from list");

                // check if the list is firebase list. If it is, immediately queue all the download.
                if (useFirebaseList) {
                    queueDownloads(inList);
                }

                // queue the firebase song's download
                if (!keepCurrSong || !mediaPlayer.isPlaying()) {

                    counter = 0;
                    positionList = inList;
                    if (inList.size() == 0) {
                        Log.d(TAG, "inList size is 0, music player service stopped");
                        stopMedia();
                        broadcastSongChange();
                        stopForeground(STOP_FOREGROUND_REMOVE);
                        stopSelf();
                    } else
                        prepSongAsync(true);
                } else {

                    inList.add(0, positionList.get(counter));
                    counter = 0;
                    positionList = inList;
                }
            } else {

                int pos = extras.getInt(SongActivity.SINGLE_SONG_INTENT, -1);

                Log.d(TAG, "Incoming list is null, playing a single song, position " + pos);

                positionList = new ArrayList<>();
                positionList.add(pos);
                counter = 0;
                prepSongAsync(false);
            }

        }
        // Invalid intent: just stop and exit.
        catch (NullPointerException | IndexOutOfBoundsException e) {
            Log.d(TAG, "Invalid incoming playlist, exiting...");
            e.printStackTrace();
            stopMedia();
            broadcastSongChange();
            stopForeground(STOP_FOREGROUND_REMOVE); stopSelf();
        }

        return super.onStartCommand(intent, flags, startId);
    }


    /**
     * Called when the service is destroyed. A MPService is destroyed when
     * all songs finish playing, or the OS forced to kill it.
     */
    @Override
    public void onDestroy() {
        Log.d(TAG, "MusicPlayerService "+ this + " destroyed...");

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

    @Override
    public void onTaskRemoved(Intent rootIntent){
        stopForeground(STOP_FOREGROUND_REMOVE); stopSelf();
    }

    /* ---------------------OVERRIDE LOCATIONLISTENER------------------------ */
    /**
     * Update the current location.
     * @param location latest location.
     */
    @Override
    public void onLocationChanged(Location location) {
        currLoc = location;
        Log.d(TAG, "Location updated, Lat: " + location.getLatitude() + ", Lng: " + location.getLongitude() +
            ", this instance: " + MusicPlayerService.this);
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

    /* ---------------------OVERRIDE MEDIAPLAYER.ON???------------------------- */

    /**
     * Callback function when prepareAsync() finish. Start to play the prepared media,
     * Cache the current location and time, and broadcast a songinfoupdate message.
     *
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

        songDateTimeCache = AppTime.getInstance();

        // update UI by broadcast
        broadcastSongChange();
    }

    /**
     * Callback function when a song completes. Update the song location and time history.
     * If all the songs are finished, stop the service.
     * @param mp media player associated with the song.
     */
    @Override
    public void onCompletion(MediaPlayer mp) {
        // Invoked when playback of a media source has completed

        if (positionList.size() == 0) {
            broadcastSongChange();
            stopForeground(STOP_FOREGROUND_REMOVE); stopSelf();
            return;
        }
        // The Song whose info we need to update
        Song toUpdate = SongList.getSongs().get(positionList.get(counter));
        // Update the most recently played song's latest location, datetime
        Log.d(TAG, "Song " + toUpdate +" finished playing.");

        // Update by calling a separate method
        updateLocTime(toUpdate, songDateTimeCache, songLatLngCache);
        /*SharedPreferences sp = getSharedPreferences("metadata", MODE_PRIVATE);
        int trackNum = mp.getSelectedTrack(MEDIA_TRACK_TYPE_AUDIO);
        String a = sp.getString(songs.get(trackNum).getPath(),null);
        Log.d("meta", a);*/

        nextSong();

    }

    /**
     * Media player error callback. Unused except for logging information.
     * @param mp media player this callback works on
     * @param what what's the error
     * @param extra extra information
     * @return false for now
     */
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

    /**
     * Invoked to communicate some info. Unused for now.
     * @param mp
     * @param what
     * @param extra
     * @return
     */
    @Override
    public boolean onInfo(MediaPlayer mp, int what, int extra) {
        //Invoked to communicate some info.
        return false;
    }

    /* ---------------------MEDIA PLAYER CONTROL------------------------- */

    /**
     * Initialize a media player and set the listeners.
     */
    private void initMediaPlayer() {
        if (mediaPlayer == null)
            mediaPlayer = new MediaPlayer();
        //Set up MediaPlayer event listeners
        mediaPlayer.setOnCompletionListener(this);
        mediaPlayer.setOnErrorListener(this);
        mediaPlayer.setOnPreparedListener(this);
        mediaPlayer.setOnInfoListener(this);

    }

    /**
     * prepare the mediaPlayer to play song at current counter position
     * ignore the songs that are disliked.
     */
    private void prepSongAsync(boolean skipDislike) {
        mediaPlayer.reset();
        Log.d(TAG, "Entering prepSongAsync...");
        try {
            // Set the data source to the mediaFile location
            Song currSong;
            int index = positionList.get(counter);
            List<Song> songList;
            if (!useFirebaseList) {
                songList = SongList.getSongs();
                currSong = songList.get(index);
            }
            else {

                for (Song s: SongList.getSongs()) {
                    Log.d(TAG, "Local songlist has item " + s.getTitle());
                }
                for (Song s: FirebaseSongList.getSongs()) {
                    Log.d(TAG, "Firebase songlist has item " + s.getTitle());
                }

                songList = FirebaseSongList.getSongs();
                int curr = counter;
                currSong = songList.get(index);
                Log.d(TAG, "curr: "  + curr);
                Log.d(TAG, "Currsong: " + currSong.getTitle());
                Log.d(TAG,"SongList.getSongs().indexOf(currSong) == -1: " + (SongList.getSongs().indexOf(currSong) == -1));

                while (SongList.getSongs().indexOf(currSong) == -1 && ++curr < positionList.size()) {
                    // the song didn't finish downloading, find the next available song and swap
                    Log.d(TAG, "Song " + currSong.getTitle() + " is not available locally, advancing...");
                    currSong = songList.get(positionList.get(curr));


                }

                Log.d(TAG, "curr after: "  + curr);

                if (curr < positionList.size()) {
                    // swap
                    int tmp = positionList.get(counter);
                    positionList.set(counter, positionList.get(curr));
                    positionList.set(curr, tmp);
                    broadcastSongList();

                }

                else if (curr >= positionList.size()) {
                    // if no song is ready, don't prepare anything and wait for the next download finish signal.
                    return;
                }
            }


            if (skipDislike) {
                while (currSong.isDisliked()) {
                    counter += 1;
                    currSong = songList.get(positionList.get(counter));
                }
            }

            mediaPlayer.setDataSource(MusicPlayerActivity.MUSIC_DIR + "/" + currSong.getPath());

            mediaPlayer.prepareAsync();
            Log.d(TAG, "Preparing song " + currSong.getTitle());

        } catch (IOException e) {
            Log.d(TAG, "PrepSongAsync got IO exception, exiting...");
            e.printStackTrace();
            stopForeground(STOP_FOREGROUND_REMOVE); stopSelf(); 
        } catch (ArrayIndexOutOfBoundsException | NullPointerException e) {

            Log.d(TAG, "PrepSongAsync got invalid list, exiting...");
            broadcastSongChange();
            stopForeground(STOP_FOREGROUND_REMOVE); stopSelf();
        }

    }

    /**
     * Play the media.
     */
    private void playMedia() {
        if (!mediaPlayer.isPlaying()) {
            mediaPlayer.start();
            Log.d(TAG, "Media player started");
        }
    }

    /**
     * Pause the media.
     */
    private void pauseMedia() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            Log.d(TAG, "Media player paused");
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
            prepSongAsync(true);
        }
        else {
            broadcastSongChange();
            stopForeground(STOP_FOREGROUND_REMOVE); stopSelf();
        }
    }

    /**
     * Stop the currently playing media.
     */
    private void stopMedia() {
        if (mediaPlayer == null) return;
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            Log.d(TAG, "Media player stopped");
        }
    }




    /* ---------------------BROADCAST RELATIVE------------------------ */


    /**
     * broadcast song change: broadcast the latest position.
     */
    public void broadcastSongChange() {
        int index;
        boolean playerStatus;

        try {
            if (useFirebaseList)
                index = SongList.getSongs().indexOf(FirebaseSongList.getSongs().get(positionList.get(counter)));
            else
                index = positionList.get(counter);

            playerStatus = mediaPlayer.isPlaying();
        }
        catch (IndexOutOfBoundsException | NullPointerException | IllegalStateException e ) {
            index = -1;
            playerStatus = false;
        }
        Intent intent = new Intent(BROADCAST_SONG_CHANGE);
        intent.putExtra(BROADCAST_SONG_CHANGE_POSITION, index);
        intent.putExtra(BROADCAST_SONG_CHANGE_STATUS, playerStatus);
        localBroadcastManager.sendBroadcast(intent);
        Log.d(TAG, "Broadcast song change, position " + index + ", this instance " + this);
    }

    /**
     * Broadcast the current playlist when requested.
     */
    private void broadcastSongList() {
        Log.d(TAG, "Broadcasting song list");

        Log.d(TAG, "positionList is null: " + (positionList == null));
        if (positionList != null)
            for(int i: positionList) {
                Log.d(TAG, "PosList has item " + i);
            }

        Intent intent = new Intent(BROADCAST_SONG_LIST);
        intent.putIntegerArrayListExtra(BROADCAST_SONG_LIST, positionList);
        localBroadcastManager.sendBroadcast(intent);
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
            Log.d(TAG, "Current song disliked, skipping to next song...");
            nextSong();
        }

    }

    /**
     * Called when songs are copied over to the music directory. Useful for firebase song list.
     * Start a new song if download finishes and no song was playing due to they're all offline.
     */
    private void onSongDownloadedHandled() {
        if (!useFirebaseList || mediaPlayer == null || mediaPlayer.isPlaying())
            return;

        prepSongAsync(true);
    }

    /**
     * "Callback funciton" for a song control event
     * @param controlCode code of this control
     */
    private void onSongControl(int controlCode) {
        if (mediaPlayer == null)
            return;

        switch (controlCode) {
            case ControlButtons.CTRL_PAUSE_PLAY:
                if (mediaPlayer.isPlaying()) {
                    pauseMedia();
                }

                else {
                    playMedia();
                }
                break;

            case ControlButtons.CTRL_SKIP:
                nextSong();
                break;

            default:
                break;

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

        if (!songControlReceiverRegistered) {
            localBroadcastManager.registerReceiver(songContolReceiver,
                    new IntentFilter(ControlButtons.CTRL_BROADCAST));
            songControlReceiverRegistered = true;
        }

        if (!songListRequestReceiverRegistered) {
            localBroadcastManager.registerReceiver(songListRequestReceiver,
                    new IntentFilter(CurrSongActivity.PLAYLIST_REQUEST));
            songControlReceiverRegistered = true;
        }

        if (!songDownladedHandledReceiverRegistered) {
            localBroadcastManager.registerReceiver(songDislikedReceiver,
                    new IntentFilter(VibeModeDownloadedFileHanlderDecorator.BROADCAST_FILE_HANDLED));
            songDownladedHandledReceiverRegistered = true;
        }

    }

    /**
     * Unregister all the broadcast receivers
     */
    private void unregisterBroadcastReceivers() {
        localBroadcastManager.unregisterReceiver(songUpdateRequestReceiver);
        songUpdateReceiverRegistered = false;
        localBroadcastManager.unregisterReceiver(songDislikedReceiver);
        songDislikeReceiverRegistered = false;
        localBroadcastManager.unregisterReceiver(songContolReceiver);
        songControlReceiverRegistered = false;
        localBroadcastManager.unregisterReceiver(songListRequestReceiver);
        songListRequestReceiverRegistered = false;
        localBroadcastManager.unregisterReceiver(songDownloadHandledReceiver);
        songDownladedHandledReceiverRegistered = false;
    }

    /* ---------------------UPDATE SONG SP------------------------ */


    /**
     * Update a song's latest location and time information, and append it to the history.
     * @param song song to be updated
     * @param time latest time
     * @param loc latest location
     */
    public void updateLocTime(Song song, ZonedDateTime time, LatLng loc) {
        SharedPreferences sp = getSharedPreferences("metadata", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        String json = sp.getString(song.getPath(),null);
        Log.d(TAG, "Meta old: " + json);
        // SongJsonParser.updateSongLocTime(song, time,loc);

        //editor.putString(song.getPath(), newJson);
        //editor.apply();

        // Update for Firebase
        FirebaseSongList.updateHistory(song, time, loc);
        FirebaseSongList.updateUserHistory(song);

        String newJson = song.getJsonString();
        Log.d(TAG, "Meta new: " + newJson);
    }

    /* ---------------------BINDER STUFF------------------------ */


    // the service is now a started service so these remain unused.
    @Override
    public IBinder onBind(Intent intent) {
        return iBinder;
    }

    public class LocalBinder extends Binder {
        public MusicPlayerService getService() {
            return MusicPlayerService.this;
        }
    }

    /* ------------------------------------Vibe mode download---------------------------*/

    /**
     * Queue the downloading of non-local songs.
     * @param inList incomming firebase list
     */
    private void queueDownloads(ArrayList<Integer> inList) {
        for(int i: inList) {
            // this long if says "a song is not locally available"
            Song s = FirebaseSongList.getSongs().get(i);
            if (SongList.getSongs().indexOf(s) == -1) {
                Intent intent = new Intent(MusicPlayerService.this, VibeModeDownloadingIntentService.class);
                Log.d(TAG, s.getTitle() + "'s URL passing to intent service: " + s.getUrl());
                intent.putExtra(VibeModeDownloadingIntentService.AUTO_DOWNLOAD_REQ_URL, s.getUrl());
                startService(intent);
            }
        }
    }




}
