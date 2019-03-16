package com.comandante.uncolor.vkmusic.auth_activity;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.comandante.uncolor.vkmusic.auth_activity.auth_fragment.AuthFragment;
import com.comandante.uncolor.vkmusic.auth_activity.music_fragment.MusicFragment;


/**
 * Created by Uncolor on 04.09.2018.
 */

public class AuthFragmentPagerAdapter extends FragmentPagerAdapter {

    private static final int PAGES_COUNT = 2;

    public AuthFragmentPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position){
            case 0:
                return MusicFragment.newInstance();
            case 1:
                return AuthFragment.newInstance();
        }
        return null;
    }

    @Override
    public int getCount() {
        return PAGES_COUNT;
    }
}
