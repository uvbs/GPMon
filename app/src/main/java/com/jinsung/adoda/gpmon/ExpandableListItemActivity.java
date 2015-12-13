package com.jinsung.adoda.gpmon;

import android.app.Activity;
import android.app.ListActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.nhaarman.listviewanimations.appearance.simple.AlphaInAnimationAdapter;


/**
 * Created by adoda on 2015-12-13.
 */
public class ExpandableListItemActivity extends MyListActivity {

    private static final int INITIAL_DELAY_MILLIS = 500;
    private MyExpandableListItemAdapter mExpandableListItemAdapter;

    private boolean mLimited;

    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mExpandableListItemAdapter = new MyExpandableListItemAdapter(this);
        AlphaInAnimationAdapter alphaInAnimationAdapter = new AlphaInAnimationAdapter(mExpandableListItemAdapter);
        alphaInAnimationAdapter.setAbsListView(getListView());

        assert alphaInAnimationAdapter.getViewAnimator() != null;
        alphaInAnimationAdapter.getViewAnimator().setInitialDelayMillis(INITIAL_DELAY_MILLIS);

        getListView().setAdapter(alphaInAnimationAdapter);

        Toast.makeText(this, R.string.explainexpand, Toast.LENGTH_LONG).show();
    }
}
