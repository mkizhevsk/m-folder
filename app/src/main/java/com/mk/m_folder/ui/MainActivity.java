package com.mk.m_folder.ui;

import static com.mk.m_folder.media.Player.artists;
import static com.mk.m_folder.media.Player.isPlaying;
import static com.mk.m_folder.media.Player.mediaPlayer;
import static com.mk.m_folder.media.Player.playList;
import static com.mk.m_folder.media.Player.trackNumber;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.mk.m_folder.R;
import com.mk.m_folder.data.entity.Track;
import com.mk.m_folder.media.Player;
import com.mk.m_folder.service.BaseService;
import com.mk.m_folder.util.Helper;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    public Player player;

    public static int artistId = 0;

    private int listLevel = 0;
    private static final int ARTIST_LEVEL = 1;
    private static final int ALBUM_LEVEL = 2;
    private static final int TRACK_LEVEL = 3;

    TextView trackAndListInfo;
    SeekBar playAudioProgress;
    ListView lvMain;

    static int albumId = 0;

    public static Handler inOutHandler;
    public static Handler audioProgressHandler;
    public static Handler trackInfoHandler;
    public static Handler saveSettingsHandler;

    private static final int UPDATE_AUDIO_PROGRESS_BAR = 3;

    public static InputStream inputStream;
    public static OutputStream outputStream;

    public BaseService baseService;
    UIHandler uiHandler;

    OptionsMenuHandler optionsMenuHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        trackAndListInfo = findViewById(R.id.trackInfo);

        playAudioProgress = findViewById(R.id.play_audio_seek_bar);

        lvMain = findViewById(R.id.list_items);
        lvMain.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        player = new Player(this);
        optionsMenuHandler = new OptionsMenuHandler(this, player);
        uiHandler = new UIHandler(this, player, lvMain);

        if(Helper.checkPermissions(this, this)) {
            Log.d(TAG, "permission granted by default");
            startBaseService();
        }

        inOutHandler = getInOutHandler(); // load and organize the artists then show them
        audioProgressHandler = getAudioProgressHandler(); // show updated playAudioProgress every 1 second
        trackInfoHandler = getTrackInfoHandler();
        saveSettingsHandler = getSaveSettingsHandler();

        playAudioProgress.setOnSeekBarChangeListener(seekBarChangeListener);
    }

    // handlers
    private Handler getInOutHandler() {
        return new Handler(message -> {
            Collections.sort(artists);
            Log.d(TAG, "MainActivity inOutHandler artists: " + artists.size());
            uiHandler.showArtists();

            return true;
        });
    }

    private Handler getAudioProgressHandler() {
        return new Handler(message -> {
            if (message.what == UPDATE_AUDIO_PROGRESS_BAR && mediaPlayer != null && isPlaying) {
                int currentProgress = mediaPlayer.getCurrentPosition() / 1000;
                playAudioProgress.setProgress(currentProgress);
                trackAndListInfo.setText((trackNumber + 1) + " из " + playList.size());
            }
            return true;
        });
    }

    private Handler getTrackInfoHandler() {
        return new Handler(message -> {
            Bundle bundle = message.getData();
            String[] trackInfo = bundle.getStringArray("trackInfo");
            //Log.d(TAG, "trackInfo: " + trackInfo[0] + " - " + trackInfo[1] + " - " + trackInfo[2] + " - " + trackInfo[3]);

            Track track = baseService.getTrackByFilePath(trackInfo[3]);
            if(track == null) {
                baseService.insertTrack(trackInfo[0], trackInfo[1], trackInfo[2], trackInfo[3]);
            }

            return true;
        });
    }

    private Handler getSaveSettingsHandler() {
        return new Handler(message -> {
            baseService.saveSettings(player.getTempPath());
            return true;
        });
    }

    // BaseService
    private void startBaseService() {
        Log.d(TAG, "MainActivity startBaseService()");
        Intent intent = new Intent(this, BaseService.class);
        bindService(intent, baseServiceConnection, Context.BIND_AUTO_CREATE);
    }

    private ServiceConnection baseServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            BaseService.LocalBinder binder = (BaseService.LocalBinder) service;
            baseService = binder.getService();
            Log.d(TAG, "MainActivity baseService onServiceConnected");

            String tempPath = baseService.getSettings().get(0);
            Log.d(TAG, "MainActivity baseService tempPath=" + tempPath);

            List<Track> dbTracks = baseService.getTracks();
            Log.d(TAG, "MainActivity dbTracks=" + dbTracks.size());

            player.getMediaFiles(tempPath, dbTracks);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

            Log.d(TAG, "MainActivity baseService onBaseServiceDisconnected");
        }
    };

    // process back button
    @Override
    public void onBackPressed() {
        Log.d(TAG, String.valueOf(uiHandler.getListLevel()));
        switch (uiHandler.getListLevel()) {
            case TRACK_LEVEL:
                uiHandler.showAlbums(artists.get(artistId).getAlbums());
                uiHandler.setListLevel(ALBUM_LEVEL);
                break;
            case ALBUM_LEVEL:
                uiHandler.showArtists();
                uiHandler.setListLevel(ARTIST_LEVEL);
                break;
            case ARTIST_LEVEL:
                this.finishAffinity();
                break;
        }
    }

    public void playNextTrack(View view) {
        player.nextTrack();
    }

    // SeekBar
    private SeekBar.OnSeekBarChangeListener seekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            //Log.d(TAG, String.valueOf(progress));
            if(mediaPlayer != null && fromUser){
                mediaPlayer.seekTo(progress * 1000);
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    };

    public void onClick(View view) {
        if (mediaPlayer == null)
            return;
        switch (view.getId()) {
            case R.id.btnPlayPause:
                player.playPause();
                break;
            case R.id.btnForward:
                player.fastForward();
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        player.handleActivityResult(requestCode, resultCode, data);
    }

    // top right menu
    public  boolean onCreateOptionsMenu(Menu menu) {
        return optionsMenuHandler.onCreateOptionsMenu(menu);
    }
    public boolean onOptionsItemSelected(MenuItem item) {
        return optionsMenuHandler.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d(TAG, "permission granted by request");
        startBaseService();
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy");
        isPlaying = false;

        baseService.saveSettings(player.getTempPath());
        //baseService.exportDatabase();

        if(inputStream != null) {
            try {
                inputStream.close();
                Log.d(TAG, "inputStream was closed");
            } catch (IOException e) {
                Log.d(TAG, "Error occurred when closing inputStream");
            }
        }

        if(outputStream != null) {
            try {
                outputStream.close();
                Log.d(TAG, "outputStream was closed");
            } catch (IOException e) {
                Log.d(TAG, "Error occurred when closing outputStream");
            }
        }

        player.reset();

        stopService(new Intent(this, BaseService.class));
        if (baseServiceConnection != null) {
            unbindService(baseServiceConnection);
        }

        Log.d(TAG, "finish in MainActivity");
        super.onDestroy();
    }
}
