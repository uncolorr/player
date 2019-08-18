package com.comandante.uncolor.vkmusic.application;

import com.orhanobut.hawk.Hawk;

public class AppSettings {

    private static final String APP_PREFERENCES_TOKEN = "app_token";

    public static boolean isAuth() {
        return Hawk.contains(APP_PREFERENCES_TOKEN);
    }

    public static void signIn(String token) {
        Hawk.put(APP_PREFERENCES_TOKEN, token);
    }

    public static String getToken(){
        return Hawk.get(APP_PREFERENCES_TOKEN);
    }


    public static void logOut() {
        Hawk.delete(APP_PREFERENCES_TOKEN);
    }
}
