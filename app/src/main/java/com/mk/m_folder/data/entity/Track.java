package com.mk.m_folder.data.entity;

import androidx.annotation.NonNull;

import java.io.File;
import java.util.Objects;

public class Track implements Comparable<Track> {

    private String name;
    private String artistName;
    private String albumName;
    private String filePath;

    private File file;

    private int number;

    public Track(File file) {
        this.file = file;
    }

    public Track(String name, String artistName, String albumName, String filePath) {
        this.name = name;
        this.artistName = artistName;
        this.albumName = albumName;
        this.filePath = filePath;
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

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Track track = (Track) o;
        return name.equals(track.name) && Objects.equals(artistName, track.artistName) && Objects.equals(filePath, track.filePath);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, artistName, filePath);
    }
}
