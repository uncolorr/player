package com.example.uncolor.vkmusic.application;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.util.Log;

import com.example.uncolor.vkmusic.Apis.Api;
import com.flurry.android.FlurryAgent;

import io.realm.Realm;
import io.realm.RealmConfiguration;

/**
 * Created by Uncolor on 25.08.2018.
 */

public class App extends Application {

    private static final String FLURRY_API_KEY = "K3F4DFDFGW2WWSVNBHH6";
    private static App instance;
    public static final String APP_SETTINGS = "app_settings";
    public static final String APP_PREFERENCES_TOKEN = "app_token";

    private static SharedPreferences settings;

    @Override
    public void onCreate() {
        super.onCreate();

        new FlurryAgent.Builder()
                .withLogEnabled(true)
                .build(this, FLURRY_API_KEY);

        Api.init();
        instance = this;
        settings = getSharedPreferences(APP_SETTINGS, Context.MODE_PRIVATE);
        Realm.init(this);
        RealmConfiguration config = new RealmConfiguration.Builder().name("music.realm").build();
        Realm.setDefaultConfiguration(config);

    }

    public static Context getContext() {
        return instance.getApplicationContext();
    }


    public static void Log(String message) {
        if (!message.isEmpty()) {
            Log.i("fg", message);
        }
    }

    public static boolean isAuth() {
        return settings.contains(APP_PREFERENCES_TOKEN);
    }

    public static void saveToken(String token) {
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(APP_PREFERENCES_TOKEN, token);
        editor.apply();
    }

    public static String getToken(){
        return settings.getString(APP_PREFERENCES_TOKEN,"");
    }


    public static void logOut() {
        SharedPreferences.Editor editor = settings.edit();
        editor.remove(APP_PREFERENCES_TOKEN);
        editor.apply();
    }


    public static String getProviderAuthority() {
        return "com.example.uncolor.vkmusic.fileprovider";
    }
}
