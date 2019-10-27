package com.mk.m_folder.data.thread;

import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.DataOutputStream;
import java.io.IOException;

public class OutputStreamRunnable implements Runnable {

    private BluetoothSocket bluetoothSocket;

    private static final String TAG = "MainActivity";

    public OutputStreamRunnable(BluetoothSocket bluetoothSocket) {
        this.bluetoothSocket = bluetoothSocket;
    }

    @Override
    public void run() {
        try {
            DataOutputStream out = new DataOutputStream(bluetoothSocket.getOutputStream());
            out.writeInt(7);
            Log.e(TAG, "7 was sent");
            //Integer[] export = {1, 2 , 3};
        } catch (IOException e) {
            Log.e(TAG, "sending attempt failed", e);
        }

    }
}
