package com.mk.m_folder.data.thread;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;

import static java.util.UUID.fromString;

public class BluetoothRunnable implements Runnable {

    private final BluetoothServerSocket mmServerSocket;

    private static final String TAG = "MainActivity";

    public BluetoothRunnable(BluetoothAdapter bluetoothAdapter) {
            // Use a temporary object that is later assigned to mmServerSocket
            // because mmServerSocket is final.
            BluetoothServerSocket tmp = null;
            try {
                // MY_UUID is the app's UUID string, also used by the client code.
                tmp = bluetoothAdapter.listenUsingRfcommWithServiceRecord("myServer", fromString("hello_world"));
            } catch (IOException e) {
                Log.d(TAG, "Socket's listen() method failed", e);
            }
            mmServerSocket = tmp;
    }

    @Override
    public void run() {
            BluetoothSocket socket = null;
            // Keep listening until exception occurs or a socket is returned.
            while (true) {
                try {
                    socket = mmServerSocket.accept();
                } catch (IOException e) {
                    Log.e(TAG, "Socket's accept() method failed", e);
                    break;
                }

                if (socket != null) {
                    // A connection was accepted. Perform work associated with
                    // the connection in a separate thread.

                    //manageMyConnectedSocket(socket);
                    //mmServerSocket.close();
                    //break;
                }
            }
    }

    public void cancel() {
        try {
            mmServerSocket.close();
        } catch (IOException e) {
            Log.e(TAG, "Could not close the connect socket", e);
        }
    }

}