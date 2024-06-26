package com.mk.m_folder.data;

import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.util.Log;
import android.webkit.MimeTypeMap;

import com.mk.m_folder.data.entity.Album;
import com.mk.m_folder.data.entity.Artist;
import com.mk.m_folder.data.entity.Track;
import com.mk.m_folder.util.Helper;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class InOut {

    public static final InOut ourInstance = new InOut();

    public static InOut getInstance() {
        return ourInstance;
    }

    private List<File> properFiles;
    private List<File> otherFiles;

    private static final String TAG = "MainActivity";

    public InOut() {
    }

    public Track getTrackFromFile(File file, MediaMetadataRetriever mmr) {

        Track track = new Track(file);
        mmr.setDataSource(file.getAbsolutePath());

        track.setName(getTrackName(mmr, file));
        track.setArtistName(getArtistName(mmr));
        track.setAlbumName(getAlbumName(mmr));
        track.setNumber(getTrackNumber(mmr));
        track.setFilePath(file.getAbsolutePath());

        return track;
    }

    private String getTrackName(MediaMetadataRetriever mmr, File file) {
        String result;
        String trackName = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
        if (trackName != null && !trackName.isEmpty()) {
            result = trackName;
        } else {
            result = Helper.disableExtension(file.getName()) != null ? Helper.disableExtension(file.getName()) : "неизвестная композиция";
        }
        return result;
    }

    private String getArtistName(MediaMetadataRetriever mmr) {
        return mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST) != null
                ? mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)
                : "неизвестный артист";
    }

    private String getAlbumName(MediaMetadataRetriever mmr) {
        return mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM) != null
                ? mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM)
                : "разное";
    }

    private int getTrackNumber(MediaMetadataRetriever mmr) {
        int number = 0;
        String stringNumber = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_CD_TRACK_NUMBER);
        try {
            String[] result = stringNumber.split("/");
            number = Integer.parseInt(result[0]);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return number;
    }

    public List<Artist> getArtists(List<Track> tracks) {
        Log.d(TAG, "start InOut getArtists()");
        HashSet<String> artistNames = new HashSet<>();
        tracks.forEach(track -> artistNames.add(track.getArtistName()));

        ArrayList<Artist> artists = new ArrayList<>();
        for(String artistName : artistNames) {
            //Log.d(TAG, artistName);
            HashSet<String> albumNames = new HashSet<>();
            for(Track track : tracks) {
                if(track.getArtistName().equals(artistName)) {
                    albumNames.add(track.getAlbumName());
                }
            }

            ArrayList<Album> albums = new ArrayList<>();
            albumNames.forEach(albumName -> albums.add(getAlbum(albumName, tracks, artistName)));

            artists.add(new Artist(artistName, albums));
        }
        Log.d(TAG, "artists: " + artists.size());
        return artists;
    }

    private Album getAlbum(String albumName, List<Track> tracks, String artistName) {
            List<Track> albumTracks = new ArrayList<>();
            for(Track track : tracks) {
                if(track.getArtistName().equals(artistName) && track.getAlbumName().equals(albumName)) {
                    albumTracks.add(track);
                }
            }

            Map<String, Track> cleanMap = new LinkedHashMap<>();
            for (int i = 0; i < albumTracks.size(); i++) {
                cleanMap.put(albumTracks.get(i).getName(), albumTracks.get(i));
            }
            List<Track> cleanAlbumTracks = new ArrayList<>(cleanMap.values());

            return new Album(albumName, cleanAlbumTracks);
    }

    public StorageFiles getStorageFiles(String directoryName) {
        Log.d(TAG, "start InOut getSongs()");
        properFiles = new ArrayList<>();
        otherFiles = new ArrayList<>();

        if(getFiles(directoryName)) {
            return new StorageFiles(properFiles, otherFiles);
        }

        return null;
    }

    private boolean getFiles(String directoryName) {
        File directory = new File(directoryName);
        File[] fList = directory.listFiles();
        if(fList != null) {
            for (File file : fList) {
                if (file.isFile()) {
                    Uri uriFile = Uri.fromFile(file);
                    String fileExt = MimeTypeMap.getFileExtensionFromUrl(uriFile.toString());
                    if(fileExt.equals("mp3") || fileExt.equals("m4a")) {
                        properFiles.add(file);
                    } else {
                        String filePath = file.getAbsolutePath();
                        int strLength = filePath.lastIndexOf(".");
                        if(strLength > 0) {
                            String tempExt = filePath.substring(strLength + 1).toLowerCase();
                            if(tempExt.equals("mp3") || tempExt.equals("m4a")) {
                                properFiles.add(file);
                                continue;
                            }
                        }
                        otherFiles.add(file);
                    }
                } else if (file.isDirectory()) {
                    getFiles(file.getAbsolutePath());
                }
            }
            return true;
        } else {
            Log.d(TAG, "InOut getFiles() there is no access to the music directory");
            return false;
        }
    }

}
