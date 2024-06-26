package com.mk.m_folder.ui;

import static com.mk.m_folder.media.Player.currentTrack;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.mk.m_folder.R;
import com.mk.m_folder.util.Helper;

import java.util.Locale;

public class OptionsMenuHandler {

    private static final String TAG = "OptionsMenuHandler";

    private Context context;
    private MainActivity mainActivity;

    public OptionsMenuHandler(Context context, MainActivity mainActivity) {
        this.context = context;
        this.mainActivity = mainActivity;
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, 1, 0, context.getString(R.string.menu_path_to_music));
        menu.add(0, 2, 0, context.getString(R.string.menu_edit_path));
        menu.add(0, 3, 0, context.getString(R.string.menu_track_info));
        menu.add(0, 4, 0, context.getString(R.string.menu_delete_track));
        menu.add(0, 5, 0, context.getString(R.string.menu_deleted_tracks_info));
        menu.add(0, 6, 0, context.getString(R.string.menu_clear_deletions));
        menu.add(0, 7, 0, context.getString(R.string.menu_exit));
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 1: // path to music
                mainActivity.player.initPath();
                break;
            case 2: // edit path
                mainActivity.player.editPath();
                break;
            case 3: // track info
                String trackInfo = currentTrack.getFilePath();
                Log.d(TAG, trackInfo);
                Intent intent = new Intent(mainActivity, ListActivity.class);
                intent.putExtra("content", trackInfo);
                mainActivity.startActivity(intent);
                break;
            case 4:
                deleteTrack();
                break;
            case 5:
                String deletedTracksInfo = Helper.getDeletedTracksInfo(mainActivity.baseService.getDeletions());
                Log.d(TAG, deletedTracksInfo);
                Intent deletedIntent = new Intent(mainActivity, ListActivity.class);
                deletedIntent.putExtra("content", deletedTracksInfo);
                mainActivity.startActivity(deletedIntent);
                break;
            case 6:
                int clearedDeletions = mainActivity.baseService.clearDeletions();
                Toast.makeText(mainActivity, String.format(Locale.ENGLISH,
                                "%d deleted tracks was cleared",
                                clearedDeletions),
                        Toast.LENGTH_LONG).show();
                break;
            case 7:
                showExitConfirmationDialog();
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

    public void showExitConfirmationDialog() {
        new AlertDialog.Builder(context)
                .setTitle(R.string.exit_confirmation_title)
                .setMessage(R.string.exit_confirmation_message)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        mainActivity.finishAffinity();
                    }
                })
                .setNegativeButton(android.R.string.no, null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }
}

