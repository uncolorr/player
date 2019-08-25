package com.comandante.uncolor.vkmusic.application;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.comandante.uncolor.vkmusic.Apis.Api;
import com.comandante.uncolor.vkmusic.services.music.PlaylistRepository;
import com.crashlytics.android.Crashlytics;
import com.flurry.android.FlurryAgent;
import com.orhanobut.hawk.Hawk;

import io.fabric.sdk.android.Fabric;
import io.realm.Realm;
import io.realm.RealmConfiguration;

/**
 * Created by Uncolor on 25.08.2018.
 */

public class App extends Application {

    private static final String FLURRY_API_KEY = "K3F4DFDFGW2WWSVNBHH6";
    private static App instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        new FlurryAgent.Builder()
                .withLogEnabled(true)
                .build(this, FLURRY_API_KEY);
        Fabric.with(this, new Crashlytics());
        Hawk.init(instance).build();
        Api.init();
        Realm.init(this);
        RealmConfiguration config = new RealmConfiguration.Builder().name("music.realm").build();
        Realm.setDefaultConfiguration(config);
        PlaylistRepository.init();
    }

    public static Context getContext() {
        return instance.getApplicationContext();
    }

    public static void Log(String message) {
        if (!message.isEmpty()) {
            Log.i("fg", message);
        }
    }
}
