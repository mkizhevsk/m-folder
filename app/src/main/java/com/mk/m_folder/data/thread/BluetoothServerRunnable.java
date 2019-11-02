package com.mk.m_folder.data.thread;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.util.UUID;

public class BluetoothServerRunnable implements Runnable {

    private final BluetoothServerSocket bluetoothServerSocket;
    public static boolean running = false;

    private static final String TAG = "MainActivity";

    public BluetoothServerRunnable(BluetoothAdapter bluetoothAdapter) {
            // Use a temporary object that is later assigned to bluetoothServerSocket
            // because bluetoothServerSocket is final.
            BluetoothServerSocket tmp = null;
            UUID myId = UUID.fromString("302a5a70-c085-4946-b702-fc1deb1046af");
            try {
                // MY_UUID is the app's UUID string, also used by the client code.
                tmp = bluetoothAdapter.listenUsingRfcommWithServiceRecord("myServer", myId);
            } catch (IOException e) {
                Log.d(TAG, "Socket's listen() method failed", e);
            }
            bluetoothServerSocket = tmp;
    }

    @Override
    public void run() {
            running = true;
            Log.d(TAG, "bluetooth server run start");
            BluetoothSocket bluetoothSocket = null;
            // Keep listening until exception occurs or a socket is returned.
            while (running) {
                //Log.d(TAG, "start running");
                try {
                    Log.d(TAG, "try to connect..");
                    bluetoothSocket = bluetoothServerSocket.accept();
                } catch (IOException e) {
                    Log.e(TAG, "Socket's accept() method failed", e);
                    break;
                }

                if (bluetoothSocket != null) {
                    // A connection was accepted. Perform work associated with
                    // the connection in a separate thread.
                    Log.d(TAG, "connection was accepted");

                    Thread inputThread = new Thread(new InputRunnable(bluetoothSocket));
                    inputThread.start();

                    //cancel();
                    //break;
                } else {
                    Log.d(TAG, "socket null");
                }
            }
    }

    public void cancel() {
        try {
            bluetoothServerSocket.close();
        } catch (IOException e) {
            Log.e(TAG, "bluetoothServerSocket", e);
        }
    }

}
