package com.ichi2.anki;

import android.app.ActionBar;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.ichi2.libanki.Card;
import com.ichi2.libanki.Collection;
import com.ichi2.libanki.Decks;
import com.ichi2.libanki.Sched;
import com.ichi2.libanki.Utils;
import com.ichi2.themes.Themes;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by jiox on 28.11.14.
 */
public class SmartwatchSettings extends NavigationDrawerActivity {
    private View view;
    protected Sched mSched = null;
    private long mDeck;
    private Collection mCol;
    private String mBaseUrl;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        view = getLayoutInflater().inflate(R.layout.smartwatch_fragment, null);
        setContentView(view);
        initNavigationDrawer(view);
        startLoadingCollection();
    }
    protected void onCollectionLoaded(Collection col) {
        super.onCollectionLoaded(col);
        mCol = col;
        mSched = col.getSched();
        mBaseUrl = Utils.getBaseUrl(col.getMedia().dir());
        try {
            mDeck = mSched.getCol().getDecks().current().getLong("id");
        } catch (JSONException e) {
            e.printStackTrace();
        }


        initLayout();

        // Initialize dictionary lookup feature
        Lookup.initialize(this);

        deselectAllNavigationItems();
        supportInvalidateOptionsMenu();
        dismissOpeningCollectionDialog();
    }
    private View.OnClickListener mButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mSched == null)
            {
                return;
            }
            switch (v.getId())
            {
                case R.id.startLearning:
                    startWatchLearning();
                    break;
                case R.id.stopLearning:
                    break;
            }
        }
    };

    private void startWatchLearning()
    {
        List<Long> cids = mSched.getCol().getDb().queryColumn(Long.class, "select id from cards where did = " + mDeck, 0);

        //mSched.sortCards(Utils.toPrimitive(cids), 1, 1, true, false);
        Log.i("length", "" + cids.size());
        Log.i("first", "" + cids.get(0));
        Card card = mCol.getCard(cids.get(0));
        Log.i("question", card._getQA().get("q"));
        Log.i("answer", card.getPureAnswerForReading());
    }

    private void initLayout()
    {
        final Button buttonStart = (Button) findViewById(R.id.startLearning);
        final Button buttonStop = (Button) findViewById(R.id.stopLearning);
        buttonStart.setOnClickListener(mButtonClickListener);
        buttonStop.setOnClickListener(mButtonClickListener);
    }
}



