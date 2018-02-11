package edu.ucsd.team6flashbackplayer;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.LogPrinter;
import android.view.GestureDetector;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SongActivity extends AppCompatActivity {


    private static final String TAG = "SongActivity";
    private List<Song> songList;
    private ListView songView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_song);

        String albumName;
        Bundle extras = getIntent().getExtras();
        if(extras == null) {
            albumName = null;
            SongList sl = new SongList();
            songList = sl.getSongs();
        } else {
            albumName = extras.getString("albumName");
            AlbumList al = new AlbumList();
            songList = al.getAlbums().get(albumName).getSongs();
        }

        songView = findViewById(R.id.song_list);

        SongAdapter songAdt = new SongAdapter(this, songList);
        songView.setAdapter(songAdt);

    }

    @Override
    public void onResume() {
        // this refreshes the like/dislike status.
        // a little bit of overkill though...
        super.onResume();
        songView = findViewById(R.id.song_list);

        SongAdapter songAdt = new SongAdapter(this, songList);
        songView.setAdapter(songAdt);
    }

}
