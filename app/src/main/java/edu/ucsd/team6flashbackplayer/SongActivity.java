package edu.ucsd.team6flashbackplayer;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import java.util.List;

public class SongActivity extends MusicPlayerNavigateActivity {

    protected final String TAG = "SongActivity";
    private List<Song> songList;
    private ListView songView;
    private SongEntryAdapter songAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_song);

        currSong = findViewById(R.id.current_song);
        currSong.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startCurrSongActivity();
            }
        });

        String albumName;
        Bundle extras = getIntent().getExtras();
        if(extras == null) {
            albumName = null;
            songList = SongList.getSongs();
            // set title of this activity
            setTitle(R.string.song_activity_title);
        } else {
            albumName = extras.getString("albumName");
            songList = AlbumList.getAlbum(albumName).getSongs();
            // set title of this activity
            setTitle(albumName);
        }

        songView = findViewById(R.id.song_list);

        songAdapter = new SongEntryAdapter(this, songList);

        songView.setAdapter(songAdapter);
        songView.setItemsCanFocus(false);
        songView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Song listItem = (Song)songView.getItemAtPosition(position);
                Log.d(TAG, "Song: " + listItem.getTitle());
                play(listItem);
            }
        });

        final SharedPreferences.Editor editor = fbModeSharedPreferences.edit();
        Button flashBackButton = findViewById(R.id.fb_button);
        flashBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editor.putBoolean("mode" , true);
                editor.apply();
                startCurrSongActivity();
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        songAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    private void play(Song song) {
        PositionPlayList ppl = new PositionPlayList(song);
        Intent playerIntent = new Intent(this, MusicPlayerService.class);
        playerIntent.putIntegerArrayListExtra(PositionPlayList.POS_LIST_INTENT, ppl.getPositionList());
        playerIntent.putExtra(MusicPlayerActivity.START_MUSICSERVICE_KEEP_CURRPLAY, false);
        startService(playerIntent);
    }

    @Override
    public void onResume() {
        // this refreshes the like/dislike status.
        // a little bit of overkill though...
        super.onResume();
        songAdapter.notifyDataSetChanged();
        Log.d(TAG, "On resume of song activity called.");
    }
}
