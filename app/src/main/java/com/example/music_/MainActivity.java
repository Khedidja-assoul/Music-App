package com.example.music_;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.ConditionVariable;

import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    EditText downloadingLink;
    Button downloadButton ;
    ImageView playImage;
    ImageView stopImage;
    ImageView pauseImage;

    int counter = 0, cursor = 0;
    
    private MediaPlayer myMediaPlayer;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        int REQUEST_CODE=1;

        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        }, REQUEST_CODE);

        downloadingLink = findViewById(R.id.downloadingLink);
        playImage = findViewById(R.id.playImage);
        pauseImage = findViewById(R.id.pauseImage);
        stopImage = findViewById(R.id.stopImage);

        playImage.setVisibility(View.GONE);
        stopImage.setVisibility(View.GONE);
        pauseImage.setVisibility(View.GONE);

        downloadButton = findViewById(R.id.downloadButton);

        playImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playMusic();
            }
        });
        pauseImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pauseMusic();
            }
        });
        stopImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopMusic();
            }
        });




        downloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!downloadingLink.getText().toString().equals("")){
                    Toast.makeText(MainActivity.this,downloadingLink.getText().toString(),Toast.LENGTH_LONG).show();
                    Uri uri = Uri.parse(downloadingLink.getText().toString());
                    new DownloadFile().execute(uri);
                }else{
                    Toast.makeText(MainActivity.this,"you need to paste your link first ",Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    protected void playMusic(){

        Uri firstUri = Uri.parse("file:///sdcard/"+"music"+counter);
        myMediaPlayer  = new MediaPlayer();

        myMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

        try {
            myMediaPlayer.setDataSource(getApplicationContext(), firstUri);
            myMediaPlayer.prepare();
            myMediaPlayer.seekTo(cursor);
            myMediaPlayer.start();
            Toast.makeText(MainActivity.this,"music is playing now",Toast.LENGTH_LONG).show();


            playImage.setVisibility(View.GONE);
            pauseImage.setVisibility(View.VISIBLE);
            stopImage.setVisibility(View.VISIBLE);

        } catch (IOException e) {}
    }

    protected void stopMusic(){

        myMediaPlayer.stop();
        myMediaPlayer.release();
        cursor = 0;

        playImage.setVisibility(View.VISIBLE);
        pauseImage.setVisibility(View.GONE);
        stopImage.setVisibility(View.GONE);

    }

    protected void pauseMusic(){

        myMediaPlayer.pause();
        cursor= myMediaPlayer.getCurrentPosition();

        playImage.setVisibility(View.VISIBLE);
        pauseImage.setVisibility(View.GONE);
        stopImage.setVisibility(View.VISIBLE);
    }


    class DownloadFile extends AsyncTask<Uri, Integer, Integer> {

        @Override
        protected Integer doInBackground(Uri... uris) {
                DownloadData(uris[0]);
            return 0;
        }
        @Override
        protected void onProgressUpdate(Integer... values) {

        }
        @Override
        protected void onPostExecute(Integer s) {
            playImage.setVisibility(View.VISIBLE);
            stopImage.setVisibility(View.GONE);
            pauseImage.setVisibility(View.GONE);
            downloadingLink.setText("");
        }
        private void DownloadData (Uri uri) {

            DownloadManager downloadManager = (DownloadManager)
                    getApplicationContext().getSystemService(Context.DOWNLOAD_SERVICE);
            DownloadManager.Request request = new DownloadManager.Request(uri);
            request.setTitle("Ma song");
            request.setDescription("Downloading");

            counter++;
            request.setDestinationInExternalPublicDir("",
                    "music"+counter);

            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            final long downloadId=downloadManager.enqueue(request);

            final ConditionVariable myCondition = new ConditionVariable(false);
            IntentFilter filter = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
            BroadcastReceiver receiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    long reference = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
                    if (downloadId == reference) {
                        myCondition.open(); }
                }
            };
            getApplicationContext().registerReceiver(receiver, filter);
            myCondition.block();

        }
            }


}
