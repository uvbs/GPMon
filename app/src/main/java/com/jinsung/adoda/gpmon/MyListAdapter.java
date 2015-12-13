package com.jinsung.adoda.gpmon;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.nhaarman.listviewanimations.ArrayAdapter;
import android.widget.TextView;

/**
 * Created by adoda on 2015-12-13.
 */
public class MyListAdapter extends ArrayAdapter<String> {

    private final Context mContext;

    public MyListAdapter(final Context context) {
        mContext = context;
        for (int i = 0; i < 1000; i++) {
            add(mContext.getString(R.string.row_number, i));
        }
    }

    @Override
    public long getItemId(final int position) {
        return getItem(position).hashCode();
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public View getView(final int position, final View convertView, final ViewGroup parent) {
        TextView view = (TextView) convertView;
        if (view == null) {
            view = (TextView) LayoutInflater.from(mContext).inflate(R.layout.list_row, parent, false);
        }

        view.setText(getItem(position));

        return view;
    }
}
