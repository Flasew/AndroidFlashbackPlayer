package edu.ucsd.team6flashbackplayer;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

/**
 * Class FBPlaylistActivity
 * This class correspond to the the Flashback mode play list page, which is
 * avaliable when the user is in the flashback mode.
 */
public class FBPlayListActivity extends AppCompatActivity {

    public static final String FB_POS_LIST = "FBPosList";   // debug tag
    private TextEntryAdapter<Song> songAdapter;
    private ListView listView;

    /**
     * Set the list view.
     * Intent passed in should be the position list (array list)
     * There's really not too much worth logging here...
     * @param savedInstanceState saved instance state on last entry
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fbplay_list);
        setTitle(R.string.fbList_activity_title);

        List<Song> songList = new ArrayList<>();

        Bundle extras = getIntent().getExtras();

        // if extra is null, it's just an empty list.
        if(extras != null) {
            // retrive song info from the position list.
            List<Integer> positionList = extras.getIntegerArrayList(FB_POS_LIST);
            if (positionList != null)
                for (int pos: positionList) {
                    Song currSong = SongList.getSongs().get(pos);
                    if (!currSong.isDisliked())
                        songList.add(currSong);
                }


        }
        // update the adapter view
        songAdapter = new TextEntryAdapter<>(this, songList);
        listView = findViewById(R.id.fb_list);
        listView.setAdapter(songAdapter);;
    }
}
