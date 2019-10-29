package com.mk.m_folder.data.thread;

import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.DataOutputStream;
import java.io.IOException;

public class OutputStreamRunnable implements Runnable {

    private BluetoothSocket bluetoothSocket;

    private static final String TAG = "MainActivity";

    private int tempInt;

    public OutputStreamRunnable(BluetoothSocket bluetoothSocket, int tempInt) {
        this.bluetoothSocket = bluetoothSocket;
        this.tempInt = tempInt;
    }

    @Override
    public void run() {
        try {
            DataOutputStream out = new DataOutputStream(bluetoothSocket.getOutputStream());
            out.writeInt(tempInt);
            Log.e(TAG, "server out: " + tempInt);
        } catch (IOException e) {
            Log.e(TAG, "sending attempt failed", e);
        }

    }
}
