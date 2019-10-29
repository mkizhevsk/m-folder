package com.mk.m_folder.data.thread;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import com.mk.m_folder.MainActivity;

import java.io.IOException;
import java.util.UUID;

public class BluetoothClientRunnable implements Runnable {

    private final BluetoothSocket bluetoothSocket;

    private static final String TAG = "MainActivity";

    public BluetoothClientRunnable(BluetoothDevice device) {
        BluetoothSocket tmp = null;

        try {
            UUID myId = UUID.fromString("30623c1d-b5eb-4c55-b12e-a8631f5b9240");
            tmp = device.createRfcommSocketToServiceRecord(myId);
        } catch (IOException e) {

            Log.e(TAG, "Socket's create() method failed", e);
        }
        bluetoothSocket = tmp;
    }

    @Override
    public void run() {
        Log.d(TAG, "bluetooth client run start");
        // Cancel discovery because it otherwise slows down the connection.
        //bluetoothAdapter.cancelDiscovery();

        try {
            // Connect to the remote device through the socket. This call blocks
            // until it succeeds or throws an exception.
            bluetoothSocket.connect();
        } catch (IOException connectException) {
            // Unable to connect; close the socket and return.
            try {
                bluetoothSocket.close();
            } catch (IOException closeException) {
                Log.e(TAG, "Could not close the client socket", closeException);
            }
            return;
        }
        Log.d(TAG, "connection attempt succeeded");

        //Thread inputStreamThread = new Thread(new InputStreamRunnable(bluetoothSocket));
        //inputStreamThread.start();

        Thread outputStreamRunnable = new Thread(new OutputStreamRunnable(bluetoothSocket, MainActivity.tempInt));
        outputStreamRunnable.start();
    }

    // Closes the client socket and causes the thread to finish.
    public void cancel() {
        try {
            bluetoothSocket.close();
        } catch (IOException e) {
            Log.e(TAG, "Could not close the client socket", e);
        }
    }
}
