package com.example.babymonitorce600;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Neo on 2018/4/14.
 */

public class BluetoothMessage implements Runnable {
    private static final String TAG = "BluetoothMessage";
    private static final ExecutorService sConnectExecutor = Executors.newSingleThreadExecutor();
    private BluetoothDevice mDevice;
    private BluetoothSocket mSocket;
    private BluetoothMessageListener mSessionStateListener;
    private Thread mCheckThread;
    private Runnable mMessageReport = new Runnable() {
        @Override
        public void run() {
            while (true) {
                final byte[] buffer = new byte[256];
                try {
                    // 讀取藍芽發出的訊息
                    final int bytes = mSocket.getInputStream().read(buffer);
                    final String readMessage = new String(buffer, 0, bytes);
                    Log.d(TAG, "Get BT info　" + readMessage);
                    if (mSessionStateListener != null) {
                        mSessionStateListener.onInputStreamRecive(mDevice, readMessage);
                    }
                } catch (IOException e) {
                    Log.e(TAG, " read Bluetooth BluetoothSocket InputStream Exception " + e);
                    if (mSessionStateListener != null) {
                        mSessionStateListener.onError(mDevice, e);
                    }
                    break;
                }
            }
        }
    };

    public BluetoothDevice getMessageMaster() {
        return mDevice;
    }

    public BluetoothMessage(String initkey, BluetoothDevice device) {
        mDevice = device;
        try {
            mSocket = device.createRfcommSocketToServiceRecord(UUID.fromString(initkey));
        } catch (IOException e) {
            Log.e(TAG, " create Bluetooth connection BluetoothSocket failed ");
            if (mSessionStateListener != null) {
                mSessionStateListener.onError(mDevice, e);
            }
        }
    }

    @Override
    public void run() {
        connect();
    }

    void connect() {
        sConnectExecutor.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    if (!mSocket.isConnected()) {
                        mSocket.connect();
                    }
                    if (mCheckThread == null) {
                        mCheckThread = new Thread(mMessageReport);
                    }
                    mCheckThread.start();
                } catch (IOException e) {
                    if (mSessionStateListener != null) {
                        mSessionStateListener.onError(mDevice, e);
                    }
                    try {
                        mSocket.close();
                    } catch (IOException e2) {
                        if (mSessionStateListener != null) {
                            mSessionStateListener.onError(mDevice, e2);
                        }
                    }
                }
            }
        });
    }

    public void disconnect() {
        synchronized (this) {

        }
        sConnectExecutor.submit(new Runnable() {
            @Override
            public void run() {

            }
        });
    }

    public synchronized void setOnBluetoothMessageListener(BluetoothMessageListener listener) {
        mSessionStateListener = listener;
    }

    public interface BluetoothMessageListener {
        void onConnected(@NonNull BluetoothDevice device);

        void onInputStreamRecive(@NonNull BluetoothDevice device, String message);

        void onDisconnected(@NonNull BluetoothDevice device);

        void onError(@NonNull BluetoothDevice device, @Nullable Throwable t);
    }
}
