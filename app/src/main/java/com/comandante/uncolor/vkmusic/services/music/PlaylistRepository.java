package com.comandante.uncolor.vkmusic.services.music;

import com.comandante.uncolor.vkmusic.models.BaseMusic;

import java.util.ArrayList;
import java.util.List;

public class PlaylistRepository {

    private static PlaylistRepository instance;

    private static BaseMusic currentMusic;
    private static List<BaseMusic> playlist;
    private static int position;

    private PlaylistRepository() {
        currentMusic = null;
        playlist = new ArrayList<>();
    }

    public static void init(){
        if(instance == null) {
            instance = new PlaylistRepository();
        }
    }

    public static PlaylistRepository get(){
        if(instance == null){
            throw new NullPointerException("Playlist repository should be init");
        }
        return instance;
    }

    public static void updatePosition(int position){
        if(position >= playlist.size() || position < 0){
            return;
        }
        PlaylistRepository.position = position;
        currentMusic = playlist.get(position);
    }


    public void setPlaylist(List<BaseMusic> playlist) {
        PlaylistRepository.playlist = playlist;
    }

    public List<BaseMusic> getPlaylist() {
        return playlist;
    }

    public boolean isEmpty(){
        return playlist.isEmpty();
    }


    public BaseMusic getCurrentMusic() {
        return currentMusic;
    }

    public void setCurrentMusic(BaseMusic currentMusic) {
        PlaylistRepository.currentMusic = currentMusic;
    }

    public void setPosition(int position) {
        PlaylistRepository.position = position;
    }

    public int getPosition() {
        return position;
    }
}
