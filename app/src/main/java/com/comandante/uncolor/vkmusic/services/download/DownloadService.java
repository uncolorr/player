package com.comandante.uncolor.vkmusic.services.download;

import android.app.IntentService;
import android.content.Intent;
import android.net.Uri;

import android.os.Bundle;
import android.os.Environment;

import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.RecyclerView;
import android.widget.Toast;


import com.comandante.uncolor.vkmusic.Apis.Api;
import com.comandante.uncolor.vkmusic.Apis.ApiResponse;
import com.comandante.uncolor.vkmusic.application.App;
import com.comandante.uncolor.vkmusic.models.BaseMusic;
import com.comandante.uncolor.vkmusic.models.VkMusic;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Objects;

import io.realm.Realm;
import okhttp3.ResponseBody;


/**
 * Created by Uncolor on 13.05.2018.
 */

public class DownloadService extends IntentService implements ApiResponse.ApiFailureListener {

    public static final String ACTION_DOWNLOAD_STARTED = "com.example.uncolor.action.DOWNLOAD_STARTED";
    public static final String ACTION_DOWNLOAD_COMPLETED = "com.example.uncolor.action.DOWNLOAD_COMPLETED";
    public static final String ACTION_DOWNLOAD_FAILURE = "com.example.uncolor.action.DOWNLOAD_FAILURE";

    public static final String ARG_MUSIC = "music";

    private Realm realm;

    public DownloadService() {
        super("Download Service");
        realm = Realm.getDefaultInstance();
    }

    private BaseMusic music;

    @Override
    public void onStart(@Nullable Intent intent, int startId) {
        App.Log("onStart service");
        super.onStart(intent, startId);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        App.Log("onHandleIntent service");
        Bundle extras = intent.getExtras();
        if (extras != null) {
            music = extras.getParcelable("music");
            if (music != null) {
                App.Log("download url: " + music.getDownload());
            }
        }

        if (music == null) {
            App.Log("music null");
            sendIntent(ACTION_DOWNLOAD_FAILURE);
            return;
        }
        String artist = "";
        String title = "";
        if (music.getArtist() != null) {
            artist = music.getArtist();
        }

        if (music.getTitle() != null) {
            title = music.getTitle();
        }
        File outputFile = new File(Environment
                .getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                artist + " - " + title + ".mp3");
        String uriFromFile = Uri.fromFile(outputFile).toString();
        App.Log("Uri from file: " + uriFromFile);
        initDownload();
    }

    private void initDownload() {
        App.Log("init download");
        onDownloadStarted(ACTION_DOWNLOAD_STARTED);
        Api.getSource().downloadFile(music.getDownload()).enqueue(ApiResponse
                .getCallback(getDownloadCallback(), this));

    }

    private ApiResponse.ApiResponseListener<ResponseBody> getDownloadCallback() {
        return new ApiResponse.ApiResponseListener<ResponseBody>() {
            @Override
            public void onResponse(ResponseBody result) {
                try {
                    downloadFile(result);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
    }

    private void downloadFile(ResponseBody body) throws IOException {
        App.Log("download file");
        int count;
        byte data[] = new byte[1024 * 4];
        long fileSize = body.contentLength();
        InputStream bis = new BufferedInputStream(body.byteStream(), 1024 * 8);
        String artist = "";
        String title = "";
        if (music.getArtist() != null) {
            artist = music.getArtist();
        }

        if (music.getTitle() != null) {
            title = music.getTitle();
        }
        File outputFile =
                new File(Environment
                        .getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                        artist + " - " + title + ".mp3");
        OutputStream output = new FileOutputStream(outputFile);
        long total = 0;
        long startTime = System.currentTimeMillis();
        int timeCount = 1;
        while ((count = bis.read(data)) != -1) {
            total += count;
            int totalFileSize = (int) (fileSize / (Math.pow(1024, 2)));
            double current = Math.round(total / (Math.pow(1024, 2)));

            int progress = (int) ((total * 100) / fileSize);
            long currentTime = System.currentTimeMillis() - startTime;

            Download download = new Download();
            download.setTotalFileSize(totalFileSize);

            if (currentTime > 1000 * timeCount) {

                download.setCurrentFileSize((int) current);
                download.setProgress(progress);
                timeCount++;
            }

            output.write(data, 0, count);
        }
        App.Log("output path: " + outputFile.getAbsolutePath());
        music.setLocalPath(outputFile.getAbsolutePath());
        music.setState(BaseMusic.STATE_COMPLETED);
        saveToRealm();
        onDownloadComplete(ACTION_DOWNLOAD_COMPLETED);
        output.flush();
        output.close();
        bis.close();
        App.Log("download complete");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        App.Log("onDestroy Download service");
    }

    public void saveToRealm(){
        if (music instanceof VkMusic) {
            VkMusic vkMusic = (VkMusic) music;
            realm.beginTransaction();
            realm.copyToRealm(vkMusic);
            realm.commitTransaction();
        }
    }


    private void sendIntent(String action) {
        App.Log("send intent");
        Intent intent = new Intent();
        intent.setAction(action);
        intent.putExtra(ARG_MUSIC, music);
        sendBroadcast(intent);
    }

    private void onDownloadComplete(String action) {
        App.Log("onDownloadComplete");
        sendIntent(action);
    }

    private void onDownloadStarted(String action) {
        App.Log("onDownloadStarted");
        sendIntent(action);
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {

    }

    @Override
    public void onFailure(int code, String message) {
        App.Log("Download failure");
        sendIntent(ACTION_DOWNLOAD_FAILURE);
    }
}