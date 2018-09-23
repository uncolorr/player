package com.comandante.uncolor.vkmusic.auth_activity;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.comandante.uncolor.vkmusic.auth_activity.auth_fragment.AuthFragment_;
import com.comandante.uncolor.vkmusic.auth_activity.music_fragment.MusicFragment_;


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
                return MusicFragment_.builder().build();
            case 1:
                return AuthFragment_.builder().build();
        }
        return null;
    }

    @Override
    public int getCount() {
        return PAGES_COUNT;
    }
}
