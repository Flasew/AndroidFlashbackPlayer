package edu.ucsd.team6flashbackplayer;

import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    private MediaPlayer mediaPlayer;
    private static final int MEDIA_RES_ID = R.raw.everything_i_love;


    Class raw = R.raw.class;
    Field[] fields = raw.getFields();
    for (Field fields : fields) {
        try {
            Log.i("REFLECTION", String.format("%s is %d",field.getName(),field.getInt(null)));
        } catch(IllegalAccessException e) {
            Log.e.("REFLECTION", String.format("%s threw IllegalAccessException.",
                    field.getName()));
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        loadMedia(MEDIA_RES_ID);
        Button playButton = (Button) findViewById(R.id.button_play);
        playButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mediaPlayer.start();
                    }
                });
    }

    public void loadMedia(int resourceId) {
        if (mediaPlayer == null) {
            mediaPlayer = new MediaPlayer();
        }
        AssetFileDescriptor assetFileDescriptor = this.getResources().openRawResourceFd(resourceId);
        try {
            mediaPlayer.setDataSource(assetFileDescriptor);
            mediaPlayer.prepareAsync();
        } catch (Exception e) {
            System.out.println(e.toString());
        }

    }
}
