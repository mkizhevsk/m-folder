package com.mk.m_folder.data;

import static android.content.Context.AUDIO_SERVICE;
import static com.mk.m_folder.data.InOut.otherFiles;
import static com.mk.m_folder.data.InOut.properFiles;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.media.session.MediaSession;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import com.mk.m_folder.MainActivity;
import com.mk.m_folder.R;
import com.mk.m_folder.data.entity.Artist;
import com.mk.m_folder.data.entity.Track;
import com.mk.m_folder.data.thread.PlayProgressRunnable;
import com.mk.m_folder.data.thread.TracksRunnable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Player {

    public static List<Track> allTracks;
    public static List<Integer> playList;
    public static List<Artist> artists;
    public static int trackNumber = 0;

    private static final String TAG = "MainActivity";

    public static MediaPlayer mediaPlayer;

    private AudioManager audioManager;
    private MediaSession mediaSession;
    private AudioManager.OnAudioFocusChangeListener afChangeListener;

    private MediaMetadataRetriever mmr = new MediaMetadataRetriever();

    private final Context context;

    public static String tempPath = "/storage/5E08-92B8/Music2";

    public static boolean isPlaying;
    private static boolean pause = false;

    private Thread tracksThread;

    private ImageView coverImageView;
    private TextView songTextView;
    private TextView artistTextView;
    private TextView albumTextView;
    private SeekBar playAudioProgress;
    private Button playPause;

    public static Track currentTrack;

    public Player(Context context) {
        this.context = context;
    }

    public void getMediaFiles(String path) {
        Log.d(TAG, "start Player getMediaFiles()");
        try {
            tempPath = path;

            allTracks = new ArrayList<>();
            playList = new ArrayList<>();
            mmr = new MediaMetadataRetriever();

            if(InOut.getInstance().getSongs(tempPath)) {
                Log.d(TAG, "Player getMediaFiles proper: " + properFiles.size() + ", other: " + otherFiles.size() + "; " + (properFiles.size() + otherFiles.size()) );
                Collections.shuffle(properFiles);

                allTracks.add(InOut.getInstance().getTrackFromFile(properFiles.get(0), mmr));

                playList.add(0);

                startPlayer();
            } else {
                editPath();
            }
        } catch (Exception e) {
            Log.d(TAG, "Player getMediaFiles media exception: " + e.toString());
            e.printStackTrace();
            editPath();
        }
    }

    // start player
    public void startPlayer() {
        Log.d(TAG, "start Player startPlayer()");

        coverImageView = ((MainActivity)context).findViewById(R.id.coverImage);
        songTextView = ((MainActivity)context).findViewById(R.id.textSong);
        artistTextView = ((MainActivity)context).findViewById(R.id.textArtist);
        albumTextView = ((MainActivity)context).findViewById(R.id.textAlbum);
        playAudioProgress = ((MainActivity)context).findViewById(R.id.play_audio_seek_bar);
        playPause = ((MainActivity)context).findViewById(R.id.btnPlayPause);

        audioManager = (AudioManager) context.getSystemService(AUDIO_SERVICE);
        initMediaSession();

        playAudioProgress.setVisibility(ProgressBar.VISIBLE);

        // начинаем играть первый трек
        playSong(0);

        // удаляем первый трек из оставшихся для обработки остальных файлов
        properFiles.remove(0);

        // audioFocus
        afChangeListener =
                new AudioManager.OnAudioFocusChangeListener() {
                    public void onAudioFocusChange(int focusChange) {
                        if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
                            // Permanent loss of audio focus
                            Log.d(TAG,"AUDIOFOCUS_LOSS");
                        } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) {
                            Log.d(TAG,"AUDIOFOCUS_LOSS_TRANSIENT");
                            // Pause playback
                            mediaPlayer.pause();
                        } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK) {
                            // Lower the volume, keep playing
                            Log.d(TAG,"AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK");
                        } else if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
                            // Your app has been granted audio focus again
                            // Raise volume to normal, restart playback if necessary
                            Log.d(TAG,"AUDIOFOCUS_GAIN");
                            if(!pause) {
                                mediaPlayer.start();
                            }
                        }
                    }
                };

        int result = audioManager.requestAudioFocus(afChangeListener,
                // Use the music stream.
                AudioManager.STREAM_MUSIC,
                // Request permanent focus.
                AudioManager.AUDIOFOCUS_GAIN);

        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            Log.d(TAG, "AUDIOFOCUS_REQUEST_GRANTED");
        }

        tracksThread = new Thread(new TracksRunnable());
        tracksThread.start();
    }

    // play song by track index
    public void playSong(int trackIndex) {
        if(mediaPlayer == null){
            mediaPlayer = new MediaPlayer();
        }else{
            mediaPlayer.reset();
        }

        isPlaying = true;

        try {
            currentTrack = allTracks.get(trackIndex);

            mediaPlayer.setDataSource(currentTrack.getFile().getAbsolutePath());
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.prepare();
            mediaPlayer.start();

            songTextView.setText(currentTrack.getName());
            artistTextView.setText(currentTrack.getArtistName());
            albumTextView.setText(currentTrack.getAlbumName());

            mmr.setDataSource(currentTrack.getFile().getAbsolutePath());
            byte [] data = mmr.getEmbeddedPicture();
            if(data != null) {
                Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                coverImageView.setImageBitmap(bitmap); //associated cover art in bitmap
            } else {
                coverImageView.setImageResource(R.drawable.default_cover);
            }

            playAudioProgress.setMax(mediaPlayer.getDuration()/1000);

            pause = false;

            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    nextTrack();
                }
            });

            Thread playProgressThread = new Thread(new PlayProgressRunnable());
            playProgressThread.start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // playPause
    public void playPause() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            playPause.setText("play");
            pause = true;
        } else {
            mediaPlayer.start();
            playPause.setText("pause");
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

            if(trackNumber > (playList.size() - 1)) {
                playList.clear();
                for(Track track : allTracks) {
                    playList.add(allTracks.indexOf(track));
                }
                trackNumber = allTracks.size()/3;
            }

            playSong(playList.get(trackNumber));
        }
    }

    public void fastForward() {
        mediaPlayer.seekTo(mediaPlayer.getCurrentPosition() + 10000);
    }

    public void volumeUp() {
        audioManager.adjustVolume(AudioManager.ADJUST_RAISE, AudioManager.FLAG_PLAY_SOUND);
//        int musicVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
//        Log.d(TAG, "volume: " + musicVolume);
//        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 2,);
    }

    public void volumeDown() {
        audioManager.adjustVolume(AudioManager.ADJUST_LOWER, AudioManager.FLAG_PLAY_SOUND);
    }

    // bluetooth events etc.
    private void initMediaSession() {
        mediaSession = new MediaSession(context, "MEDIA_SESSION_TAG");
        mediaSession.setCallback(new MediaSession.Callback() {
            @Override
            public void onPlay() {//
                mediaSession.setActive(true);
            }

            @Override
            public boolean onMediaButtonEvent(Intent mediaButtonIntent) {
                Log.d(TAG, "onMediaButtonEvent called: " + mediaButtonIntent);
                KeyEvent keyEvent = mediaButtonIntent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
                if (keyEvent != null && keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
                    int keyCode = keyEvent.getKeyCode();
                    Log.d(TAG, "onMediaButtonEvent Received command: " + keyCode);

                    if(keyCode == 87) {
                        nextTrack();
                    } else if(keyCode == 127 || keyCode == 126) {
                        playPause();
                    } else if(keyCode == 88) {
                        previousTrack();
                    }
                }
                return super.onMediaButtonEvent(mediaButtonIntent);
            }

            @Override
            public void onSkipToNext() {
                Log.d(TAG, "onSkipToNext called (media button pressed)");

                super.onSkipToNext();
            }
        });

        mediaSession.setFlags(MediaSession.FLAG_HANDLES_MEDIA_BUTTONS |
                MediaSession.FLAG_HANDLES_TRANSPORT_CONTROLS);

        if (!mediaSession.isActive()) {
            mediaSession.setActive(true);
        }
    }

    // edit path
    public void editPath() {
        LayoutInflater pi = LayoutInflater.from(context);
        View pathView = pi.inflate(R.layout.path, null);
        AlertDialog.Builder newPathDialogBuilder = new AlertDialog.Builder(context);
        newPathDialogBuilder.setView(pathView);
        final EditText pathInput = pathView.findViewById(R.id.input_path);
        pathInput.setText(tempPath);
        newPathDialogBuilder
                .setCancelable(false)
                .setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                tempPath = pathInput.getText().toString();
                                Log.d(TAG, "from input: " + tempPath);
                                if(properFiles.isEmpty()) getMediaFiles(tempPath);
                            }
                        })
                .setNegativeButton("Отмена",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
        AlertDialog createDialog = newPathDialogBuilder.create();
        createDialog.show();
    }

    // reset
    public void reset() {
        if(mediaPlayer != null) {
            try {
                isPlaying = false;

                mediaPlayer.stop();
                mediaPlayer.release();
                mediaPlayer = null;

                if(mediaSession != null) {
                    mediaSession.release();
                }
                Log.d(TAG, "reset mediaPlayer success!");

                if(audioManager != null) {
                    audioManager.abandonAudioFocus(afChangeListener);
                    Log.d(TAG, "abandon AudioFocus");
                }

                TracksRunnable.running = false;
                try {
                    tracksThread.interrupt();
                    Log.d(TAG, "thread interrupt success!");
                } catch (Exception e) {
                    Log.d(TAG, "thread interrupt failure");
                }
                Log.d(TAG, "finish reset");
            } catch (Exception e) {
                Log.d(TAG, "reset failure");
                e.printStackTrace();
            }
        } else {
            Log.d(TAG, "null");
        }
    }
}
