package edu.ucsd.team6flashbackplayer;

import android.content.Context;
import android.graphics.Color;
import android.os.IBinder;
import android.support.constraint.ConstraintLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by frankwang on 2/8/18.
 */

public class SongAdapter extends BaseAdapter {

    private List<Song> songList;
    private LayoutInflater songInf;

    public SongAdapter(Context c, List<Song> theSongs){
        songList=theSongs;
        songInf=LayoutInflater.from(c);
    }

    @Override
    public int getCount() {
        return songList.size();
    }

    @Override
    public Object getItem(int arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public long getItemId(int arg0) {
        // TODO Auto-generated method stub
        return 0;
    }

    static class ViewHolder {
        TextView title;
        ImageButton likeButton;
        ImageButton dislikeButton;
        int position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        //map to song layout

        // XXX: perhaps do this later...
//        //Here, position is the index in the list, the convertView is the view to be
//        //recycled (or created), and parent is the ListView itself.
//
//        //Grab the convertView as our row of the ListView
//        View row = convertView;
//
//        //If the row is null, it means that we aren't recycling anything - so we have
//        //to inflate the layout ourselves.
//        ViewHolder holder = null;
//        if(row == null) {
//            LayoutInflater inflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//            row = inflater.inflate(R.layout.list_item, parent, false);
//            //Now create the ViewHolder
//            holder = new ViewHolder();
//            //and set its textView field to the proper value
//            holder.textView =  (TextView) row.findViewById(R.id.listItemTextView);
//            //and store it as the 'tag' of our view
//            row.setTag(holder);
//        } else {
//            //We've already seen this one before!
//            holder = (ViewHolder) row.getTag();
//        }
//
//        //Grab the item to be rendered. In this case, I'm just using a string, but
//        //you will use your underlying object type.
//        final String item = getItem(position);
//
//        //And update the ViewHolder for this View's text to the correct text.
//        holder.textView.setText(item);
//
//        //and return the row
//        return row;



        ConstraintLayout songLay = (ConstraintLayout) songInf.inflate
                (R.layout.song_entry, parent, false);
        //get title and artist views
        final TextView songView = (TextView)songLay.findViewById(R.id.song_name);
        songView.setSelected(true);
        final ImageButton likeView = (ImageButton)songLay.findViewById(R.id.like_button);
        final ImageButton dislikeView = (ImageButton)songLay.findViewById(R.id.dislike_button);
        //get song using position
        final Song currSong = songList.get(position);
        //get title and artist strings
        songView.setText(currSong.get_name());
        likeView.setBackgroundColor(currSong.is_liked()? Color.GREEN : Color.GRAY);
        dislikeView.setBackgroundColor(currSong.is_disliked()? Color.RED : Color.GRAY);

        likeView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currSong.like();
                likeView.setBackgroundColor(currSong.is_liked()? Color.GREEN : Color.GRAY);
                dislikeView.setBackgroundColor(currSong.is_disliked()? Color.RED : Color.GRAY);
            }
        });

        dislikeView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currSong.dislike();
                likeView.setBackgroundColor(currSong.is_liked()? Color.GREEN : Color.GRAY);
                dislikeView.setBackgroundColor(currSong.is_disliked()? Color.RED : Color.GRAY);
            }
        });
        //set position as tag
        songLay.setTag(position);
        return songLay;
    }
}
