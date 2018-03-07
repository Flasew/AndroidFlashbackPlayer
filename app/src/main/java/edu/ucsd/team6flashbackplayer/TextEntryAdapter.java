package edu.ucsd.team6flashbackplayer;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
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

    private static final String TAG = "TextEntryAdapter";

    private List<Item> items;
    private LayoutInflater layoutInflater;
    private Context context;
    private boolean clickable;

    public TextEntryAdapter(Context c, List<Item> itemList){
        items = itemList;
        layoutInflater = LayoutInflater.from(c);
        context = c;
    }

    /**
     * Change the items stored
     * @param items new list of items
     */
    public void setItems(List<Item> items) {
        this.items = items;
    }

    /**
     * Get the number of entries in the list
     * @return size of the list
     */
    @Override
    public int getCount() {
        return items.size();
    }

    /**
     * Get the item at position
     * @param position position of the item to be acquired
     * @return item at position @position, type Item
     */
    @Override
    public Object getItem(int position) {
        return items.get(position);
    }

    /**
     * Unused.
     * @param position
     * @return
     */
    @Override
    public long getItemId(int position) {
        return 0;
    }

    /**
     * A view holder class the hold the views of an entry.
     */
    static class ViewHolder {
        TextView name;
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
        //map to albums to layout

        View row = convertView;
        final Item currItem = items.get(position);

        Log.d(TAG, "Position: " + position);
        Log.d(TAG, "Text: " + currItem.toString());

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
