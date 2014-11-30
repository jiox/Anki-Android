package com.ichi2.anki;

import android.app.ActionBar;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by jiox on 28.11.14.
 */
public class SmartwatchSettings extends NavigationDrawerActivity {
    private View view;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        view = getLayoutInflater().inflate(R.layout.smartwatch_fragment, null);
        setContentView(view);
        initNavigationDrawer(view);
    }
}



