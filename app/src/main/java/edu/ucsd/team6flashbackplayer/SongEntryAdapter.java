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

/**
 * class SongEntryAdapter
 * Adapter class for en entry of a song in the song page. Consist a song name and two buttons
 * of like and dislike.
 */
public class SongEntryAdapter extends BaseAdapter {

    private static final String TAG = "SongEntryAdapter";

    private List<Song> songList;        // list of songs of the song lage
    private LayoutInflater songInf;     // layout inflater
    private Context context; // Context for like/dislike buttons

    public SongEntryAdapter(Context c, List<Song> theSongs) {
        songList = theSongs;
        songInf = LayoutInflater.from(c);
        context = c;
        PreferenceButtons.setLocalBroadcastManager(c);
    }

    /**
     * Get the number of entries of this page
     * @return number of entries
     */
    @Override
    public int getCount() {
        return songList.size();
    }

    /**
     * Get a song at the location position
     * @param position position of the entry
     * @return a song object corresponding to the location.
     */
    @Override
    public Object getItem(int position) {
        return songList.get(position);
    }

    /**
     * Unused
     * @param position
     * @return
     */
    @Override
    public long getItemId(int position) {
        return 0;
    }

    /**
     * ViewHolder of a row of elements. Consist a Textview of song title and a
     * pref. button group.
     */
    static class ViewHolder {
        TextView title;
        TextView albumArtist;
        PreferenceButtons buttons;
    }
    /**
     * Get the view of a row. If not initialized, initizlize the UI
     * @param position position of the row to be get
     * @param convertView
     * @param parent
     * @return the row.
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        //map to song layout

        Log.d(TAG, "Position: " + position);
        //Here, position is the index in the list, the convertView is the view to be
        //recycled (or created), and parent is the ListView itself.

        //Grab the convertView as our row of the ListView
        View row = convertView;
        Song currSong = songList.get(position);

        Log.d(TAG, "Song status: Title: " + currSong.getTitle() + ", like = " + currSong.isLiked() + ", dislike = " + currSong.isDisliked());

        //If the row is null, it means that we aren't recycling anything - so we have
        //to inflate the layout ourselves.

        if(row == null) {


            final ViewHolder holder = new ViewHolder();

            row = songInf.inflate(R.layout.name_pref_entry, parent, false);
            //Now create the ViewHolder
            //and set its textView field to the proper value

            holder.title = row.findViewById(R.id.song_name);
            holder.albumArtist = row.findViewById(R.id.song_attrs);

            holder.title.setSelected(true);
            holder.title.setText(currSong.getTitle());

            holder.albumArtist.setText(context.getResources().getString(R.string.artist_albums, currSong.getArtist(), currSong.getAlbum()));

            holder.buttons = new PreferenceButtons(currSong,
                    (ImageButton)row.findViewById(R.id.like_button),
                    (ImageButton)row.findViewById(R.id.dislike_button),
                    context
            );
            holder.buttons.setButtonListeners();
            holder.buttons.redrawButtons();


            row.setTag(holder);
        } else {
            //We've already seen this one before!
            ViewHolder holder = (ViewHolder) row.getTag();
            holder.title.setSelected(true);
            holder.title.setText(currSong.getTitle());
            holder.albumArtist.setText(context.getResources().getString(R.string.artist_albums, currSong.getArtist(), currSong.getAlbum()));
            holder.buttons.setSong(currSong);
            holder.buttons.redrawButtons();
        }

        return row;


    }
}
