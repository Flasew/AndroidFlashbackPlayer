package edu.ucsd.team6flashbackplayer;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by frankwang on 2/10/18.
 */

/**
 * text entry adapter of item type. allows customized view
 * Item should have a toString method to be converted to string.
 * @param <Item> Item type for returning in getItem method.
 */
public class TextEntryAdapter<Item> extends BaseAdapter {
    private List<Item> items;
    private LayoutInflater layoutInflater;
    private Context context;
    private boolean clickable;

    public TextEntryAdapter(Context c, List<Item> itemList){
        items = itemList;
        layoutInflater = LayoutInflater.from(c);
        context = c;
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Object getItem(int position) {
        return items.get(position);
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
        final Item currItem = items.get(position);

        if(row == null) {

            final TextEntryAdapter.ViewHolder holder = new TextEntryAdapter.ViewHolder();

            row = layoutInflater.inflate(R.layout.name_entry, parent, false);
            //Now create the ViewHolder
            //and set its textView field to the proper value

            holder.name = (TextView)row.findViewById(R.id.entry_name);
            holder.name.setSelected(true);
            holder.name.setText(currItem.toString());

            row.setTag(holder);
        } else {
            //We've already seen this one before!
            TextEntryAdapter.ViewHolder holder = (TextEntryAdapter.ViewHolder) row.getTag();
            holder.name.setSelected(true);
            holder.name.setText(currItem.toString());
        }

        return row;
    }

}
