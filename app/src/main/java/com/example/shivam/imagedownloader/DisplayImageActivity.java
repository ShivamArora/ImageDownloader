package com.example.shivam.imagedownloader;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;

import java.io.File;

public class DisplayImageActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_image);
        Intent intent = getIntent();
        if (intent!=null && intent.getAction()!=null){
            String action = intent.getAction();
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.cancel(ImageDownloaderService.NOTIFICATION_ID);

            if (action.equals(ImageDownloaderService.ACTION_VIEW_IMAGE)){
                displayImage(intent.getStringExtra("filename"));
            }
            else if (action.equals(ImageDownloaderService.ACTION_DISMISS_NOTIFICATION)){
                finish();
            }
        }
    }

    private void displayImage(String filename) {
        if (!TextUtils.isEmpty(filename)){
            ImageView imageView  =  findViewById(R.id.imageView);
            imageView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_IMMERSIVE|View.SYSTEM_UI_FLAG_FULLSCREEN|View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
            File file = getFileStreamPath(filename);
            Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
            imageView.setImageBitmap(bitmap);
        }
    }
}
