package com.jinsung.adoda.gpmon;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.nhaarman.listviewanimations.itemmanipulation.expandablelistitem.ExpandableListItemAdapter;

/**
 * Created by adoda on 2015-12-13.
 */
public class MyExpandableListItemAdapter extends ExpandableListItemAdapter<Integer> {

    private final Context mContext;
    private final BitmapCache mMemoryCache;

    public MyExpandableListItemAdapter(final Context context) {
        super(context, R.layout.activity_expandablelistitem_card, R.id.activity_expandablelistitem_card_title, R.id.activity_expandablelistitem_card_content);
        mContext = context;
        mMemoryCache = new BitmapCache();

        for (int i = 0; i < 100; i++) {
            add(i);
        }
    }

    @Override
    public View getTitleView(final int position, final View convertView, final ViewGroup parent) {
        TextView tv = (TextView) convertView;
        if (tv == null) {
            tv = new TextView(mContext);
        }
        tv.setText(mContext.getString(R.string.expandorcollapsecard, (int) getItem(position)));
        return tv;
    }

    @Override
    public View getContentView(final int position, final View convertView, final ViewGroup parent) {
        MonitorView imageView = (MonitorView) convertView;
        if (imageView == null) {
            imageView = new MonitorView(mContext);
        }

        return imageView;
    }

    private void addBitmapToMemoryCache(final int key, final Bitmap bitmap) {
        if (getBitmapFromMemCache(key) == null) {
            mMemoryCache.put(key, bitmap);
        }
    }

    private Bitmap getBitmapFromMemCache(final int key) {
        return mMemoryCache.get(key);
    }
}
