package com.mk.m_folder.thread;

import static com.mk.m_folder.media.Player.isPlaying;
import static com.mk.m_folder.ui.MainActivity.audioProgressHandler;

import android.os.Message;
import android.util.Log;

public class PlayProgressRunnable implements Runnable {

    public static boolean running = false;
    private static final String TAG = "MainActivity";
    private static final int UPDATE_AUDIO_PROGRESS_BAR = 3;

    public void run() {
        running = true;

        try {
            while (isPlaying) {
                if (audioProgressHandler != null) {
                    // Send update audio player progress message to main thread message queue
                    Message msg = new Message();
                    msg.what = UPDATE_AUDIO_PROGRESS_BAR;
                    audioProgressHandler.sendMessage(msg);

                    Thread.sleep(1000);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }

    }

}
