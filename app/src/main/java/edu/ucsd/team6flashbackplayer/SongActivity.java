package edu.ucsd.team6flashbackplayer;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.constraint.ConstraintLayout;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;

public class SongActivity extends MusicPlayerActivity {

    protected final String TAG = "SongActivity";
    private List<Song> songList;
    private ListView songView;
    private SongEntryAdapter songAdapter;
    private ConstraintLayout currSong;
    private static Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SongActivity.context = getApplicationContext();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_song);

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

        songAdapter = new SongEntryAdapter(this, songList, context);

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

        currSong = findViewById(R.id.current_song);
        currSong.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startCurrSongActivity();
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

    @Override
    protected void onSongUpdate(int position) {
        TextView currPlayingName = currSong.findViewById(R.id.curr_playing_name);
        TextView currPlayingArtist = currSong.findViewById(R.id.curr_playing_artist);
        Song currSong = SongList.getSongs().get(position);
        String title = currSong.getTitle();
        String artist = currSong.getArtist();
        currPlayingName.setText((title == null) ? "---" : title);
        currPlayingArtist.setText((artist == null) ? "---" : artist);
    }

    @Override
    protected void onSongFinish() {
        TextView currPlayingName = currSong.findViewById(R.id.curr_playing_name);
        TextView currPlayingArtist = currSong.findViewById(R.id.curr_playing_artist);
        currPlayingName.setText(NO_INFO);
        currPlayingArtist.setText(NO_INFO);
    }


    private void play(Song song) {
        PositionPlayList ppl = new PositionPlayList(song);
        Intent playerIntent = new Intent(this, MusicPlayerService.class);
        playerIntent.putIntegerArrayListExtra("posList", ppl.getPositionList());
        startService(playerIntent);
    }

    @Override
    public void onResume() {
        // this refreshes the like/dislike status.
        // a little bit of overkill though...
        super.onResume();
        songAdapter.notifyDataSetChanged();
        Log.d(TAG(), "On resume of song activity called.");
    }
}
