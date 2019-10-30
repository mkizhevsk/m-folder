package com.mk.m_folder.data.thread;

import android.bluetooth.BluetoothSocket;
import android.util.Log;
import android.widget.Toast;

import com.mk.m_folder.MainActivity;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

public class InputRunnable implements Runnable {

    private final BluetoothSocket bluetoothSocket;
    private final InputStream inputStream;

    private static final String TAG = "MainActivity";

    public InputRunnable(BluetoothSocket socket) {
        bluetoothSocket = socket;
        InputStream tmpInputStream = null;

        try {
            tmpInputStream = bluetoothSocket.getInputStream();
        } catch (IOException e) {
            Log.d(TAG, "Error occured when creating input stream");
        }

        inputStream = tmpInputStream;
    }

    @Override
    public void run() {
        Log.d(TAG, "start InputRunnable run");

        try {
            DataInputStream in = new DataInputStream(inputStream);
            int imp = in.readInt();
            Log.e(TAG, "Input: " + imp);
            MainActivity.nextTrackHandler.sendEmptyMessage(1);
            closeSocket();
        } catch (IOException e) {
            Log.e(TAG, "Input stream was disconnected", e);
            closeSocket();
        }
    }

    private void closeSocket() {
        try {
            bluetoothSocket.close();
            Log.e(TAG, "bluetoothSocket was closed");
        } catch (IOException e) {
            Log.e(TAG, "Could not close bluetoothSocket", e);
        }
    }
}
