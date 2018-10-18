package com.comandante.uncolor.vkmusic.main_activity;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.comandante.uncolor.vkmusic.main_activity.my_music_fragment.VkMusicFragment_;
import com.comandante.uncolor.vkmusic.main_activity.playlists_fragment.PlaylistsFragment_;
import com.comandante.uncolor.vkmusic.main_activity.settings_fragment.SettingsFragment_;


/**
 * Created by Uncolor on 24.08.2018.
 */

public class MainPagerAdapter extends FragmentPagerAdapter {

    private static int PAGES_COUNT = 2;

    public MainPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return VkMusicFragment_.builder().build();
            case 1:
                return SettingsFragment_.builder().build();
        }
        return null;
    }

    @Override
    public int getCount() {
        return PAGES_COUNT;
    }
}
