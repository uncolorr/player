package com.comandante.uncolor.vkmusic.services.download;

import android.app.DownloadManager;
import android.app.Service;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import androidx.annotation.Nullable;

import com.comandante.uncolor.vkmusic.application.App;
import com.comandante.uncolor.vkmusic.models.BaseMusic;

/**
 * Created by Uncolor on 25.10.2018.
 */

public class NewDownloadService extends Service {

    public static final String ACTION_DOWNLOAD_STARTED = "com.example.uncolor.action.DOWNLOAD_STARTED";
    public static final String ACTION_DOWNLOAD_COMPLETED = "com.example.uncolor.action.DOWNLOAD_COMPLETED";
    public static final String ACTION_DOWNLOAD_FAILURE = "com.example.uncolor.action.DOWNLOAD_FAILURE";

    private DownloadManager downloadManager;
    private DownloadBinder binder;
    private DownloadProgressListener downloadProgressListener;
    DownloadsMap<String, Long> downloads = new DownloadsMap<>();

    @Override
    public void onCreate() {
        super.onCreate();
        downloadManager = ((DownloadManager) getSystemService(DOWNLOAD_SERVICE));
        binder = new DownloadBinder();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }



    public void enqueue(BaseMusic music){
        if(music == null){
            App.Log("music null");
            return;
        }
        String artist = "";
        String title = "";
        if(music.getArtist() != null){
            artist = music.getArtist();
        }

        if(music.getTitle() != null){
            title = music.getTitle();
        }
        /*DownloadManager.Query query = new DownloadManager.Query();
        if(query!=null)
        {
            query.setFilterByStatus(DownloadManager.STATUS_RUNNING);
        }*/
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(music.getDownload()))
                .setTitle("file name")
                .setDestinationInExternalFilesDir(
                        this,
                        Environment.DIRECTORY_DOWNLOADS, artist + " - " + title + ".mp3");
        final long downloadId = downloadManager.enqueue(request);
        downloads.put(music.getDownload(), downloadId);
       /* new Thread(new Runnable() {

            @Override
            public void run() {

                boolean downloading = true;

                while (downloading) {

                    DownloadManager.Query q = new DownloadManager.Query();
                    q.setFilterById(downloadId);

                    Cursor cursor = downloadManager.query(q);
                    cursor.moveToFirst();
                    int bytes_downloaded = cursor.getInt(cursor
                            .getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
                    int bytes_total = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));

                    if (cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)) == DownloadManager.STATUS_SUCCESSFUL) {
                        downloading = false;
                    }

                    final double dl_progress = (bytes_downloaded / bytes_total) * 100;
                    App.Log("NEW DOWNLOAD SERVICE PROGRESS: " + dl_progress);
                    //Log.d(Constants.MAIN_VIEW_ACTIVITY, statusMessage(cursor));
                    cursor.close();
                }

            }
        }).start();*/
    }

    public class DownloadBinder extends Binder {
        public NewDownloadService getService() {
            return NewDownloadService.this;
        }
    }

    public void setDownloadListener(DownloadProgressListener listener){
        downloadProgressListener = listener;
    }

    public void onChangeCurrentTrack(BaseMusic music){
        boolean downloading = true;
        Long downloadId = downloads.get(music.getDownload());
        if(downloadId == null){
            return;
        }
        while (downloading) {
            DownloadManager.Query q = new DownloadManager.Query();
            q.setFilterById(downloadId);

            Cursor cursor = downloadManager.query(q);
            cursor.moveToFirst();
            int bytes_downloaded = cursor.getInt(cursor
                    .getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
            int bytes_total = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));
            final int dl_progress = (int) ((bytes_downloaded * 100L) / bytes_total);
            App.Log("NEW DOWNLOAD SERVICE PROGRESS: " + dl_progress);
            downloadProgressListener.onProgressChanged(dl_progress);
            if (cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)) == DownloadManager.STATUS_SUCCESSFUL) {
                downloading = false;
            }
            cursor.close();
        }
    }

    public String getDownloadUrl(long value){
        return downloads.getKey(value);
    }

    public interface DownloadProgressListener {
        void onProgressChanged(int progress);
    }
}
