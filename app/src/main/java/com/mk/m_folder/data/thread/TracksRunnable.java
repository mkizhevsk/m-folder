package com.mk.m_folder.data.thread;

import android.media.MediaMetadataRetriever;
import android.util.Log;

import com.mk.m_folder.MainActivity;
import com.mk.m_folder.data.InOut;
import com.mk.m_folder.data.entity.Track;

import java.io.File;

import static com.mk.m_folder.data.InOut.properFiles;
import static com.mk.m_folder.data.Player.allTracks;
import static com.mk.m_folder.data.Player.playList;
import static com.mk.m_folder.data.Player.artists;

public class TracksRunnable implements Runnable {

    public static boolean running = false;

    private static final String TAG = "MainActivity";

    private MediaMetadataRetriever mmr = new MediaMetadataRetriever();

    public void run() {
        running = true;

        for (File file : properFiles) {
            if (!running) {
                Log.d(TAG, "return from TracksRunnable1");
                return;
            }
            Track tempTrack = new Track(file, mmr);
            allTracks.add(tempTrack);
            playList.add(allTracks.indexOf(tempTrack));
        }
        Log.d(TAG, "allTracks before process: " + allTracks.size());

        /*for (File file : otherFiles) {
            if (!running) {
                Log.d(TAG, "return from TracksRunnable2");
                return;
            }
            if (InOut.getInstance().checkTagInfo(file)) {
                Track tempTrack = new Track(file, mmr);

                wrongSongs += tempTrack.getArtistName() + " - " + tempTrack.getAlbumName() + " - " + tempTrack.getName() + "\n";
                allTracks.add(tempTrack);
                playList.add(allTracks.indexOf(tempTrack));
            }

        }
        Log.d(TAG, "allTracks after process: " + allTracks.size());*/

        artists = InOut.getInstance().getArtists(allTracks);

        MainActivity.inOutHandler.sendEmptyMessage(1);

        Log.d(TAG, "TrackRunnable completed");
    }
}
