package com.mk.m_folder.media;

import com.mk.m_folder.data.InOut;
import com.mk.m_folder.data.StorageFiles;
import com.mk.m_folder.data.entity.Track;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class FileUtils {

    private static final String TAG = "FileUtils";

    public static List<File> initializeProperFiles(String path) {
        StorageFiles storageFiles = InOut.getInstance().getStorageFiles(path);
        if (storageFiles != null && !storageFiles.getProperFiles().isEmpty()) {
            return storageFiles.getProperFiles();
        }
        return new ArrayList<>();
    }

    public static List<File> getExistedFilesAndClearProperFiles(List<Track> dbTracks, List<File> properFiles) {
        // Find and collect existed files
        List<File> existedFiles = dbTracks.stream()
                .flatMap(track -> properFiles.stream()
                        .filter(file -> track.getFilePath().equals(file.getAbsolutePath())))
                .collect(Collectors.toList());

        // Remove existed files from properFiles
        properFiles.removeAll(existedFiles);

        return existedFiles;
    }

    public static void addExistedTracksToAllTracksAndPlayList(List<Track> dbTracks, List<File> existedFiles, Track firstTrack, List<Track> allTracks, List<Integer> playList) {
        Collections.shuffle(existedFiles);
        for (File file : existedFiles) {
            dbTracks.stream()
                    .filter(track -> !firstTrack.getFilePath().equals(track.getFilePath()))
                    .filter(track -> file.getAbsolutePath().equals(track.getFilePath()))
                    .forEach(track -> {
                        allTracks.add(track);
                        playList.add(allTracks.indexOf(track));
                    });
        }
    }
}
