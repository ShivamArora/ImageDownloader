package com.example.shivam.imagedownloader;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class ImageDownloaderService extends IntentService {

    public static final String TAG = ImageDownloaderService.class.getSimpleName();
    private static final String NOTIFICATION_CHANNEL_ID = "image-downloader-notification";
    public static final String ACTION_VIEW_IMAGE = "view-image";
    public static final String ACTION_DISMISS_NOTIFICATION = "dismiss-notification";
    public static final int NOTIFICATION_ID=25;

    private NotificationManager notificationManager;
    private NotificationCompat.Builder notificationBuilder;
    private PendingIntent viewImageIntent,dismissNotificationIntent;

    public ImageDownloaderService(){
        super("ImageDownloaderService");
    }

    public ImageDownloaderService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (intent.getAction().equals(MainActivity.ACTION_DOWNLOAD_IMAGE)){
            setupNotification(this);

            String url = intent.getStringExtra("url");
            URL imageUrl = null;
            try{
                imageUrl = new URL(url);
            }
            catch (MalformedURLException e){
                e.printStackTrace();
                displayErrorNotification("Not a valid URL!");
                return;
            }

            HttpURLConnection urlConnection;
            try{
                urlConnection = (HttpURLConnection) imageUrl.openConnection();
                urlConnection.connect();

                if (urlConnection.getResponseCode()!= HttpURLConnection.HTTP_OK){
                    Log.i(TAG, "Not able to connect");
                    displayErrorNotification("Not able to connect");
                }

                int length = urlConnection.getContentLength();
                String filename = Uri.parse(url).getLastPathSegment();

                InputStream inputStream = urlConnection.getInputStream();
                FileOutputStream fileOutputStream;
                fileOutputStream = openFileOutput(filename, Context.MODE_PRIVATE);
                File file = getFileStreamPath(filename);
                Log.i(TAG, "File Path: "+file.getAbsolutePath());

                byte[] data = new byte[1024];
                long total = 0;
                int count;

                setupPendingIntent(this,filename);
                while ((count = inputStream.read(data))!=-1){
                    total += count;

                    if (length > 0){
                        int progress = (int)(total*100/length);
                        Log.i(TAG, "Progress: "+progress);
                        displayNotification(progress);
                    }
                    fileOutputStream.write(data,0,count);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void displayErrorNotification(String errorMsg){
        notificationBuilder.setContentText(errorMsg);
        notificationBuilder.setProgress(0,0,false);
        notificationBuilder.setOngoing(false);
        notificationBuilder.setDefaults(NotificationCompat.DEFAULT_VIBRATE);
        notificationManager.notify(NOTIFICATION_ID,notificationBuilder.build());
    }

    private void displayNotification(int progress){
        if (progress<100) {
            notificationBuilder.setContentText("Downloading image... "+progress+"%");
            notificationBuilder.setContentInfo(String.valueOf(progress)+"%");
            notificationBuilder.setProgress(100, progress, false);
            startForeground(NOTIFICATION_ID,notificationBuilder.build());
        }
        else{
            stopForeground(true);

            notificationBuilder.setContentText("Download completed!");
            notificationBuilder.setProgress(0,0,false);
            notificationBuilder
                    .setOngoing(false)
                    .addAction(new NotificationCompat.Action(android.R.drawable.ic_menu_view,"View",viewImageIntent))
                    .addAction(new NotificationCompat.Action(android.R.drawable.ic_menu_close_clear_cancel,"Ignore",dismissNotificationIntent));
            notificationManager.notify(NOTIFICATION_ID,notificationBuilder.build());
        }
    }

    private void setupNotification(Context context){

        notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        NotificationChannel notificationChannel = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID,context.getString(R.string.channel_name),NotificationManager.IMPORTANCE_DEFAULT);

            notificationManager.createNotificationChannel(notificationChannel);
        }

        //Create a NotificationCompat.Builder
        notificationBuilder = new NotificationCompat.Builder(context,NOTIFICATION_CHANNEL_ID)
                .setColor(ContextCompat.getColor(context,R.color.colorPrimary))
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(context.getString(R.string.app_name))
                .setContentText(context.getString(R.string.downloading_image))
                .setProgress(100,0,false)
                .setOngoing(true)
                .setContentIntent(null)
                .setAutoCancel(false);

    }

    private void setupPendingIntent(Context context,String filename){
        Intent intent = new Intent(context,DisplayImageActivity.class);
        intent.setAction(ACTION_VIEW_IMAGE);
        intent.putExtra("filename",filename);
        viewImageIntent = PendingIntent.getActivity(context,22,intent,PendingIntent.FLAG_ONE_SHOT);

        intent.setAction(ACTION_DISMISS_NOTIFICATION);
        dismissNotificationIntent = PendingIntent.getActivity(context,22,intent,PendingIntent.FLAG_ONE_SHOT);
    }
}
