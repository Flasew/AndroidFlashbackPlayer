package edu.ucsd.team6flashbackplayer;

import android.app.ListActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

/**
 * Flashback play list.
 */
public class FBPlayListActivity extends AppCompatActivity {

    public static final String FB_POS_LIST = "FBPosList";
    private TextEntryAdapter<Song> songAdapter;
    private ListView listView;

    /**
     * Set the list view.
     * Intent passed in should be the position list (array list)
     *
     * TODO: Register flashback list change listener
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
                for (int pos: positionList)
                    songList.add(SongList.getSongs().get(pos));

        }
        // update the adapter view
        songAdapter = new TextEntryAdapter<>(this, songList);
        listView = findViewById(R.id.fb_list);
        listView.setAdapter(songAdapter);;
    }
}
