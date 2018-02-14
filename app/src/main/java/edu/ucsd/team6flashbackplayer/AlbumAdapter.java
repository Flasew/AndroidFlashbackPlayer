package edu.ucsd.team6flashbackplayer;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 * Created by frankwang on 2/10/18.
 */

public class AlbumAdapter extends BaseAdapter {
    private List<Album> albums;
    private LayoutInflater albumInf;
    private Context context;

    public AlbumAdapter(Context c, HashMap<String, Album> theAlbums){
        albums = new ArrayList<>(theAlbums.values());
        albumInf = LayoutInflater.from(c);
        context = c;
    }

    @Override
    public int getCount() {
        return albums.size();
    }

    @Override
    public Object getItem(int position) {
        return albums.get(position);
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return 0;
    }

    static class ViewHolder {
        TextView name;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        //map to albums to layout

        View row = convertView;
        final Album currAlbum = albums.get(position);

        if(row == null) {

            final AlbumAdapter.ViewHolder holder = new AlbumAdapter.ViewHolder();

            row = albumInf.inflate(R.layout.album_enrty, parent, false);
            //Now create the ViewHolder
            //and set its textView field to the proper value

            holder.name = (TextView)row.findViewById(R.id.album_name);
            holder.name.setSelected(true);
            holder.name.setText(currAlbum.getName());

            row.setTag(holder);
        } else {
            //We've already seen this one before!
            AlbumAdapter.ViewHolder holder = (AlbumAdapter.ViewHolder) row.getTag();
            holder.name.setSelected(true);
            holder.name.setText(currAlbum.getName());
        }

        return row;

    }
}
