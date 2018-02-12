package edu.ucsd.team6flashbackplayer;

import android.app.Service;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.io.IOException;
import java.util.List;

public class MusicPlayerService extends Service implements MediaPlayer.OnCompletionListener,
        MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener,
        MediaPlayer.OnInfoListener {

    final static String BROADCAST_UI_UPDATE = "uiUpdate";
    private final static String TAG = "MusicPlayerService";
    private final IBinder iBinder = new LocalBinder();
    private List<Song> songs = SongList.getSongs();
    private int counter = 0;
    private MediaPlayer mediaPlayer;        // media player that plays the song
    private List<Integer> positionList;     // list of songs to be played
    private LocalBroadcastManager broadcastManager;

    public MusicPlayerService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // get the position list indicating songs

        try {
            positionList = intent.getExtras().getIntegerArrayList("posList");
        } catch (NullPointerException e) {
            stopSelf();
        }

        if (positionList != null && positionList.size() != 0) {
            counter = 0;
            broadcastManager = LocalBroadcastManager.getInstance(this);
            initMediaPlayer();
        }
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

    public void updateUI() {
        Intent intent = new Intent(BROADCAST_UI_UPDATE);
        intent.putExtra(BROADCAST_UI_UPDATE, positionList.get(counter));
        broadcastManager.sendBroadcast(intent);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return iBinder;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        //Invoked when playback of a media source has completed
        stopMedia();
        counter += 1;
        if (counter < positionList.size()) {
            prepSongAsync();
        }
        else {
            stopSelf();
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
        // update UI by broadcast
        updateUI();
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
