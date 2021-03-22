package com.mk.m_folder.data.entity;

import android.media.MediaMetadataRetriever;

import androidx.annotation.NonNull;

import java.io.File;

public class Track implements Comparable<Track> {

    private String name;
    private String artistName;
    private String albumName;

    File file;

    int number;

    private static final String TAG = "Track";

    public Track(File file, MediaMetadataRetriever mmr) {
        mmr.setDataSource(file.getAbsolutePath());

//        this.artistName = names[0];
//        this.albumName = names[1];
//        this.name = names[2];
        this.name = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE) != null ? mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE) : "неизвестная композиция";

        this.artistName = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST) != null ? mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST) : "неизвестный артист";

        this.albumName = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM) != null ? mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM) : "разное";

        this.number = 0;
        String stringNumber = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_CD_TRACK_NUMBER);
        try {
            String[] result = stringNumber.split("/");
            this.number = Integer.parseInt(result[0]);
        } catch (Exception e) {
            e.printStackTrace();
        }
        //Log.d(TAG, stringNumber + " - " + String.valueOf(this.number));
        this.setFile(file);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getArtistName() {
        return artistName;
    }

    public void setArtistName(String artistName) {
        this.artistName = artistName;
    }

    public String getAlbumName() {
        return albumName;
    }

    public void setAlbumName(String albumName) {
        this.albumName = albumName;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    @Override
    public int compareTo(@NonNull Track track) {
        if(this.number == track.number) {
            return 0;
        } else if(this.number > track.getNumber()) {
            return 1;
        }
        return -1;
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof Track)) {
            return false;
        }

        Track that = (Track) other;

        return this.name.equals(that.name)
                && (this.artistName.equals(that.artistName))
                && (this.albumName.equals(that.albumName));
    }

    public String print() {
        return this.artistName + " - " + this.albumName + " - " + this.name + " - " + this.number;
    }
}
