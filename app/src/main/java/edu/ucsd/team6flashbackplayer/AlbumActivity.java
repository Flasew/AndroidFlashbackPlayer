package edu.ucsd.team6flashbackplayer;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import java.util.List;

public class AlbumActivity extends AppCompatActivity {

    private final String TAG = "AlbumActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album);

        final ListView albumView = findViewById(R.id.album_list);

        AlbumAdapter albumAdt = new AlbumAdapter(this, AlbumList.getAlbums());
        albumView.setAdapter(albumAdt);
        albumView.setItemsCanFocus(false);
        albumView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.v(TAG, "On click listener.");
                Album listItem = (Album)albumView.getItemAtPosition(position);
                Log.v(TAG, "Album" + listItem.getName());
                startSongActivity(listItem);
            }
        });
<<<<<<< HEAD

        ConstraintLayout currSong = findViewById(R.id.current_song);
        currSong.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startCurrSongActivity();
            }
        });

        final SharedPreferences sp = getSharedPreferences("mode", MODE_PRIVATE);
        final SharedPreferences.Editor editor = sp.edit();
        Button flashBackButton = findViewById(R.id.fb_button);
        flashBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editor.putBoolean("mode" , true);
                editor.apply();
                startCurrSongActivity();
            }
        });
=======
>>>>>>> origin/milestone_1
    }

    private void startSongActivity(Album album) {
        Intent intent = new Intent(this, SongActivity.class);
        intent.putExtra("albumName", album.getName());
        startActivity(intent);
    }

<<<<<<< HEAD
    public void startCurrSongActivity() {
        Intent intent = new Intent(this, CurrSongActivity.class);
        startActivity(intent);
    }

=======
>>>>>>> origin/milestone_1
}
