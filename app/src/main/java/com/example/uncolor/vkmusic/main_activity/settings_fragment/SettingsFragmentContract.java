package com.example.uncolor.vkmusic.main_activity.settings_fragment;

public interface SettingsFragmentContract {
    interface View{
        void showUserInfo();
        void showClearCacheDialog();

    }

    interface Presenter {
        void onLoadMusicInfo();
        void onClearCache();

    }
}
