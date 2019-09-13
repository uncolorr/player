package com.comandante.uncolor.vkmusic.services.download;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;

import androidx.annotation.Nullable;

import com.comandante.uncolor.vkmusic.Apis.Api;
import com.comandante.uncolor.vkmusic.Apis.ApiResponse;
import com.comandante.uncolor.vkmusic.application.App;
import com.comandante.uncolor.vkmusic.database.DatabaseManager;
import com.comandante.uncolor.vkmusic.models.BaseMusic;
import com.comandante.uncolor.vkmusic.models.VkMusic;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import okhttp3.ResponseBody;


/**
 * Created by Uncolor on 13.05.2018.
 */

public class DownloadService extends IntentService implements ApiResponse.ApiFailureListener {

    public static final String ACTION_DOWNLOAD_STARTED = "com.example.uncolor.action.DOWNLOAD_STARTED";
    public static final String ACTION_DOWNLOAD_COMPLETED = "com.example.uncolor.action.DOWNLOAD_COMPLETED";
    public static final String ACTION_DOWNLOAD_FAILURE = "com.example.uncolor.action.DOWNLOAD_FAILURE";

    public static final String ARG_MUSIC = "music";

    private BaseMusic music;

    public DownloadService() {
        super("Download service");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        App.Log("onHandleIntent");
        if(intent == null){
            return;
        }
        Bundle extras = intent.getExtras();
        if(extras == null){
            return;
        }

        music = extras.getParcelable(ARG_MUSIC);
        if(music == null){
            return;
        }

        initDownload(music);
    }

    private void initDownload(BaseMusic music) {
        App.Log("init download");
        onDownloadStarted(ACTION_DOWNLOAD_STARTED);
        Api.getSource().downloadFile(music.getDownload()).enqueue(ApiResponse
                .getCallback(getDownloadCallback(), this));

    }

    private ApiResponse.ApiResponseListener<ResponseBody> getDownloadCallback() {
        return result -> {
            try {
                downloadFile(result);
            } catch (IOException e) {
                e.printStackTrace();
            }
        };
    }

    private void downloadFile(ResponseBody body) throws IOException {
        App.Log("download file");
        int count;
        byte[] data = new byte[1024 * 4];
        long fileSize = body.contentLength();
        InputStream bis = new BufferedInputStream(body.byteStream(), 1024 * 8);
        File outputFile =
                new File(Environment
                        .getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                        music.getArtist() + " - " + music.getTitle() + ".mp3");
        OutputStream output = new FileOutputStream(outputFile);

        long total = 0;
        long startTime = System.currentTimeMillis();
        int timeCount = 1;
        Download download = new Download();

        while ((count = bis.read(data)) != -1) {
            total += count;
            int totalFileSize = (int) (fileSize / (Math.pow(1024, 2)));
            double current = Math.round(total / (Math.pow(1024, 2)));

            int progress = (int) ((total * 100) / fileSize);
            long currentTime = System.currentTimeMillis() - startTime;

            download.setTotalFileSize(totalFileSize);

            if (currentTime > 1000 * timeCount) {

                download.setCurrentFileSize((int) current);
                download.setProgress(progress);
                timeCount++;
            }

            output.write(data, 0, count);
        }
        output.flush();
        output.close();
        bis.close();

        music.setLocalPath(outputFile.getAbsolutePath());
        music.setState(BaseMusic.STATE_COMPLETED);
        saveToRealm();
        onDownloadComplete(ACTION_DOWNLOAD_COMPLETED);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        App.Log("onDestroy Download service");
    }

    public void saveToRealm(){
        if (music instanceof VkMusic) {
            VkMusic vkMusic = (VkMusic) music;
            DatabaseManager.get().save(vkMusic);
        }
    }


    private void sendBroadcastIntent(String action) {
        App.Log("send intent");
        Intent intent = new Intent();
        intent.setAction(action);
        intent.putExtra(ARG_MUSIC, music);
        sendBroadcast(intent);
    }

    private void onDownloadComplete(String action) {
        App.Log("onDownloadComplete");
        sendBroadcastIntent(action);
    }

    private void onDownloadStarted(String action) {
        App.Log("onDownloadStarted");
        sendBroadcastIntent(action);
    }

    @Override
    public void onFailure(int code, String message) {
        App.Log("Download failure");
        sendBroadcastIntent(ACTION_DOWNLOAD_FAILURE);
    }
}