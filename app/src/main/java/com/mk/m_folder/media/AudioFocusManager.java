package com.mk.m_folder.media;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.util.Log;

public class AudioFocusManager {

    private static final String TAG = "AudioFocusManager";
    private AudioManager audioManager;
    private AudioManager.OnAudioFocusChangeListener afChangeListener;

    public AudioFocusManager(Context context, MediaPlayer mediaPlayer) {
        this.audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        setupAudioFocus(mediaPlayer);
    }

    private void setupAudioFocus(MediaPlayer mediaPlayer) {
        afChangeListener = focusChange -> {
            switch (focusChange) {
                case AudioManager.AUDIOFOCUS_LOSS:
                    Log.d(TAG, "AUDIOFOCUS_LOSS");
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                    Log.d(TAG, "AUDIOFOCUS_LOSS_TRANSIENT");
                    mediaPlayer.pause();
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                    Log.d(TAG, "AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK");
                    break;
                case AudioManager.AUDIOFOCUS_GAIN:
                    Log.d(TAG, "AUDIOFOCUS_GAIN");
                    mediaPlayer.start();
                    break;
                default:
                    break;
            }
        };

        int result = audioManager.requestAudioFocus(afChangeListener,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN);

        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            Log.d(TAG, "AUDIOFOCUS_REQUEST_GRANTED");
        }
    }

    public void abandonAudioFocus() {
        audioManager.abandonAudioFocus(afChangeListener);
        Log.d(TAG, "Audio focus abandoned");
    }

//    public void volumeUp() {
//        audioManager.adjustVolume(AudioManager.ADJUST_RAISE, AudioManager.FLAG_PLAY_SOUND);
//    }
//
//    public void volumeDown() {
//        audioManager.adjustVolume(AudioManager.ADJUST_LOWER, AudioManager.FLAG_PLAY_SOUND);
//    }
}
