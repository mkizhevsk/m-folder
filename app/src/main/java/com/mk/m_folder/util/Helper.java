package com.mk.m_folder.util;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.mk.m_folder.data.entity.Deletion;

import java.util.ArrayList;
import java.util.List;

public class Helper {

    public static String getDeletedTracksInfo(List<Deletion> deletions) {
        String deletedTracksInfo = "";
        for(Deletion deletion : deletions) {
            StringBuilder sb = new StringBuilder();
            if(deletedTracksInfo.isEmpty()) {
                deletedTracksInfo = deletion.getFullInfo();
            } else {
                deletedTracksInfo = sb.append(deletedTracksInfo).append(getNewLine()).append(getNewLine()).append(deletion.getFullInfo()).toString();
            }
        }
        return deletedTracksInfo;
    }

    private static String getNewLine() {
        return System.getProperty("line.separator");
    }

    public static String disableExtension (String str) {
        // Handle null case specially.
        if (str == null) return null;

        // Get position of last '.'.
        int pos = str.lastIndexOf(".");

        // If there wasn't any '.' just return the string as is.
        if (pos == -1) return str;

        // Otherwise return the string, up to the dot.
        return str.substring(0, pos);
    }

    public static boolean checkPermissions(Context context, Activity mainActivity) {
        String[] permissions = new String[]{
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
        };

        int result;
        List<String> listPermissionsNeeded = new ArrayList<>();
        for (String p : permissions) {
            result = ContextCompat.checkSelfPermission(context, p);
            if (result != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(p);
            }
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(mainActivity, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), 100);
            return false;
        }
        return true;
    }

}
