package com.mk.m_folder.media;

import android.content.Context;
import android.content.Intent;
import android.media.session.MediaSession;
import android.util.Log;
import android.view.KeyEvent;

public class MediaSessionManager {

    private static final String TAG = "MediaSessionManager";
    private MediaSession mediaSession;

    public MediaSessionManager(Context context, Player player) {
        initMediaSession(context, player);
    }

    private void initMediaSession(Context context, Player player) {
        mediaSession = new MediaSession(context, "MEDIA_SESSION_TAG");
        mediaSession.setCallback(new MediaSession.Callback() {
            @Override
            public void onPlay() {
                mediaSession.setActive(true);
            }

            @Override
            public boolean onMediaButtonEvent(Intent mediaButtonIntent) {
                Log.d(TAG, "onMediaButtonEvent called: " + mediaButtonIntent);
                KeyEvent keyEvent = mediaButtonIntent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
                if (keyEvent != null && keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
                    handleMediaButtonEvent(keyEvent, player);
                }
                return super.onMediaButtonEvent(mediaButtonIntent);
            }

            @Override
            public void onSkipToNext() {
                Log.d(TAG, "onSkipToNext called (media button pressed)");
                player.nextTrack();
                super.onSkipToNext();
            }
        });

        mediaSession.setFlags(MediaSession.FLAG_HANDLES_MEDIA_BUTTONS |
                MediaSession.FLAG_HANDLES_TRANSPORT_CONTROLS);

        if (!mediaSession.isActive()) {
            mediaSession.setActive(true);
        }
    }

    private void handleMediaButtonEvent(KeyEvent keyEvent, Player player) {
        int keyCode = keyEvent.getKeyCode();
        Log.d(TAG, "onMediaButtonEvent Received command: " + keyCode);

        switch (keyCode) {
            case KeyEvent.KEYCODE_MEDIA_NEXT:
                player.nextTrack();
                break;
            case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
            case KeyEvent.KEYCODE_MEDIA_PAUSE:
            case KeyEvent.KEYCODE_MEDIA_PLAY:
                player.playPause();
                break;
            case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
                player.previousTrack();
                break;
            default:
                break;
        }
    }

    public void release() {
        if (mediaSession != null) {
            mediaSession.release();
        }
    }
}
