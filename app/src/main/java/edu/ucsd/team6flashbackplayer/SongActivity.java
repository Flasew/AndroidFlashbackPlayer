package edu.ucsd.team6flashbackplayer;

import android.app.DialogFragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.DownloadListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import java.util.List;


/**
 * class SongActivity.
 * This class corresponds to the song page in the application.
 * Consist of a list view of song entries.
 * It can also be invoked from an album activity, in which it will
 * display a list of songs in this album
 */
public class SongActivity extends MusicPlayerNavigateActivity implements DownloadDialogFragment.DownloadDialogListener {

    protected final String TAG = "SongActivity";

    static final String SINGLE_SONG_INTENT = "SingleSongIntent";
    private List<Song> songList;
    private ListView songView;
    private SongEntryAdapter songAdapter;
    private WebMusicDownloader downloader;

    /**
     * On create will know if it's displaying a list of all songs or an album's songs from
     * the intent passed in.
     * @param savedInstanceState unused
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_song);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

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

        downloader = new WebMusicDownloader(
                new NormalModeDownloadedFileHandlerDecorator(
                        new DownloadedSongHandler(this)
                )
        );

    }

    /**
     * Create the menu of the app
     * @param menu menu object
     * @return ignored
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_song, menu);
        return true;
    }

    /**
     * Handles menu item click. In this case both are for download.
     * @param item item clicked
     * @return result of handle
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //
        if (id == R.id.sort_holder) {
            return true;
        }

        else if (id == R.id.add_song) {
            DialogFragment downloadDialog = new DownloadDialogFragment();
            downloadDialog.show(getFragmentManager(), getResources().getString(R.string.download_song));
        }

        return super.onOptionsItemSelected(item);
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

    /**
     * Song activity should update the list of songs when files are downloaded
     */
    protected void onFileDownloaded() {
        songAdapter.notifyDataSetChanged();
        Log.d(TAG, "List updated after file download.w");
    }

    /**
     * Listener of user clicking download button in dialog
     * @param dialog dialog fragment
     */
    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {
        EditText urlField = dialog.getDialog().findViewById(R.id.download_url);
        String url = urlField.getText().toString();
        downloader.downloadFromUrl(url);
        dialog.dismiss();
    }

    /**
     * Listener of user clicking cancel button in dialog
     * @param dialog dialog fragment
     */
    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {
        dialog.dismiss();
    }
}
