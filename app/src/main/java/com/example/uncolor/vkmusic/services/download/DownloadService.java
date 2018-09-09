package com.example.uncolor.vkmusic.services.download;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;

import android.os.Build;
import android.os.Bundle;
import android.os.Environment;

import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.content.LocalBroadcastManager;


import com.example.uncolor.vkmusic.Apis.Api;
import com.example.uncolor.vkmusic.Apis.ApiResponse;
import com.example.uncolor.vkmusic.R;
import com.example.uncolor.vkmusic.application.App;
import com.example.uncolor.vkmusic.models.BaseMusic;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;

import okhttp3.ResponseBody;


/**
 * Created by Uncolor on 13.05.2018.
 */

public class DownloadService extends IntentService implements ApiResponse.ApiFailureListener {

    public DownloadService() {
        super("Download Service");
    }

    private NotificationCompat.Builder notificationBuilder;
    private NotificationManager notificationManager;
    private int totalFileSize;

    private BaseMusic music;

    @Override
    public void onStart(@Nullable Intent intent, int startId) {
        App.Log("onStart service");
        super.onStart(intent, startId);
        Bundle extras = intent.getExtras();
        if (extras != null) {
            music = extras.getParcelable("music");
            if(music != null) {
                App.Log("download url: " + music.getDownload());
            }
        }
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        App.Log("onHandleIntent service");
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationBuilder = new NotificationCompat.Builder(this, App.NOTIFICATION_CHANNEL_ID)
                .setContentTitle("Загрузка")
                .setContentText("Загрузка файла")
                .setAutoCancel(false)
                .setChannelId(App.NOTIFICATION_CHANNEL_ID)
                .setLargeIcon(BitmapFactory
                        .decodeResource(this.getResources(), R.mipmap.ic_launcher));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            notificationBuilder.setSmallIcon(R.drawable.ic_status_bar);
            notificationBuilder.setColor(getResources().getColor(R.color.colorMain));
        } else {
            notificationBuilder.setSmallIcon(R.drawable.ic_status_bar);
        }
        File outputFile =
                new File(Environment
                        .getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                        music.getArtist() + " - " + music.getTitle() + ".mp3");
        String uriFromFile = Uri.fromFile(outputFile).toString();
        App.Log("Uri from file: " + uriFromFile);
        Uri uri = FileProvider.getUriForFile(getApplicationContext(),
                App.getProviderAuthority(),
                outputFile);
        Intent i = new Intent(Intent.ACTION_VIEW, uri);
        i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        PendingIntent contentIntent =
                PendingIntent.getActivity(this, 0,
                        i, 0);
        notificationBuilder.setContentIntent(contentIntent);
        notificationManager.notify(0, notificationBuilder.build());
        initDownload();
    }

    private void initDownload() {
        App.Log("init download: " + music.getDownload());
        Api.getSource().downloadFile(music.getDownload()).enqueue(ApiResponse.getCallback(getDownloadCallback(), this));
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
        File outputFile =
                new File(Environment
                        .getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                        music.getArtist() + " - " + music.getTitle() + ".mp3");
        App.Log("bbb: " + outputFile.getAbsolutePath());
        OutputStream output = new FileOutputStream(outputFile);
        long total = 0;
        long startTime = System.currentTimeMillis();
        int timeCount = 1;
        while ((count = bis.read(data)) != -1) {
            total += count;
            totalFileSize = (int) (fileSize / (Math.pow(1024, 2)));
            double current = Math.round(total / (Math.pow(1024, 2)));

            int progress = (int) ((total * 100) / fileSize);

            long currentTime = System.currentTimeMillis() - startTime;

            Download download = new Download();
            download.setTotalFileSize(totalFileSize);

            if (currentTime > 1000 * timeCount) {

                download.setCurrentFileSize((int) current);
                download.setProgress(progress);
                sendNotification(download);
                timeCount++;
            }

            output.write(data, 0, count);
        }
        onDownloadComplete();
        output.flush();
        output.close();
        bis.close();

    }

    private void sendNotification(Download download) {
        App.Log("send notification");
        sendIntent(download);
        notificationBuilder.setProgress(100, download.getProgress(), false);
        notificationBuilder.setContentText("Скачивание файла " + download.getCurrentFileSize() + "/" + totalFileSize + " MB");
        notificationManager.notify(0, notificationBuilder.build());
    }

    private void sendIntent(Download download) {
        App.Log("send intent");
        Intent intent = new Intent(App.APP_MESSAGE_PROGRESS);
        intent.putExtra("download", download);
        LocalBroadcastManager.getInstance(DownloadService.this).sendBroadcast(intent);
    }

    private void onDownloadComplete() {
        App.Log("onDownloadComplete");
        Download download = new Download();
        download.setProgress(100);
        sendIntent(download);
        notificationManager.cancel(0);
        notificationBuilder.setProgress(0, 0, false);
        notificationBuilder.setContentText("Файл загружен в папку загрузок");
        int id = (int) ((new Date().getTime() / 1000L) % Integer.MAX_VALUE);
        notificationManager.notify(id, notificationBuilder.build());
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        notificationManager.cancel(0);
    }

    @Override
    public void onFailure(int code, String message) {
        App.Log(message);
    }
}