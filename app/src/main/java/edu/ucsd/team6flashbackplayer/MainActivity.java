package edu.ucsd.team6flashbackplayer;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static MediaPlayer media_player;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button songButton = findViewById(R.id.main_songs);
        songButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startSongActivity();
            }
        });
    }

    public void startSongActivity() {
        Intent intent = new Intent(this, SongActivity.class);
        startActivity(intent);
    }


    public void loadMedia(int resourceId) {
        if (media_player == null) {
            media_player = new MediaPlayer();
        }
        AssetFileDescriptor assetFileDescriptor = this.getResources().openRawResourceFd(resourceId);
        try {
            media_player.setDataSource(assetFileDescriptor);
            media_player.prepareAsync();
        } catch (Exception e) {
            System.out.println(e.toString());
        }

    }
}
