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
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;


/**
 * class Album activity.
 * This class corresponds to the album page in the application.
 * Consist of a list view of album entries.
 */
public class AlbumActivity extends MusicPlayerNavigateActivity implements DownloadDialogFragment.DownloadDialogListener {

    protected final String TAG = "AlbumActivity";   // debug tag

    private WebMusicDownloader downloader;
    private TextEntryAdapter<Album> albumAdt;

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

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        currSong = findViewById(R.id.current_song);
        currSong.setOnClickListener(v -> startCurrSongActivity());

        setControlButtonsUI();

        final ListView albumView = findViewById(R.id.album_list);

        // populate the listview
        albumAdt = new TextEntryAdapter<Album>(this, AlbumList.getAlbums() );
        albumView.setAdapter(albumAdt);
        albumView.setItemsCanFocus(false);
        albumView.setOnItemClickListener((parent, view, position, id) -> {
                Log.d(TAG, "On click listener.");
                Album listItem = (Album)albumView.getItemAtPosition(position);
                Log.d(TAG, "Album" + listItem.getName());
                play(listItem);
                startSongActivity(listItem);
        });

        final SharedPreferences.Editor editor = fbModeSharedPreferences.edit();
        Button flashBackButton = findViewById(R.id.fb_button);
        flashBackButton.setOnClickListener(v -> {
                editor.putBoolean("mode" , true);
                editor.apply();
                startCurrSongActivity();
        });

        downloader = new WebMusicDownloader(
                new NormalModeDownloadedFileHandlerDecorator(
                        new DownloadedAlbumHandler(this)
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
        getMenuInflater().inflate(R.menu.menu_album, menu);
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

        if (id == R.id.add_album) {
            DialogFragment downloadDialog = new DownloadDialogFragment();
            downloadDialog.show(getFragmentManager(), getResources().getString(R.string.download_album));
        }

        return super.onOptionsItemSelected(item);
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

    /**
     * Update the UI
     */
    @Override
    protected void onFileDownloaded() {
        albumAdt.setItems(AlbumList.getAlbums());
        albumAdt.notifyDataSetChanged();
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
