package com.comandante.uncolor.vkmusic.database;

import com.comandante.uncolor.vkmusic.application.App;
import com.comandante.uncolor.vkmusic.models.VkMusic;

import java.io.File;
import java.util.ArrayList;

import io.realm.Case;
import io.realm.Realm;
import io.realm.RealmResults;

public class DatabaseManager {

    private Realm realm;

    private static DatabaseManager instance;

    public static DatabaseManager get(){
        if(instance == null){
            instance = new DatabaseManager();
        }
        return instance;
    }

    private DatabaseManager() {
        realm = Realm.getDefaultInstance();
    }

    public void save(VkMusic track) {
        realm.executeTransaction(realm -> realm.insert(track));
    }

    public boolean removeTrack(VkMusic track) {
        App.Log("Database.removeTrack");
        File file = new File(track.getLocalPath());
        App.Log("local path: " + track.getLocalPath());


        if(file.exists()){
           boolean isDeleted = file.delete();
           if(isDeleted){
               App.Log("file deleted");
           }
        }
        realm.executeTransaction(realm -> {
            RealmResults<VkMusic> results = realm.where(VkMusic.class)
                    .equalTo("id", track.getId())
                    .findAll();
            results.deleteAllFromRealm();
        });
        return true;
    }

    public void clearCache() {
        realm.executeTransaction(realm -> {
            RealmResults<VkMusic> results = realm.where(VkMusic.class).findAll();
            for (int i = 0; i < results.size(); i++) {
                File file = new File(results.get(i).getLocalPath());
                if (file.exists()) {
                    file.delete();
                }
            }
            results.deleteAllFromRealm();
        });
    }

    public ArrayList<VkMusic> search(String query){
        RealmResults<VkMusic> results = realm.where(VkMusic.class)
                .beginGroup()
                .contains("artist", query, Case.INSENSITIVE)
                .or()
                .contains("title", query, Case.INSENSITIVE)
                .endGroup()
                .findAll();
         return new ArrayList<>(results);
    }

    public ArrayList<VkMusic> findAll(){
        RealmResults<VkMusic> results = realm.where(VkMusic.class).findAll();
        return new ArrayList<>(results);
    }

    public VkMusic findById(long id){
        return realm.where(VkMusic.class)
                .equalTo("id", id)
                .findFirst();
    }
}
