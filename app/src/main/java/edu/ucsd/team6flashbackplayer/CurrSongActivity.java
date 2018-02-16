package edu.ucsd.team6flashbackplayer;

import android.content.SharedPreferences;
import android.icu.text.DateTimePatternGenerator;
import android.location.Address;
import android.location.Geocoder;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;

import org.w3c.dom.Text;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class CurrSongActivity
        extends MusicPlayerActivity {

    private boolean flashBackMode;
    private TextView timeClockView;
    private TextView timeDateView;
    private TextView locationTextView;
    private TextView songTitleView;
    private TextView songArtistView;
    private PreferenceButtons buttons;
    private static final String TAG = "CurrSongActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_curr_song);

        // UI elements
        timeClockView = findViewById(R.id.time_clock_txt);
        timeDateView = findViewById(R.id.time_date_txt);
        locationTextView = findViewById(R.id.location_txt);
        songTitleView = findViewById(R.id.song_name);
        songArtistView = findViewById(R.id.song_artist);
        buttons = new PreferenceButtons(
                (ImageButton) findViewById(R.id.like_button),
                (ImageButton) findViewById(R.id.dislike_button)
        );
        buttons.redrawButtons();

        final SharedPreferences.Editor editor = fbModeSharedPreferences.edit();
        flashBackMode = fbModeSharedPreferences.getBoolean("mode", false);
        final Button flashBackButton = findViewById(R.id.fb_button);

        Log.d(TAG(), "fbmode on enter: " + flashBackMode);

        if (flashBackMode) {
            flashBackButton.setBackground(getDrawable(R.drawable.fb_enabled));
        }
        else {
            flashBackButton.setBackground(getDrawable(R.drawable.fb_disabled));
        }

        flashBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG(), "fbmode before pressing: " + flashBackMode);
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

    // update the page's content on song playing update
    @Override
    protected void onSongUpdate(int position) {

        final Song currSong = SongList.getSongs().get(position);

        songTitleView.setText(currSong.getTitle());
        songArtistView.setText(currSong.getArtist());
        locationTextView.setText(getStrAddress(currSong.getLatestLoc()));
        timeDateView.setText(getStrDate(currSong.getLatestTime()));
        timeClockView.setText(getStrClock(currSong.getLatestTime()));
        buttons.setSong(currSong);
        buttons.setButtonListeners();
        buttons.redrawButtons();
    }

    @Override
    protected void onSongFinish() {

        songTitleView.setText(NO_INFO);
        songArtistView.setText(NO_INFO);
        locationTextView.setText(NO_INFO);
        timeDateView.setText(NO_INFO);
        timeClockView.setText(NO_INFO);
        buttons.setSong(null);
        buttons.removeButtonListeners();
        buttons.redrawButtons();

    }

    // get the string location from a longitude and latitude
    private String getStrAddress(LatLng ll) {
        if (ll == null)
            return NO_INFO;

        double lng = ll.longitude;
        double lat = ll.latitude;

        if (lat != 0 && lng != 0) {
            try {
                Geocoder geocoder = new Geocoder(this);
                List<Address> addresses = geocoder.getFromLocation(lat, lng, 1);
                String address = addresses.get(0).getAddressLine(0);
                String city = addresses.get(0).getAddressLine(1);
                String country = addresses.get(0).getAddressLine(2);
                Log.d(TAG(), "address = " + address + ", city = " + city + ", country = " + country);
                return address + ", " + city;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return NO_INFO;
    }

    // get a date in format of "DD-MM-YYYY, DayOfWeek" or "---" if no info is available
    private String getStrDate(ZonedDateTime zdt) {
        if (zdt == null)
            return NO_INFO;

        return zdt.format(
                DateTimeFormatter.ofPattern("dd-MM-yyyy, EEEE")
        );
    }

    // get a time in the format of "HH:MM AM/PM" or "---" if no info is available
    private String getStrClock(ZonedDateTime zdt) {
        if (zdt == null)
            return NO_INFO;

        return zdt.format(
                DateTimeFormatter.ofPattern("hh:mm a")
        );
    }


}
