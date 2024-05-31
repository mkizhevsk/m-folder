package com.mk.m_folder.ui;

import static com.mk.m_folder.data.Player.currentTrack;

import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.mk.m_folder.util.Helper;
import com.mk.m_folder.data.Player;

import java.util.Locale;

public class OptionsMenuHandler {

    private static final String TAG = "OptionsMenuHandler";
    private final MainActivity mainActivity;
    private final Player player;

    public OptionsMenuHandler(MainActivity mainActivity, Player player) {
        this.mainActivity = mainActivity;
        this.player = player;
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, 1, 0, "path to music");
        menu.add(0, 2, 0, "track info");
        menu.add(0, 3, 0, "delete track");
        menu.add(0, 4, 0, "show deleted tracks");
        menu.add(0, 5, 0, "clear deleted tracks");
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 1: // path to music
                mainActivity.player.editPath();
                break;
            case 2: // track info
                String trackInfo = currentTrack.getFilePath();
                Log.d(TAG, trackInfo);
                Intent intent = new Intent(mainActivity, ListActivity.class);
                intent.putExtra("content", trackInfo);
                mainActivity.startActivity(intent);
                break;
            case 3:
                deleteTrack();
                break;
            case 4:
                String deletedTracksInfo = Helper.getDeletedTracksInfo(mainActivity.baseService.getDeletions());
                Log.d(TAG, deletedTracksInfo);
                Intent deletedIntent = new Intent(mainActivity, ListActivity.class);
                deletedIntent.putExtra("content", deletedTracksInfo);
                mainActivity.startActivity(deletedIntent);
                break;
            case 5:
                int clearedDeletions = mainActivity.baseService.clearDeletions();
                Toast.makeText(mainActivity, String.format(Locale.ENGLISH,
                                "%d deleted tracks was cleared",
                                clearedDeletions),
                        Toast.LENGTH_LONG).show();
                break;
        }
        return true;
    }

    private void deleteTrack() {
        Log.d(TAG, "delete");
        mainActivity.baseService.insertDeletion(currentTrack.getName(),
                currentTrack.getArtistName(),
                currentTrack.getAlbumName(),
                currentTrack.getFilePath());
        mainActivity.player.nextTrack();
    }
}

