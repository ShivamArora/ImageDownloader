package com.example.shivam.imagedownloader;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentActivity;
import android.support.v4.os.ResultReceiver;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    public static final String ACTION_DOWNLOAD_IMAGE = "download-image";

    Button btnDownload;
    EditText etUrl;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etUrl = findViewById(R.id.et_url);
        btnDownload = findViewById(R.id.btn_download);
        btnDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = etUrl.getText().toString();
                if (!TextUtils.isEmpty(url)) {
                    Intent intent = new Intent(MainActivity.this, ImageDownloaderService.class);
                    intent.setAction(ACTION_DOWNLOAD_IMAGE);
                    intent.putExtra("url", url);
                    startService(intent);
                }
                else{
                    View root = findViewById(android.R.id.content);
                    Snackbar.make(root,R.string.empty_url_error,Snackbar.LENGTH_SHORT).show();
                }
            }
        });
    }


    class ImageDownloaderTask extends AsyncTask<String,Integer,Void>{

        final String TAG = ImageDownloaderTask.class.getSimpleName();

        @Override
        protected Void doInBackground(String... urls) {
            String url = urls[0];
            URL imageUrl = null;
            try{
                imageUrl = new URL(url);
            }
            catch (MalformedURLException e){
                e.printStackTrace();
            }

            HttpURLConnection urlConnection;
            try{
                urlConnection = (HttpURLConnection) imageUrl.openConnection();
                urlConnection.connect();

                if (urlConnection.getResponseCode()!= HttpURLConnection.HTTP_OK){
                    Log.i(TAG, "doInBackground: "+"Not able to connect");
                }

                int length = urlConnection.getContentLength();
                String filename = Uri.parse(url).getLastPathSegment();

                InputStream inputStream = urlConnection.getInputStream();
                FileOutputStream fileOutputStream;
                fileOutputStream = openFileOutput(filename, Context.MODE_PRIVATE);
                File file = getFileStreamPath(filename);
                Log.i(TAG, "doInBackground: "+file.getAbsolutePath());

                byte[] data = new byte[1024];
                long total = 0;
                int count;
                while ((count = inputStream.read(data))!=-1){
                    total += count;

                    if (length > 0){
                        publishProgress((int) total * 100/ length);
                    }
                    fileOutputStream.write(data,0,count);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            Log.i(TAG, "onProgressUpdate: "+values[0]);
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            Log.i(TAG, "onPostExecute: "+"Download Complete!");
        }
    }
}
