package com.mk.m_folder.data.thread;

import android.app.Notification;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;

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
            int tempNumber = in.readInt();
            Log.e(TAG, "Input: " + tempNumber);

            Message message = new Message();
            Bundle bundle = new Bundle();
            bundle.putInt("buttonNumber", tempNumber);
            message.setData(bundle);
            MainActivity.inputHandler.sendMessage(message);

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
