package com.comandante.uncolor.vkmusic.application;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.provider.Settings;
import android.util.Base64;
import android.util.Log;

import com.comandante.uncolor.vkmusic.Apis.Api;
import com.crashlytics.android.Crashlytics;
import com.flurry.android.FlurryAgent;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;

import io.fabric.sdk.android.Fabric;
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



    protected static final String UTF8 = "utf-8";
    private static final char[] SECRET = {'f', '4','y', 'f','a', 'f','b', 'g','f', 'h','f', 'o'} ;

    private static SharedPreferences settings;

    private static ContentResolver contentResolver;

    @Override
    public void onCreate() {
        super.onCreate();

        new FlurryAgent.Builder()
                .withLogEnabled(true)
                .build(this, FLURRY_API_KEY);
        Fabric.with(this, new Crashlytics());

        Api.init();
        instance = this;
        settings = getSharedPreferences(APP_SETTINGS, Context.MODE_PRIVATE);
        contentResolver = getContentResolver();
        Realm.init(this);
        RealmConfiguration config = new RealmConfiguration.Builder().name("music.realm").build();
        Realm.setDefaultConfiguration(config);
      /*  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            App.getContext().startForegroundService(new Intent(App.getContext(), NewDownloadService.class));
        } else {
            App.getContext().startService(new Intent(App.getContext(), NewDownloadService.class));
        }*/
       // startService(new Intent(this, NewDownloadService.class));
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
        token = encrypt(token);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(APP_PREFERENCES_TOKEN, token);
        editor.apply();
    }

    public static String getToken(){
        return decrypt(settings.getString(APP_PREFERENCES_TOKEN,""));
    }


    public static void logOut() {
        SharedPreferences.Editor editor = settings.edit();
        editor.remove(APP_PREFERENCES_TOKEN);
        editor.apply();
    }

    @SuppressLint("HardwareIds")
    private static String encrypt(String value) {
        try {
            final byte[] bytes = value != null ? value.getBytes(UTF8) : new byte[0];
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBEWithMD5AndDES");
            SecretKey key = keyFactory.generateSecret(new PBEKeySpec(SECRET));
            Cipher pbeCipher = Cipher.getInstance("PBEWithMD5AndDES");
            pbeCipher.init(Cipher.ENCRYPT_MODE, key,
                    new PBEParameterSpec(Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
                            .getBytes(UTF8), 20));
            return new String(Base64.encode(pbeCipher.doFinal(bytes), Base64.NO_WRAP),UTF8);

        } catch( Exception e ) {
            throw new RuntimeException(e);
        }

    }

    @SuppressLint("HardwareIds")
    private static String decrypt(String value){
        try {
            final byte[] bytes = value != null ? Base64.decode(value, Base64.DEFAULT) : new byte[0];
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBEWithMD5AndDES");
            SecretKey key = keyFactory.generateSecret(new PBEKeySpec(SECRET));
            Cipher pbeCipher = Cipher.getInstance("PBEWithMD5AndDES");
            pbeCipher.init(Cipher.DECRYPT_MODE, key,
                    new PBEParameterSpec(Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
                            .getBytes(UTF8), 20));
            return new String(pbeCipher.doFinal(bytes),UTF8);

        } catch( Exception e) {
            throw new RuntimeException(e);
        }
    }
}
