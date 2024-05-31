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

    public static List<File> clearProperFilesAndGetExistedFiles(List<Track> dbTracks, List<File> properFiles) {
        List<File> existedFiles = new ArrayList<>();
        for (Track track : dbTracks) {
            properFiles.stream()
                    .filter(file -> track.getFilePath().equals(file.getAbsolutePath()))
                    .forEach(existedFiles::add);
        }
        return properFiles.stream()
                .filter(file -> !existedFiles.contains(file))
                .collect(Collectors.toList());
    }

    public static void addExistedTracksToAllTracks(List<Track> dbTracks, List<File> existedFiles, Track firstTrack, List<Track> allTracks, List<Integer> playList) {
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
