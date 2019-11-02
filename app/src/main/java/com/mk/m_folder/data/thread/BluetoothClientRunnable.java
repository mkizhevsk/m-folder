package com.mk.m_folder.data.thread;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import com.mk.m_folder.MainActivity;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
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
            Log.d(TAG, "try to connect..");
            // Connect to the remote device through the socket. This call blocks
            // until it succeeds or throws an exception.
            bluetoothSocket.connect();
        } catch (IOException connectException) {
            // Unable to connect; close the socket and return.
            try {
                bluetoothSocket.close();
                Log.d(TAG, "bluetoothSocket was closed");
            } catch (IOException e) {
                Log.e(TAG, "Could not close bluetoothSocket", e);
            }
            return;
        }
        Log.d(TAG, "client connection success!");

        try {
            OutputStream outputStream = bluetoothSocket.getOutputStream();

            DataOutputStream dataOutputStream = new DataOutputStream(outputStream);

            dataOutputStream.writeInt(MainActivity.tempInt);
            Log.e(TAG, "Output: " + MainActivity.tempInt);

            //cancel();
        } catch (IOException e) {
            Log.d(TAG, "Error occured when creating output stream");
            cancel();
        }
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
