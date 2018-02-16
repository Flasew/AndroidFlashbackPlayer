package edu.ucsd.team6flashbackplayer;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.List;

/**
 * Created by frankwang on 2/8/18.
 */

public class SongEntryAdapter extends BaseAdapter {

    private List<Song> songList;
    private LayoutInflater songInf;
    private static final String TAG = "SongEntryAdapter";

    public SongEntryAdapter(Context c, List<Song> theSongs){
        songList=theSongs;
        songInf=LayoutInflater.from(c);
    }

    @Override
    public int getCount() {
        return songList.size();
    }

    @Override
    public Object getItem(int position) {
        return songList.get(position);
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return 0;
    }

    static class ViewHolder {
        TextView title;
        PreferenceButtons buttons;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        //map to song layout

        Log.d(TAG, "Position: " + position);
        //Here, position is the index in the list, the convertView is the view to be
        //recycled (or created), and parent is the ListView itself.

        //Grab the convertView as our row of the ListView
        View row = convertView;
        Song currSong = songList.get(position);

        Log.d(TAG, "Song status: Title: " + currSong.getTitle() + ",Like = " + currSong.isLiked() + ", dislike = " + currSong.isDisliked());

        //If the row is null, it means that we aren't recycling anything - so we have
        //to inflate the layout ourselves.

        if(row == null) {


            final ViewHolder holder = new ViewHolder();

            row = songInf.inflate(R.layout.name_pref_entry, parent, false);
            //Now create the ViewHolder
            //and set its textView field to the proper value

            holder.title = row.findViewById(R.id.song_name);
            holder.title.setSelected(true);
            holder.title.setText(currSong.getTitle());
            holder.buttons = new PreferenceButtons(currSong,
                    (ImageButton)row.findViewById(R.id.like_button),
                    (ImageButton)row.findViewById(R.id.dislike_button)
            );
            holder.buttons.setButtonListeners();
            holder.buttons.redrawButtons();

            row.setTag(holder);
        } else {
            //We've already seen this one before!
            ViewHolder holder = (ViewHolder) row.getTag();
            holder.title.setSelected(true);
            holder.title.setText(currSong.getTitle());
            holder.buttons.setSong(currSong);
            holder.buttons.redrawButtons();
        }

        return row;


    }
}
