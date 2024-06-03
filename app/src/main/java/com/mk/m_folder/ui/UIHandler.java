package com.mk.m_folder.ui;

import static com.mk.m_folder.media.Player.allTracks;
import static com.mk.m_folder.media.Player.artists;
import static com.mk.m_folder.media.Player.playList;
import static com.mk.m_folder.media.Player.trackNumber;

import android.util.Log;
import android.widget.ListView;

import com.mk.m_folder.data.entity.Album;
import com.mk.m_folder.data.entity.Artist;
import com.mk.m_folder.data.entity.Track;
import com.mk.m_folder.media.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class UIHandler {
    private static final String TAG = "UIHandler";

    private MainActivity activity;
    private Player player;
    private ListView lvMain;
    private int listLevel;

    private static final int ALBUM_LEVEL = 2;
    private static final int TRACK_LEVEL = 3;

    public UIHandler(MainActivity activity, Player player, ListView lvMain) {
        this.activity = activity;
        this.player = player;
        this.lvMain = lvMain;
    }

    public void showArtists() {
        String[] stringArtists = new String[artists.size()];
        int i = 0;
        Collections.sort(artists);
        for(Artist artist : artists) {
            stringArtists[i] = artist.getName();
            i++;
        }

        final MyArrayAdapter artistsAdapter = new MyArrayAdapter(activity, stringArtists);
        lvMain.setAdapter(artistsAdapter);
        MyArrayAdapter.selectedItemPosition = 100;

        lvMain.setOnItemClickListener((parent, view, position, id) -> {
            view.setSelected(true);
            Collections.sort(artists.get(position).getAlbums());

            MainActivity.artistId = position;
            showAlbums(artists.get(position).getAlbums());
        });

        lvMain.setLongClickable(true);
        lvMain.setOnItemLongClickListener((parent, view, position, id) -> {
            Log.d(TAG, String.valueOf(position));
            Artist artist = artists.get(position);
            Collections.sort(artist.getAlbums());

            addTracksToPlaylist(artist.getAlbums().stream()
                    .flatMap(album -> album.getSortedTracks().stream())
                    .collect(Collectors.toList()));

            trackNumber = 0;
            player.playSong(playList.get(trackNumber));

            // Update the selected item position
            MyArrayAdapter.selectedItemPosition = position;
            artistsAdapter.notifyDataSetChanged(); // Notify the adapter to refresh the list

            return true;
        });

        listLevel = ALBUM_LEVEL;
    }

    public void showAlbums(final ArrayList<Album> albums) {
        String[] stringAlbums = new String[albums.size()];
        int i = 0;
        for(Album album : albums) {
            stringAlbums[i] = album.getName();
            i++;
        }

        final MyArrayAdapter albumsAdapter = new MyArrayAdapter(activity, stringAlbums);
        lvMain.setAdapter(albumsAdapter);
        MyArrayAdapter.selectedItemPosition = 100;

        lvMain.setOnItemClickListener((parent, view, position, id) -> {
            MainActivity.albumId = position;

            List<Track> albumTracks = albums.get(position).getSortedTracks();
            Collections.sort(albumTracks);
            showSongs(albumTracks);
        });

        lvMain.setLongClickable(true);
        lvMain.setOnItemLongClickListener((parent, view, position, id) -> {
            Log.d(TAG, "showAlbums setOnItemLongClickListener: position = " + position + ", id = " + id);
            playList.clear();
            view.setSelected(true);

            List<Track> albumTracks = albums.get(position).getSortedTracks();
            Collections.sort(albumTracks);

            addTracksToPlaylist(albumTracks);

            trackNumber = 0;
            player.playSong(playList.get(trackNumber));

            return true;
        });

        listLevel = ALBUM_LEVEL;
    }

    public void showSongs(final List<Track> albumTracks) {
        String[] stringSongs = new String[albumTracks.size()];
        int i = 0;
        for (Track track : albumTracks) {
            stringSongs[i] = track.getName();
            i++;
        }

        final MyArrayAdapter songsAdapter = new MyArrayAdapter(activity, stringSongs);
        lvMain.setAdapter(songsAdapter);

        lvMain.setOnItemClickListener((parent, view, position, id) -> {
            MyArrayAdapter.selectedItemPosition = position;

            addTracksToPlaylist(albumTracks);

            trackNumber = position;
            player.playSong(playList.get(trackNumber));

            // Notify the adapter to refresh the list
            songsAdapter.notifyDataSetChanged();
        });

        lvMain.setLongClickable(false);

        listLevel = TRACK_LEVEL;
    }


    private void addTracksToPlaylist(List<Track> albumTracks) {
        playList.clear();
        for (Track track : albumTracks) {
            for (Track thisTrack : allTracks) {
                if (track.equals(thisTrack)) {
                    playList.add(allTracks.indexOf(thisTrack));
                    break;
                }
            }
        }
    }

    public int getListLevel() {
        return listLevel;
    }

    public void setListLevel(int listLevel) {
        this.listLevel = listLevel;
    }
}
