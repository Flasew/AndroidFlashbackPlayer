package edu.ucsd.team6flashbackplayer;

import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class CurrSongActivity extends AppCompatActivity {

    private boolean flashBackMode;
    private static final String TAG = "CurrSongActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_curr_song);

        final SharedPreferences sp = getSharedPreferences("mode", MODE_PRIVATE);
        final SharedPreferences.Editor editor = sp.edit();
        flashBackMode = sp.getBoolean("mode", false);
        final Button flashBackButton = findViewById(R.id.fb_button);

        Log.v(TAG, "fbmode on enter: " + flashBackMode);

        if (flashBackMode) {
            flashBackButton.setBackground(getDrawable(R.drawable.fb_enabled));
        }
        else {
            flashBackButton.setBackground(getDrawable(R.drawable.fb_disabled));
        }

        flashBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.v(TAG, "fbmode before pressing: " + flashBackMode);
                flashBackMode = !flashBackMode;
                flashBackButton.setBackground((flashBackMode)?
                        getDrawable(R.drawable.fb_enabled):
                        getDrawable(R.drawable.fb_disabled));
                editor.putBoolean("mode", flashBackMode);
                editor.apply();
            }
        });
        // retrieve information from player
    }

    @Override
    public void onBackPressed() {
        if (!flashBackMode) {
            super.onBackPressed();
        }
    }
}
