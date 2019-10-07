package com.mk.m_folder.data.entity;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

public class Album implements Comparable<Album> {

    private String name;

    private List<Track> tracks;

    public Album(String name, List<Track> tracks) {
        this.name = name;
        this.tracks = tracks;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Track> getTracks() {
        return tracks;
    }

    public void setTracks(ArrayList<Track> tracks) {
        this.tracks = tracks;
    }

    @Override
    public int compareTo(@NonNull Album album) {
        return this.name.compareTo(album.getName());
    }
}
