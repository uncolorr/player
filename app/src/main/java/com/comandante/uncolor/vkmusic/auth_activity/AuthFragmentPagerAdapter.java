package com.comandante.uncolor.vkmusic.auth_activity;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.comandante.uncolor.vkmusic.auth_activity.auth_fragment.AuthFragment;


/**
 * Created by Uncolor on 04.09.2018.
 */

public class AuthFragmentPagerAdapter extends FragmentPagerAdapter {

    private static final int PAGES_COUNT = 1;

    public AuthFragmentPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position){
            case 0:
                return AuthFragment.newInstance();
        }
        return null;
    }

    @Override
    public int getCount() {
        return PAGES_COUNT;
    }
}
