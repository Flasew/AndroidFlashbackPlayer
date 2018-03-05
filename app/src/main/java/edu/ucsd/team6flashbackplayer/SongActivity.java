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


/**
 * class SongActivity.
 * This class corresponds to the song page in the application.
 * Consist of a list view of song entries.
 * It can also be invoked from an album activity, in which it will
 * display a list of songs in this album
 */
public class SongActivity extends MusicPlayerNavigateActivity {

    protected final String TAG = "SongActivity";

    static final String SINGLE_SONG_INTENT = "SingleSongIntent";
    private List<Song> songList;
    private ListView songView;
    private SongEntryAdapter songAdapter;

    /**
     * On create will know if it's displaying a list of all songs or an album's songs from
     * the intent passed in.
     * @param savedInstanceState unused
     */
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

        // check if it's from an album or should display the global list.
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

        // setup the UI
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



    /**
     * Play a song. Called when user select a song on the page.
     * This method will pass the song as a single integer, so the MPService can know it's a
     * single song and will still play it even if it's disliked.
     * @param song song to be played.
     */
    private void play(Song song) {
        Log.d(TAG, "Start playing song: " + song.getTitle());
        PositionPlayListFactory ppl = new PositionPlayListFactory(song);
        Intent playerIntent = new Intent(this, MusicPlayerService.class);
        playerIntent.putExtra(SINGLE_SONG_INTENT, ppl.getPositionList().get(0));

        playerIntent.putIntegerArrayListExtra(PositionPlayListFactory.POS_LIST_INTENT, null);
        playerIntent.putExtra(MusicPlayerActivity.START_MUSICSERVICE_KEEP_CURRPLAY, false);
        startService(playerIntent);
    }

    /**
     * Refresh the like/dislike status on resume
     */
    @Override
    public void onResume() {
        // this refreshes the like/dislike status.
        // a little bit of overkill though...
        super.onResume();
        songAdapter.notifyDataSetChanged();
        Log.d(TAG, "On resume of song activity called.");
    }
}
