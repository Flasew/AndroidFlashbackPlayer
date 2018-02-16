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
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static android.media.MediaPlayer.TrackInfo.MEDIA_TRACK_TYPE_AUDIO;
import static edu.ucsd.team6flashbackplayer.MainActivity.tracker;

public class MusicPlayerService extends Service implements MediaPlayer.OnCompletionListener,
        MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener,
        MediaPlayer.OnInfoListener {

    final static String BROADCAST_SONG_CHANGE = "uiUpdate";
    private final static String TAG = "MusicPlayerService";
    private final IBinder iBinder = new LocalBinder();
    private List<Song> songs = SongList.getSongs();
    private int counter = 0;                // current song position counter
    private MediaPlayer mediaPlayer;        // media player that plays the song
    private static List<Integer> positionList;     // list of songs to be played
    private LocalBroadcastManager localBroadcastManager;
    private BroadcastReceiver songUpdateRequestReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            broadcastSongChange();
        }
    };

    private static Location curSongLoc;
    private ZonedDateTime curSongTime;

    public MusicPlayerService() {

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // get the position list indicating songs
        if (localBroadcastManager == null)
            localBroadcastManager = LocalBroadcastManager.getInstance(this);
        try {
            positionList = intent.getExtras().getIntegerArrayList("posList");
        } catch (NullPointerException e) {
            stopSelf();
        } catch (IndexOutOfBoundsException e) {
            stopSelf();
        }

        if (positionList != null && positionList.size() != 0) {
            counter = 0;
            initMediaPlayer();
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
        catch (IndexOutOfBoundsException e) {
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

    @Override
    public void onCompletion(MediaPlayer mp) {
        //Invoked when playback of a media source has completed

        // Update the most recently played song's latest location, datetime
        int trackNum = mp.getSelectedTrack(MEDIA_TRACK_TYPE_AUDIO);
        songs.get(trackNum).setLatestLoc(new LatLng(curSongLoc.getLatitude(), curSongLoc.getLongitude()));
        songs.get(trackNum).setLatestTime(curSongTime);

        stopMedia();
        counter += 1;
        if (counter < positionList.size()) {
            prepSongAsync();
        }
        else {
            stopSelf();
            broadcastSongChange();
        }
    }

    @Override
    public boolean onInfo(MediaPlayer mp, int what, int extra) {
        //Invoked to communicate some info.
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        playMedia();

        // Get Android's current location
        curSongLoc = tracker.getCurrentLocation();
        // Get current datetime
        curSongTime = ZonedDateTime.now();
        Log.d("log", Double.toString(curSongLoc.getLatitude()));
        /*SharedPreferences sp = getSharedPreferences("metadata", MODE_PRIVATE);
        int trackNum = mp.getSelectedTrack(MEDIA_TRACK_TYPE_AUDIO);
        String a = sp.getString(songs.get(trackNum).getId(),null);
        Log.d("meta", a);*/
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
