package com.mk.m_folder.data.thread;

import android.media.MediaMetadataRetriever;
import android.util.Log;

import com.mk.m_folder.MainActivity;
import com.mk.m_folder.data.InOut;
import com.mk.m_folder.data.entity.Track;

import java.io.File;

import static com.mk.m_folder.data.Player.allTracks;
import static com.mk.m_folder.data.Player.playList;
import static com.mk.m_folder.data.Player.artists;
import static com.mk.m_folder.data.Player.properFiles;

public class TracksRunnable implements Runnable {

    public static boolean running = false;

    private static final String TAG = "MainActivity";

    private MediaMetadataRetriever mmr = new MediaMetadataRetriever();

    public void run() {
        running = true;

        for (File file : properFiles) {
            if (!running) {
                Log.d(TAG, "return from TracksRunnable");
                return;
            }
            Track tempTrack = InOut.getInstance().getTrackFromFile(file, mmr);

            allTracks.add(tempTrack);

            playList.add(allTracks.indexOf(tempTrack));
        }
        Log.d(TAG, "allTracks before process: " + allTracks.size());

        artists = InOut.getInstance().getArtists(allTracks);

        MainActivity.inOutHandler.sendEmptyMessage(1);

        Log.d(TAG, "TrackRunnable completed");
    }
}
