package com.mk.m_folder;

import static com.mk.m_folder.data.Player.allTracks;
import static com.mk.m_folder.data.Player.artists;
import static com.mk.m_folder.data.Player.currentTrack;
import static com.mk.m_folder.data.Player.isPlaying;
import static com.mk.m_folder.data.Player.mediaPlayer;
import static com.mk.m_folder.data.Player.playList;
import static com.mk.m_folder.data.Player.trackNumber;

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
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.mk.m_folder.data.Helper;
import com.mk.m_folder.data.Player;
import com.mk.m_folder.data.entity.Album;
import com.mk.m_folder.data.entity.Artist;
import com.mk.m_folder.data.entity.Track;
import com.mk.m_folder.data.service.BaseService;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    Player player;

    public static int listLevel = 0;
    public static boolean readyToEnd = true;
    public static int artistId = 0;

    TextView trackAndListInfo;
    SeekBar playAudioProgress;
    ListView lvMain;

    int albumId = 0;

    public static Handler inOutHandler;
    public static Handler audioProgressHandler;
    public static Handler trackInfoHandler;
    public static Handler saveSettingsHandler;

    private static final int UPDATE_AUDIO_PROGRESS_BAR = 3;

    public static InputStream inputStream;
    public static OutputStream outputStream;

    BaseService baseService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        trackAndListInfo = findViewById(R.id.trackInfo);

        playAudioProgress = findViewById(R.id.play_audio_seek_bar);

        lvMain = findViewById(R.id.list_items);
        lvMain.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        player = new Player(this);

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
            showArtists();

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

    // media content
    public void showArtists() {
        String[] stringArtists = new String[artists.size()];
        int i = 0;
        Collections.sort(artists);
        for(Artist artist : artists) {
            stringArtists[i] = artist.getName();
            i++;
        }

        final MyArrayAdapter artistsAdapter = new MyArrayAdapter(this, stringArtists);
        lvMain.setAdapter(artistsAdapter);
        MyArrayAdapter.selectedItemPosition = 100;

        lvMain.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                //Log.d(TAG, "itemClick: position = " + position + ", id = " + id);
                view.setSelected(true);
                Collections.sort(artists.get(position).getAlbums());

                MainActivity.artistId = position;
                showAlbums(artists.get(position).getAlbums());
            }
        });

        lvMain.setLongClickable(true);
        lvMain.setOnItemLongClickListener((parent, v, position, id) -> {
            //Log.d(TAG, String.valueOf(position));
            Artist artist = artists.get(position);
            Collections.sort(artist.getAlbums());
            playList.clear();

            for(Album album : artist.getAlbums()) {
                Collections.sort(album.getTracks());
                for(Track track : album.getTracks()) {
                    for(Track thisTrack : allTracks) {
                        if(track.equals(thisTrack)) {
                            playList.add(allTracks.indexOf(thisTrack));
                            break;
                        }
                    }
                }
            }

            trackNumber = 0;
            player.playSong(playList.get(trackNumber));
            return true;
        });

        listLevel++;
    }

    public void showAlbums(final ArrayList<Album> albums) {
        String[] stringAlbums = new String[albums.size()];
        int i = 0;
        for(Album album : albums) {
            stringAlbums[i] = album.getName();
            i++;
        }

        final MyArrayAdapter albumsAdapter = new MyArrayAdapter(this, stringAlbums);
        lvMain.setAdapter(albumsAdapter);
        MyArrayAdapter.selectedItemPosition = 100;

        lvMain.setOnItemClickListener((parent, view, position, id) -> {
            //Log.d(TAG, "itemClick: position = " + position + ", id = " + id);
            view.setSelected(true);
            albumId = position;

            List<Track> albumTracks = albums.get(position).getTracks();
            Collections.sort(albumTracks);
            showSongs(albumTracks);
        });

        lvMain.setLongClickable(true);
        lvMain.setOnItemLongClickListener((parent, v, position, id) -> {
            playList.clear();

            List<Track> albumTracks = albums.get(position).getTracks();
            Collections.sort(albumTracks);
            for (Track track : albumTracks) {
                for (Track thisTrack : allTracks) {
                    if (track.equals(thisTrack)) {
                        playList.add(allTracks.indexOf(thisTrack));
                        break;
                    }
                }
            }

            trackNumber = 0;
            player.playSong(playList.get(trackNumber));

            return true;
        });

        listLevel++;
        readyToEnd = false;
        //Log.d(TAG, "showAlbums: " + listLevel);
    }

    public void showSongs(final List<Track> albumTracks) {
        String[] stringSongs = new String[albumTracks.size()];
        int i = 0;
        for(Track track : albumTracks) {
            stringSongs[i] = track.getName();
            i++;
        }

        final MyArrayAdapter albumsAdapter = new MyArrayAdapter(this, stringSongs);
        lvMain.setAdapter(albumsAdapter);

        lvMain.setOnItemClickListener((parent, view, position, id) -> {
            //Log.d(TAG, "itemClick: position = " + position + ", id = " + id);
            view.setSelected(true);
            MyArrayAdapter.selectedItemPosition = position;

            playList.clear();
            for(Track track : albumTracks) {
                for(Track thisTrack : allTracks) {
                    if(track.equals(thisTrack)) {
                        playList.add(allTracks.indexOf(thisTrack));
                        break;
                    }
                }
            }
            trackNumber = position;
            player.playSong(playList.get(trackNumber));
        });

        if(listLevel < 3) {
            listLevel++;
        }
    }

    // process back button
    @Override
    public void onBackPressed() {
        //Log.d(TAG, String.valueOf(listLevel));
        if(listLevel == 3) {
            listLevel = listLevel - 2;
            readyToEnd = false;
            showAlbums(artists.get(artistId).getAlbums());
        } else if(listLevel == 2) {
            listLevel = listLevel - 2;
            readyToEnd = false;
            showArtists();
        } else if(listLevel == 1) {
            readyToEnd = true;
        }

        if(readyToEnd) this.finishAffinity();
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

    // top right menu
    public  boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, 1, 0, "path to music");
        menu.add(0, 2, 0, "track info");
        menu.add(0, 3, 0, "delete track");
        menu.add(0, 4, 0, "show deleted tracks");
        menu.add(0, 5, 0, "clear deleted tracks");
        return super.onCreateOptionsMenu(menu);
    }
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 1: // path to music
                player.editPath();
                break;
            case 2: // track info
                String trackInfo = currentTrack.getFilePath();
//                Toast.makeText(this, trackInfo, Toast.LENGTH_LONG).show();
                Log.d(TAG, trackInfo);
                Intent intent = new Intent(this, ListActivity.class);
                intent.putExtra("content", trackInfo);
                startActivity(intent);
                break;
            case 3:
                deleteTrack();
                break;
            case 4:
                String deletedTracksInfo = Helper.getDeletedTracksInfo(baseService.getDeletions());
                Log.d(TAG, deletedTracksInfo);
                Intent deletedIntent = new Intent(this, ListActivity.class);
                deletedIntent.putExtra("content", deletedTracksInfo);
                startActivity(deletedIntent);
                break;
            case 5:
                int clearedDeletions = baseService.clearDeletions();
                Toast.makeText(this, String.format(Locale.ENGLISH,
                        "%d deleted tracks was cleared",
                        clearedDeletions),
                        Toast.LENGTH_LONG).show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void deleteTrack() {
        Log.d(TAG, "delete");
        baseService.insertDeletion(currentTrack.getName(), currentTrack.getArtistName(), currentTrack.getAlbumName(), currentTrack.getFile().getName());
        player.nextTrack();
        //File deletedFile = currentTrack.getFile();

        //boolean deleted = deletedFile.delete();
        //if (deleted) Log.d(TAG, "file was deleted");
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
