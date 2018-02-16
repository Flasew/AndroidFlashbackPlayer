package edu.ucsd.team6flashbackplayer;

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

import java.util.ArrayList;
import java.util.List;

public class AlbumActivity extends MusicPlayerActivity {

    protected final String TAG = "AlbumActivity";
    private ConstraintLayout currSong;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album);

        // set title of this activity
        setTitle(R.string.album_activity_title);

        final ListView albumView = findViewById(R.id.album_list);

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

    private void play(Album album) {

        PositionPlayList ppl = new PositionPlayList(album);
        Intent playerIntent = new Intent(this, MusicPlayerService.class);
        playerIntent.putIntegerArrayListExtra("posList", ppl.getPositionList());
        startService(playerIntent);

    }

    private void startSongActivity(Album album) {
        Intent intent = new Intent(this, SongActivity.class);
        intent.putExtra("albumName", album.getName());
        startActivity(intent);
    }

}
