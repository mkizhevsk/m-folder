package com.mk.m_folder.media;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Message;
import android.provider.DocumentsContract;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.mk.m_folder.R;
import com.mk.m_folder.data.InOut;
import com.mk.m_folder.data.entity.Artist;
import com.mk.m_folder.data.entity.Track;
import com.mk.m_folder.thread.PlayProgressRunnable;
import com.mk.m_folder.thread.TracksRunnable;
import com.mk.m_folder.ui.MainActivity;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Player {

    private static final String TAG = "Player";
    private static final String PAUSE_TEXT = "pause";
    private static final String PLAY_TEXT = "play";

    public static List<File> properFiles;
    public static List<Track> allTracks;
    public static List<Integer> playList;
    public static List<Artist> artists;
    public static int trackNumber = 0;
    public static MediaPlayer mediaPlayer;
    public static Track currentTrack;

    private final Context context;
    private MediaMetadataRetriever mmr;
    private Thread tracksThread;
    public static boolean isPlaying;
    private boolean pause;
    private String tempPath;

    private ImageView coverImageView;
    private TextView songTextView;
    private TextView artistTextView;
    private TextView albumTextView;
    private SeekBar playAudioProgress;
    private Button playPause;

    private AudioFocusManager audioFocusManager;
    private MediaSessionManager mediaSessionManager;

    private static final int REQUEST_CODE_OPEN_DIRECTORY = 1;

    public Player(Context context) {
        this.context = context;
        this.mmr = new MediaMetadataRetriever();
        this.tempPath = "/storage/5E08-92B8/Music2";
        Player.isPlaying = false;
        this.pause = false;
    }

    public void getMediaFiles(String path, List<Track> dbTracks) {
        Log.d(TAG, "start Player getMediaFiles()");
        try {
            tempPath = path;
            initializeMediaLists();

            properFiles = FileUtils.initializeProperFiles(tempPath);
            if (!properFiles.isEmpty()) {
                Collections.shuffle(properFiles);

                Track firstTrack = InOut.getInstance().getTrackFromFile(properFiles.get(0), mmr);
                allTracks.add(firstTrack);
                playList.add(0);
                startPlayer();

                properFiles.remove(0);
                List<File> existedFiles = FileUtils.clearProperFilesAndGetExistedFiles(dbTracks, properFiles);
                FileUtils.addExistedTracksToAllTracks(dbTracks, existedFiles, firstTrack, allTracks, playList);

                tracksThread = new Thread(new TracksRunnable());
                tracksThread.start();
            } else {
                editPath();
            }
        } catch (Exception e) {
            Log.e(TAG, "Player getMediaFiles media exception: " + e.getMessage(), e);
            editPath();
        }
    }

    private void initializeMediaLists() {
        allTracks = new ArrayList<>();
        playList = new ArrayList<>();
    }

    // start player
    public void startPlayer() {
        Log.d(TAG, "start Player startPlayer()");
        initializeUIComponents();

        mediaPlayer = new MediaPlayer();
        mediaSessionManager = new MediaSessionManager(context, this);
        audioFocusManager = new AudioFocusManager(context, mediaPlayer);

        playAudioProgress.setVisibility(ProgressBar.VISIBLE);
        playSong(0);
    }

    private void initializeUIComponents() {
        MainActivity activity = (MainActivity) context;
        coverImageView = activity.findViewById(R.id.coverImage);
        songTextView = activity.findViewById(R.id.textSong);
        artistTextView = activity.findViewById(R.id.textArtist);
        albumTextView = activity.findViewById(R.id.textAlbum);
        playAudioProgress = activity.findViewById(R.id.play_audio_seek_bar);
        playPause = activity.findViewById(R.id.btnPlayPause);
    }

    // play song by track index
    public void playSong(int playListIndex) {
        Log.d(TAG, "playSong " + playListIndex);

        if(mediaPlayer == null){
            mediaPlayer = new MediaPlayer();
        }else{
            mediaPlayer.reset();
        }

        isPlaying = true;

        try {
            initializeMediaPlayer();
            currentTrack = allTracks.get(playListIndex);

            mediaPlayer.setDataSource(currentTrack.getFilePath());
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.prepare();
            mediaPlayer.start();

            updateUIWithTrackDetails();
            setupMediaPlayerListeners();

            pause = false;
            playPause.setText(PAUSE_TEXT);

            new Thread(new PlayProgressRunnable()).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initializeMediaPlayer() {
        if (mediaPlayer == null) {
            mediaPlayer = new MediaPlayer();
        } else {
            mediaPlayer.reset();
        }
    }

    private void updateUIWithTrackDetails() {
        songTextView.setText(currentTrack.getName());
        artistTextView.setText(currentTrack.getArtistName());
        albumTextView.setText(currentTrack.getAlbumName());

        mmr.setDataSource(currentTrack.getFilePath());
        byte[] data = mmr.getEmbeddedPicture();
        if (data != null) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
            coverImageView.setImageBitmap(bitmap);
        } else {
            coverImageView.setImageResource(R.drawable.default_cover);
        }

        playAudioProgress.setMax(mediaPlayer.getDuration() / 1000);
    }

    private void setupMediaPlayerListeners() {
        mediaPlayer.setOnCompletionListener(mp -> nextTrack());
    }

    // playPause
    public void playPause() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            playPause.setText(PLAY_TEXT);
            pause = true;
        } else {
            mediaPlayer.start();
            playPause.setText(PAUSE_TEXT);
            pause = false;
        }
    }

    // previous track
    public void previousTrack() {
        if(trackNumber > 0) {
            trackNumber--;
            playSong(playList.get(trackNumber));
        }
    }

    // next track
    public void nextTrack() {
        Log.d(TAG, "start next");
        if(!pause) {
            trackNumber++;

            if(trackNumber > (playList.size() - 1))
                resetPlayList();

            playSong(playList.get(trackNumber));
        }
    }

    private void resetPlayList() {
        playList.clear();
        for(Track track : allTracks) {
            playList.add(allTracks.indexOf(track));
        }
        trackNumber = allTracks.size()/3;
    }

    public void fastForward() {
        mediaPlayer.seekTo(mediaPlayer.getCurrentPosition() + 10000);
    }

    public void editPath() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        ((MainActivity) context).startActivityForResult(intent, REQUEST_CODE_OPEN_DIRECTORY);
    }

    public void handleActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == REQUEST_CODE_OPEN_DIRECTORY && resultCode == AppCompatActivity.RESULT_OK) {
            if (data != null) {
                Uri uri = data.getData();
                if (uri != null) {
                    // Get the document ID from the URI
                    String documentId = DocumentsContract.getTreeDocumentId(uri);

                    // Log the document ID for debugging purposes
                    Log.d(TAG, "Document ID: " + documentId);

                    // Map the logical document ID to the physical path
                    if (documentId.startsWith("primary:")) {
                        tempPath = "/storage/emulated/0/" + documentId.substring(8);
                    } else {
                        // Handle other cases if necessary
                        Log.e(TAG, "Unsupported storage type");
                        return;
                    }

                    // Log the constructed path
                    Log.d(TAG, "Constructed path: " + tempPath);

                    MainActivity.saveSettingsHandler.sendMessage(new Message());

                    // Load media files if there are no existing proper files
                    if (properFiles == null || properFiles.isEmpty()) {
                        getMediaFiles(tempPath, new ArrayList<>());
                    }
                }
            }
        }
    }

    public void reset() {
        isPlaying = false;
        trackNumber = 0;

        if (mediaPlayer != null) {
            try {
                mediaPlayer.stop();
                mediaPlayer.release();
                mediaPlayer = null;

                mediaSessionManager.release();
                audioFocusManager.abandonAudioFocus();

                TracksRunnable.running = false;
                tracksThread.interrupt();
                Log.d(TAG, "thread interrupt success!");
                Log.d(TAG, "finish reset");
            } catch (Exception e) {
                Log.d(TAG, "reset failure");
                e.printStackTrace();
            }
        } else {
            Log.d(TAG, "null");
        }
    }

    public String getTempPath() {
        return tempPath;
    }

    public void setTempPath(String tempPath) {
        this.tempPath = tempPath;
    }
}
