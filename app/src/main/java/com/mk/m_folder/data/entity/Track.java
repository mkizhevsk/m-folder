package com.mk.m_folder.data.entity;

import androidx.annotation.NonNull;

import java.io.File;

public class Track implements Comparable<Track> {

    private String name;
    private String artistName;
    private String albumName;
    private String filePath;

    File file;

    int number;

    public Track(File file) {
        this.file = file;
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

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
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
        if(this.number > 0 && track.getNumber() > 0) {
            if(this.number == track.number) {
                return 0;
            } else if(this.number > track.getNumber()) {
                return 1;
            }
            return -1;
        } else return this.name.compareTo(track.getName());
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
