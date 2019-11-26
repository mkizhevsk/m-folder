package com.mk.m_folder.data.thread;

import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;

import com.mk.m_folder.MainActivity;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


public class ConnectedThread extends Thread {

        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;
        private byte[] mmBuffer;

        public static boolean running = false;

        private static final String TAG = "MainActivity";

        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try {
                tmpIn = socket.getInputStream();
            } catch (IOException e) {
                Log.d(TAG, "Error occurred when creating input stream", e);
            }
            try {
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.d(TAG, "Error occurred when creating output stream", e);
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        @Override
        public void run() {
            running = true;

            MainActivity.inputStream = this.mmInStream;
            MainActivity.outputStream = this.mmOutStream;

            try {
                DataOutputStream dataOutputStream = new DataOutputStream(mmOutStream);
                dataOutputStream.writeUTF("some sending");
                Log.e(TAG, "Output success!");
            } catch (IOException e) {
                Log.d(TAG, "Error occurred when sending data");
            }

            while (running) {
                try {
                    DataInputStream in = new DataInputStream(mmInStream);
                    int tempNumber = in.readInt();
                    Log.e(TAG, "Input: " + tempNumber);

                    Message message = new Message();
                    Bundle bundle = new Bundle();
                    bundle.putInt("buttonNumber", tempNumber);
                    message.setData(bundle);
                    MainActivity.inputHandler.sendMessage(message);

                    MainActivity.connected = true;
                } catch (IOException e) {
                    MainActivity.connected = false;

                    Log.d(TAG, "Input stream was disconnected");
                    try {
                        mmSocket.close();
                        Log.d(TAG, "bluetoothSocket was closed");
                    } catch (IOException be) {
                        Log.e(TAG, "Could not close bluetoothSocket", be);
                    }
                    break;
                }
            }
        }

        public void write(byte[] bytes) {
            try {
                mmOutStream.write(bytes);
                Log.d(TAG, "something was sent");
            } catch (IOException e) {
                Log.d(TAG, "Error occurred when sending data");
            }
        }
    }
