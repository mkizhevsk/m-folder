package com.mk.m_folder;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.mk.m_folder.data.InOut;
import com.mk.m_folder.data.Player;
import com.mk.m_folder.data.entity.Album;
import com.mk.m_folder.data.entity.Artist;
import com.mk.m_folder.data.entity.Track;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.mk.m_folder.data.InOut.tempPath;
import static com.mk.m_folder.data.Player.afChangeListener;
import static com.mk.m_folder.data.Player.allTracks;
import static com.mk.m_folder.data.Player.artists;
import static com.mk.m_folder.data.Player.audioManager;
import static com.mk.m_folder.data.Player.isPlaying;
import static com.mk.m_folder.data.Player.mediaPlayer;
import static com.mk.m_folder.data.Player.playList;
import static com.mk.m_folder.data.Player.trackNumber;
import static com.mk.m_folder.data.Player.wrongSongs;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    //Helper helper;
    Player player;

    public static int listLevel = 0;
    public static boolean readyToEnd = true;
    public static int artistId = 0;

    TextView trackAndListInfo;
//    public ImageView coverImageView;
//    public TextView songTextView;
//    public TextView artistTextView;
//    public TextView albumTextView;
    SeekBar playAudioProgress;
    ListView lvMain;

    int albumId = 0;

    public static Handler inOutHandler;
    public static Handler audioProgressHandler = new Handler();

    private static final int UPDATE_AUDIO_PROGRESS_BAR = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        trackAndListInfo = findViewById(R.id.trackInfo);
//        coverImageView = findViewById(R.id.coverImage);
//        songTextView = findViewById(R.id.textSong);
//        artistTextView = findViewById(R.id.textArtist);
//        albumTextView = findViewById(R.id.textAlbum);
        playAudioProgress = findViewById(R.id.play_audio_seek_bar);

        lvMain = findViewById(R.id.list_items);
        lvMain.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        //helper = new Helper(this, this);
        player = new Player(this,this);

        if(player.checkPermissions()) {
            Log.d(TAG, "permission granted by default");
            player.getMediaFiles();
            Log.d(TAG, String.valueOf(allTracks.size()));
        }

        inOutHandler = new Handler(new Handler.Callback() {
            public boolean handleMessage(Message message) {
                Collections.sort(artists);
                Log.d(TAG, "artists: " + artists.size());
                listLevel++;
                showArtists();

                return true;
            }
        });

        audioProgressHandler = new Handler(new Handler.Callback() {
            public boolean handleMessage(Message message) {
                if (message.what == UPDATE_AUDIO_PROGRESS_BAR) {
                    if (mediaPlayer != null) {
                        if(isPlaying) {
                            int currentProgress = mediaPlayer.getCurrentPosition() / 1000;
                            playAudioProgress.setProgress(currentProgress);
                            trackAndListInfo.setText((trackNumber + 1) + " из " + playList.size());
                        }
                    }
                }

                return true;
            }
        });

        playAudioProgress.setOnSeekBarChangeListener(seekBarChangeListener);
    }

    // media content
    public void showArtists() {
        String[] stringArtists = new String[artists.size()];
        int i = 0;
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
                //adapter.setSelectedItem(position);
                Collections.sort(artists.get(position).getAlbums());

                MainActivity.artistId = position;
                MainActivity.listLevel++;
                showAlbums(artists.get(position).getAlbums());
            }
        });

        lvMain.setLongClickable(true);
        lvMain.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            public boolean onItemLongClick(AdapterView<?> parent, View v, int position, long id) {
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
            }
        });
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

        lvMain.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                //Log.d(TAG, "itemClick: position = " + position + ", id = " + id);
                view.setSelected(true);

                Collections.sort(albums.get(position).getTracks());

                albumId = position;
                MainActivity.listLevel++;
                showSongs(albums.get(position).getTracks());

            }
        });
    }
    public void showSongs(final List<Track> tracks) {
        String[] stringSongs = new String[tracks.size()];
        int i = 0;
        for(Track track : tracks) {
            stringSongs[i] = track.getName();
            i++;
        }

        final MyArrayAdapter albumsAdapter = new MyArrayAdapter(this, stringSongs);
        lvMain.setAdapter(albumsAdapter);

        lvMain.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                Log.d(TAG, "itemClick: position = " + position + ", id = " + id);
                view.setSelected(true);

                playList.clear();
                for(Track track : tracks) {
                    for(Track thisTrack : allTracks) {
                        if(track.equals(thisTrack)) {
                            playList.add(allTracks.indexOf(thisTrack));
                            break;
                        }
                    }
                }
                trackNumber = position;
                player.playSong(playList.get(trackNumber));
                MyArrayAdapter.selectedItemPosition = position;
            }
        });
    }

    public void playNextTrack(View view) {
        player.nextTrack();
    }

    // process back button
    @Override
    public void onBackPressed() {
        //Log.d(TAG, String.valueOf(listLevel));
        if(listLevel == 3) {
            //Log.d(TAG, "3");
            listLevel--;
            readyToEnd = false;
            showAlbums(artists.get(artistId).getAlbums());
        } else if(listLevel == 2) {
            //Log.d(TAG, "2");
            listLevel--;
            readyToEnd = false;
            showArtists();
        } else if(listLevel == 1) {
            //Log.d(TAG, "1");
            readyToEnd = true;
        }

        if(readyToEnd) this.finishAffinity();
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
                mediaPlayer.seekTo(mediaPlayer.getCurrentPosition() + 10000);
                break;
        }
    }

    // top right menu
    public  boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, 1, 0, "path to music");
        menu.add(0, 2, 0, "show incorrect tracks");
        return super.onCreateOptionsMenu(menu);
    }
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 1: // path to music
                player.editPath();
                //return super.onOptionsItemSelected(item);
                break;
            case 2: // show incorrect tracks
                Intent intent = new Intent(this, ListActivity.class);
                intent.putExtra("wrongSongs", wrongSongs);
                startActivity(intent);

                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d(TAG, "permission granted by request");
        player.getMediaFiles();
        Log.d(TAG, String.valueOf(allTracks.size()));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isPlaying = false;
        InOut.getInstance().savePath(this, tempPath);

        if(audioManager != null) {
            audioManager.abandonAudioFocus(afChangeListener);
        }

        player.reset();

        //helper = null;
        Log.d(TAG, "finish in MainActivity");
    }
}
