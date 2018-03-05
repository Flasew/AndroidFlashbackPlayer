package edu.ucsd.team6flashbackplayer;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;


/**
 * class Album activity.
 * This class corresponds to the album page in the application.
 * Consist of a list view of album entries.
 */
public class AlbumActivity extends MusicPlayerNavigateActivity {

    protected final String TAG = "AlbumActivity";   // debug tag

    /**
     * On create of album activity. Initialize the UI and listeners of the album activity.
     * @param savedInstanceState savedInstanceState from previous runs
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album);

        // set title of this activity
        setTitle(R.string.album_activity_title);

        currSong = findViewById(R.id.current_song);
        currSong.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startCurrSongActivity();
            }
        });

        resetSongStatusBar();

        final ListView albumView = findViewById(R.id.album_list);

        // populate the listview
        TextEntryAdapter<Album> albumAdt = new TextEntryAdapter<Album>(this, AlbumList.getAlbums() );
        albumView.setAdapter(albumAdt);
        albumView.setItemsCanFocus(false);
        albumView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "On click listener.");
                Album listItem = (Album)albumView.getItemAtPosition(position);
                Log.d(TAG, "Album" + listItem.getName());
                play(listItem);
                startSongActivity(listItem);
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
     * Play an album by construct a PositionPlayListFactory for this album and pass it to music service.
     * @param album album to be played
     */
    private void play(Album album) {
        Log.d(TAG, "Start playing album: " + album.getName());
        PositionPlayListFactory ppl = new PositionPlayListFactory(album);
        Intent playerIntent = new Intent(this, MusicPlayerService.class);
        playerIntent.putIntegerArrayListExtra(PositionPlayListFactory.POS_LIST_INTENT, ppl.getPositionList());
        playerIntent.putExtra(MusicPlayerActivity.START_MUSICSERVICE_KEEP_CURRPLAY, false);
        startService(playerIntent);

    }

    /**
     * Start a song activity when select an album entry. The song activity should have
     * all the songs in this album and nothing else.
     * @param album album selected.
     */
    private void startSongActivity(Album album) {
        Log.d(TAG, "Start SongActivity of album: " + album.getName());
        Intent intent = new Intent(this, SongActivity.class);
        intent.putExtra("albumName", album.getName());
        startActivity(intent);
    }

}
