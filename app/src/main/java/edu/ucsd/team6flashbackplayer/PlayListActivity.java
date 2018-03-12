package edu.ucsd.team6flashbackplayer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

/**
 * Class FBPlaylistActivity
 * This class correspond to the the Flashback mode play list page, which is
 * avaliable when the user is in the flashback mode.
 */
public class PlayListActivity extends AppCompatActivity {

    private static final String TAG = PlayListActivity.class.getName();

    private LocalBroadcastManager localBroadcastManager;
    private TextEntryAdapter<Song> songAdapter;
    private ListView listView;
    private ArrayList<Song> songList = new ArrayList<>();

    private BroadcastReceiver listUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            ArrayList<Integer> playlist = intent.getIntegerArrayListExtra(MusicPlayerService.BROADCAST_SONG_LIST);
            Log.d(TAG, "Received new playlist, is null: " + (playlist == null));
            if (playlist != null)
                for(int i: playlist) {
                    Log.d(TAG, "Playlist has item " + i);
                }
            updateListUI(playlist);
        }
    };

    private void updateListUI(ArrayList<Integer> playlist) {

        songList.clear();

        if (playlist != null) {
            for (int pos : playlist) {
                Song currSong = SongList.getSongs().get(pos);
                if (!currSong.isDisliked())
                    songList.add(currSong);
            }
        }

        songAdapter.notifyDataSetChanged();

    }

    /**
     * Set the list view.
     * Intent passed in should be the position list (array list)
     * There's really not too much worth logging here...
     * @param savedInstanceState saved instance state on last entry
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fbplay_list);
        setTitle(R.string.fbList_activity_title);

        localBroadcastManager = LocalBroadcastManager.getInstance(this);

        songAdapter = new TextEntryAdapter<>(this, songList);
        listView = findViewById(R.id.fb_list);
        listView.setAdapter(songAdapter);

    }

    @Override
    public void onStart() {
        super.onStart();
        localBroadcastManager.registerReceiver(listUpdateReceiver,
                new IntentFilter(MusicPlayerService.BROADCAST_SONG_LIST));
        broadcastPlaylistRequest();
    }

    @Override
    public void onStop() {
        localBroadcastManager.unregisterReceiver(listUpdateReceiver);
        super.onStop();
    }

    private void broadcastPlaylistRequest() {
        Intent intent = new Intent(CurrSongActivity.PLAYLIST_REQUEST);
        localBroadcastManager.sendBroadcast(intent);
        Log.d(TAG, "Broadcast playlist request");
    }
}
