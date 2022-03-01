package com.mk.m_folder.data;

import android.app.Activity;
import android.content.SharedPreferences;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.webkit.MimeTypeMap;

import com.mk.m_folder.data.entity.Album;
import com.mk.m_folder.data.entity.Artist;
import com.mk.m_folder.data.entity.Track;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static android.content.Context.MODE_PRIVATE;

public class InOut {

    public static final InOut ourInstance = new InOut();

    public static InOut getInstance() {
        return ourInstance;
    }

    static public ArrayList<File> properFiles;
    static public ArrayList<File> otherFiles;

    MediaMetadataRetriever mmr = new MediaMetadataRetriever();

    SharedPreferences sPref;
    final String SAVED_PATH = "saved_text";
    public static String tempPath = "/storage/emulated/0/music";
    //public static String tempPath = "/storage/1944-3E26/Android/data/itunes.android.synctunesultimate/files";

    final String DIR_LINES = "Download";
    final String FILENAME_SD = "deleted_songs.txt";

    private static final String TAG = "MainActivity";

    public InOut() {
    }

    public Track getTrackFromFile(File file, MediaMetadataRetriever mmr) {
        Track track = new Track(file);

        mmr.setDataSource(file.getAbsolutePath());
        //Log.d(TAG, file.toString());

        String trackName = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
        if (trackName != null && !trackName.isEmpty()) {
            track.setName(trackName);
        } else {
            track.setName(Helper.disableExtension(file.getName()) != null ? Helper.disableExtension(file.getName()) : "неизвестная композиция");
        }

        track.setArtistName(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST) != null ? mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST) : "неизвестный артист");

        track.setAlbumName(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM) != null ? mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM) : "разное");

        int number = 0;
        String stringNumber = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_CD_TRACK_NUMBER);
        try {
            String[] result = stringNumber.split("/");
            number = Integer.parseInt(result[0]);
        } catch (Exception e) {
            e.printStackTrace();
        }
        track.setNumber(number);

        return track;
    }

    public List<Artist> getArtists(List<Track> tracks) {
        HashSet<String> artistNames = new HashSet<>();
        for(Track track : tracks) {
            artistNames.add(track.getArtistName());
        }

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
            for(String albumName : albumNames) {
                List<Track> albumTracks = new ArrayList<>();
                for(Track track : tracks) {
                    //Log.d(TAG, "  " + track.getName() + " " + track.getAlbumName() + " " + track.getArtistName());
                    if(track.getArtistName().equals(artistName) && track.getAlbumName().equals(albumName)) {
                        albumTracks.add(track);
                    }
                }

                Map<String, Track> cleanMap = new LinkedHashMap<>();
                for (int i = 0; i < albumTracks.size(); i++) {
                    cleanMap.put(albumTracks.get(i).getName(), albumTracks.get(i));
                }
                List<Track> cleanAlbumTracks = new ArrayList<>(cleanMap.values());

                albums.add(new Album(albumName, cleanAlbumTracks));
            }
            artists.add(new Artist(artistName, albums));
        }
        Log.d(TAG, "artists: " + artists.size());
        return artists;
    }

    public void getSongs(String directoryName) {
//        Log.d(TAG, "start");
        properFiles = new ArrayList<>();
        otherFiles = new ArrayList<>();

        getFiles(directoryName);
    }

    private void getFiles(String directoryName) {
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
        }
    }

    public String loadPath(Activity activity) {
        sPref = activity.getPreferences(MODE_PRIVATE);
        String savedText = sPref.getString(SAVED_PATH, "");
        //etText.setText(savedText);
        //Toast.makeText(this, "Text loaded", Toast.LENGTH_SHORT).show();
        return  savedText;
    }

    public void savePath(Activity activity, String path) {
        sPref = activity.getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor editor = sPref.edit();
        editor.putString(SAVED_PATH, path);
        editor.commit();
    }

    public void writeLine(String line) {
        if (!Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {
            Log.d(TAG, "SD-карта не доступна: " + Environment.getExternalStorageState());
            return;
        }

        File sdPath = Environment.getExternalStorageDirectory();
        sdPath = new File(sdPath.getAbsolutePath() + "/" + DIR_LINES);
        sdPath.mkdirs();
        File sdFile = new File(sdPath, FILENAME_SD);

        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(sdFile, true));
            bw.write(line  + "\n");
            bw.close();
            Log.d(TAG, "Файл записан: " + sdFile.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //
    public boolean checkTagInfo(File file) {
        try {
            mmr.setDataSource(file.getAbsolutePath());

            String tempArtistName = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
            String tempSongName = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);

            if(tempArtistName != null && tempSongName != null) {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

}
