package com.mk.m_folder.data.entity;

import androidx.annotation.NonNull;

import java.util.ArrayList;

public class Artist implements Comparable<Artist> {

    private String name;

    private ArrayList<Album> albums;

    public Artist(String name, ArrayList<Album> albums) {
        this.name = name;
        this.albums = albums;
    }

    @Override
    public int compareTo(@NonNull Artist artist) {
        return this.name.compareTo(artist.getName());
    }

    @Override
    public String toString() {
        return this.name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ArrayList<Album> getAlbums() {
        return albums;
    }

    public void setAlbums(ArrayList<Album> albums) {
        this.albums = albums;
    }

}