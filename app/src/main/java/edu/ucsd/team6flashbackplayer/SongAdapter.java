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
        ImageButton likeButton;
        ImageButton dislikeButton;
        int position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        //map to song layout

        //Here, position is the index in the list, the convertView is the view to be
        //recycled (or created), and parent is the ListView itself.

        //Grab the convertView as our row of the ListView
        View row = convertView;
        final Song currSong = songList.get(position);

        //If the row is null, it means that we aren't recycling anything - so we have
        //to inflate the layout ourselves.

        if(row == null) {


            final ViewHolder holder = new ViewHolder();

            row = songInf.inflate(R.layout.song_entry, parent, false);
            //Now create the ViewHolder
            //and set its textView field to the proper value

            holder.title =  (TextView)row.findViewById(R.id.song_name);
            holder.title.setSelected(true);
            holder.title.setText(currSong.getTitle());
            holder.likeButton = (ImageButton)row.findViewById(R.id.like_button);
            holder.dislikeButton = (ImageButton)row.findViewById(R.id.dislike_button);
            holder.likeButton.setBackgroundColor(currSong.isLiked()? Color.GREEN : Color.GRAY);
            holder.dislikeButton.setBackgroundColor(currSong.isDisliked()? Color.RED : Color.GRAY);

            //and store it as the 'tag' of our view
            holder.likeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SongPreference sp = new SongPreference();
                    sp.like(currSong);
                    holder.likeButton.setBackgroundColor(currSong.isLiked()? Color.GREEN : Color.GRAY);
                    holder.dislikeButton.setBackgroundColor(currSong.isDisliked()? Color.RED : Color.GRAY);
                }
            });

            holder.dislikeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SongPreference sp = new SongPreference();
                    sp.dislike(currSong);
                    holder.likeButton.setBackgroundColor(currSong.isLiked()? Color.GREEN : Color.GRAY);
                    holder.dislikeButton.setBackgroundColor(currSong.isDisliked()? Color.RED : Color.GRAY);
                }
            });
            row.setTag(holder);
        } else {
            //We've already seen this one before!
            ViewHolder holder = (ViewHolder) row.getTag();
            holder.title.setSelected(true);
            holder.title.setText(currSong.getTitle());
            holder.likeButton.setBackgroundColor(currSong.isLiked()? Color.GREEN : Color.GRAY);
            holder.dislikeButton.setBackgroundColor(currSong.isDisliked()? Color.RED : Color.GRAY);
        }

        return row;


    }
}
