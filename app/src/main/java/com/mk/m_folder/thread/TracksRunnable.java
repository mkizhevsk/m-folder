package com.mk.m_folder.thread;

import static com.mk.m_folder.data.Player.allTracks;
import static com.mk.m_folder.data.Player.artists;
import static com.mk.m_folder.data.Player.playList;
import static com.mk.m_folder.data.Player.properFiles;

import android.media.MediaMetadataRetriever;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;

import com.mk.m_folder.ui.MainActivity;
import com.mk.m_folder.data.InOut;
import com.mk.m_folder.data.entity.Track;

import java.io.File;

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

            Track track = InOut.getInstance().getTrackFromFile(file, mmr);
            if(track != null) {
                allTracks.add(track);
                playList.add(allTracks.indexOf(track));

                MainActivity.trackInfoHandler.sendMessage(getTrackInfoHandlerMessage(track));
            }
        }
        Log.d(TAG, "allTracks before process: " + allTracks.size());

        artists = InOut.getInstance().getArtists(allTracks);

        MainActivity.inOutHandler.sendEmptyMessage(1);

        Log.d(TAG, "TrackRunnable completed");
    }

    private Message getTrackInfoHandlerMessage(Track track) {
        Bundle bundle = new Bundle();
        bundle.putStringArray("trackInfo", new String[] {track.getName(), track.getArtistName(), track.getAlbumName(), track.getFilePath()});

        Message message = new Message();
        message.setData(bundle);

        return message;
    }
}
