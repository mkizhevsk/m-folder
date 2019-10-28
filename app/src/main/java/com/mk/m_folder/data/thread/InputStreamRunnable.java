package com.mk.m_folder.data.thread;

import android.bluetooth.BluetoothSocket;
import android.util.Log;

import com.mk.m_folder.MainActivity;

import java.io.DataInputStream;
import java.io.IOException;

public class InputStreamRunnable implements Runnable {

    private BluetoothSocket bluetoothSocket;

    private static final String TAG = "MainActivity";

    public InputStreamRunnable(BluetoothSocket bluetoothSocket) {
        this.bluetoothSocket = bluetoothSocket;
    }

    @Override
    public void run() {
        //Log.d(TAG, "start input run");
        try {
            DataInputStream in = new DataInputStream(bluetoothSocket.getInputStream());
            int imp = in.readInt();
            Log.e(TAG, "server input: " + imp);

            //MainActivity.tempInt = imp + i;
            //MainActivity.inputHandler.sendEmptyMessage(1);

//            try {
//                bluetoothSocket.close();
//                Log.d(TAG, "client socket was closed");
//            } catch (IOException e) {
//                Log.e(TAG, "could not close the client socket", e);
//            }
        } catch (IOException e) {
            Log.e(TAG, "sending attempt failed", e);
        }

    }
}
