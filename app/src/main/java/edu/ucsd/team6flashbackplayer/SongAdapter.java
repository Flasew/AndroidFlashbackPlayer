package edu.ucsd.team6flashbackplayer;

import android.content.Context;
import android.graphics.Color;
import android.support.constraint.ConstraintLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
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

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        //map to song layout
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
