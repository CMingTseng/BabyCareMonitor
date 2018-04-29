package com.example.babymonitorce600;

import android.bluetooth.BluetoothDevice;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.ArrayMap;
import android.util.Log;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Neo on 2018/4/14.
 */

public class BluetoothSocketManager implements BluetoothMessage.BluetoothMessageListener {
    private static final String TAG = "BluetoothSocketManager";
    private static final ExecutorService sConnectExecutor = Executors.newSingleThreadExecutor();
    private static Handler mHandler;
    private static BluetoothSocketManager sManager = null;
    private final ArrayMap<String, BluetoothMessage> mSessions = new ArrayMap<>();
    private BluetoothSocketWatcher mBluetoothSocketWatcher;

    private BluetoothSocketManager() {
        mHandler = new Handler(Looper.getMainLooper());
    }

    public static BluetoothSocketManager getBluetoothSocketManager() {
        if (sManager == null) {
            sManager = new BluetoothSocketManager();
        }
        return sManager;
    }

    public void prepare(String initkey, BluetoothDevice device) {
        BluetoothMessage session;
        synchronized (this) {
            final String macaddress = device.getAddress();
            session = mSessions.get(macaddress);
            if (session == null) {
                mSessions.put(macaddress, session = new BluetoothMessage(initkey, device));
            }
            session.setOnBluetoothMessageListener(this);
        }
        if (session != null) {
            if (this.mBluetoothSocketWatcher != null) {
                this.mBluetoothSocketWatcher.onBluetoothSocketPrepared(session);
            }
        }
    }

    public void connect(String macaddress) {
        BluetoothMessage session;
        synchronized (this) {
            session = mSessions.get(macaddress);
            if (session == null) {
                return;
            }
            session.setOnBluetoothMessageListener(this);
        }
        Log.d(TAG, "connect session : " + macaddress);
        sConnectExecutor.submit(session);
    }

    public synchronized BluetoothMessage getBluetoothSocket(String macaddress) {
        return mSessions.get(macaddress);
    }

    public synchronized BluetoothMessage getBluetoothSocket(int index) {
        return mSessions.valueAt(index);
    }

    public synchronized int getBluetoothSocketCount() {
        return mSessions.size();
    }

    @Override
    public void onConnected(@NonNull BluetoothDevice device) {
        if (this.mBluetoothSocketWatcher != null) {
            this.mBluetoothSocketWatcher.onBluetoothSocketStateChange(getBluetoothSocket(device.getAddress()), 1);
        }
    }

    @Override
    public void onInputStreamRecive(@NonNull BluetoothDevice device, String message) {
        if (this.mBluetoothSocketWatcher != null) {
            this.mBluetoothSocketWatcher.onBluetoothSocketReciveMessage(getBluetoothSocket(device.getAddress()), message);
        }
    }

    @Override
    public void onDisconnected(@NonNull BluetoothDevice device) {
        if (this.mBluetoothSocketWatcher != null) {
            this.mBluetoothSocketWatcher.onBluetoothSocketStateChange(getBluetoothSocket(device.getAddress()), -1);
        }
    }

    @Override
    public void onError(@NonNull BluetoothDevice device, @Nullable Throwable t) {
        if (this.mBluetoothSocketWatcher != null) {
            this.mBluetoothSocketWatcher.onBluetoothSocketError(getBluetoothSocket(device.getAddress()), t);
        }
    }

    public synchronized void setOnBluetoothSocketWatcher(BluetoothSocketWatcher listener) {
        this.mBluetoothSocketWatcher = listener;
    }

    public synchronized void unregisterBluetoothSocketWatcher() {
        this.mBluetoothSocketWatcher = null;
    }

    public interface BluetoothSocketWatcher {
        void onBluetoothSocketPrepared(@NonNull BluetoothMessage bt);

        void onBluetoothSocketReciveMessage(@NonNull BluetoothMessage bt, String message);

        void onBluetoothSocketStateChange(@NonNull BluetoothMessage bt, int i);

        void onBluetoothSocketError(@NonNull BluetoothMessage bt, @Nullable Throwable th);
    }
}
