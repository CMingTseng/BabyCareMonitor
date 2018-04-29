package com.example.babymonitorce600;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.drawable.AnimationDrawable;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

public class MainFragment extends Fragment {
    private static final String TAG = "MainFragment";
    private BluetoothDeviceAdapter mBluetoothDevicesAdapter;
    private PairDeviceDetailAdapter mPairedDevicesAdapter;
    private BluetoothDevice mPairDevice;
    private ServiceConnection mServiceConnection;


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View root = inflater.inflate(R.layout.fragment_main, container, false);
        final Spinner deviceslist = (Spinner) root.findViewById(R.id.deviceslist);
        mBluetoothDevicesAdapter = new BluetoothDeviceAdapter();
        deviceslist.setAdapter(mBluetoothDevicesAdapter);
        deviceslist.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "onItemSelected : " + position);
                mPairDevice = mBluetoothDevicesAdapter.getItem(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Log.d(TAG, "SonNothingSelected");
            }
        });
        final Button pair = (Button) root.findViewById(R.id.pair);
        pair.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final int type = mPairDevice.getType();
                switch (type) {
                    case BluetoothDevice.DEVICE_TYPE_DUAL:
                        break;
                    case BluetoothDevice.DEVICE_TYPE_LE:


                        break;
                    case BluetoothDevice.DEVICE_TYPE_CLASSIC:
                        if (mPairDevice.getBondState() == BluetoothDevice.BOND_NONE) {
                            try {
                                createBond(mPairDevice);
                            } catch (Exception e) {
                                Log.d(TAG, "BluetoothDevice createBond Exception : " + e);
                            }
                        }
                    default:
                        break;

                }
            }
        });
        mPairedDevicesAdapter = new PairDeviceDetailAdapter();
        final RecyclerView pairlist = (RecyclerView) root.findViewById(R.id.pairlist);
        pairlist.setAdapter(mPairedDevicesAdapter);
        return root;
    }

    private class BluetoothDeviceAdapter extends BaseAdapter {
        protected int mSelected = -1;
        private final ArrayList<BluetoothDevice> mBluetoothDevices = new ArrayList<BluetoothDevice>();
        private final BroadcastReceiver mDeviceDiscoveringReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                final String action = intent.getAction();
                Log.d(TAG, "BluetoothDeviceAdapter Show get Action : " + action);
                Log.d(TAG, "BluetoothDeviceAdapter Show get intent : " + intent);
                if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                    final BluetoothDevice e = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    Log.d(TAG, "BluetoothDeviceAdapter Show BluetoothDevice info  : " + e.toString());
                    Log.d(TAG, "BluetoothDeviceAdapter Show BluetoothDevice info name : " + e.getName());
                    Log.d(TAG, "BluetoothDeviceAdapter Show BluetoothDevice info mac : " + e.getAddress());
                    Log.d(TAG, "BluetoothDeviceAdapter Show BluetoothDevice getBondState : " + e.getBondState());
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                        Log.d(TAG, "BluetoothDeviceAdapter Show BluetoothDevice info uuids : " + e.getUuids());
                        Log.d(TAG, "BluetoothDeviceAdapter Show BluetoothDevice info BT　type : " + e.getType());
                    }
                    if (e.getBondState() == BluetoothDevice.BOND_NONE && !mBluetoothDevices.contains(e)) {
                        mBluetoothDevices.add(e);
                        notifyDataSetChanged();
                    }
                } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                }
            }
        };

        private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
            @Override
            public void onLeScan(final BluetoothDevice device, final int rssi, byte[] scanRecord) {
                Log.d(TAG, " Show get BT　Address:" + device.getAddress());
                Log.d(TAG, " Show get BT　Name:" + device.getName());
                Log.d(TAG, " Show get BT　rssi:" + rssi);
            }
        };

        public BluetoothDeviceAdapter() {
        }

        public void startMonitor() {
            final IntentFilter bluetoothfilter = new IntentFilter();
            bluetoothfilter.addAction(BluetoothDevice.ACTION_FOUND);
            bluetoothfilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
            getActivity().registerReceiver(mDeviceDiscoveringReceiver, bluetoothfilter);
        }

        public void stopMonitor() {
            getActivity().unregisterReceiver(mDeviceDiscoveringReceiver);
        }

        public void setSelected(int position) {
            mSelected = position;
        }

        public int select(BluetoothDevice e) {
            if (mBluetoothDevices.isEmpty()) {
                mSelected = -1;
            } else {
                mSelected = mBluetoothDevices.indexOf(e);
            }
            return mSelected;
        }

        @Override
        public int getCount() {
            return mBluetoothDevices.size();
        }

        @Override
        public BluetoothDevice getItem(int position) {
            return mBluetoothDevices.get(position);
        }

        @Override
        public long getItemId(int position) {
            return mBluetoothDevices.get(position).hashCode();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = View.inflate(parent.getContext(), android.R.layout.simple_list_item_1, null);
                final BTDevicesViewHolder holder = new BTDevicesViewHolder(convertView);
                convertView.setTag(R.id.tag_view_holder, holder);
            }
            final BTDevicesViewHolder holder = (BTDevicesViewHolder) convertView.getTag(R.id.tag_view_holder);
            final BluetoothDevice e = getItem(position);
            holder.name.setText(e.getName());
            return convertView;
        }

        private class BTDevicesViewHolder {
            TextView name;

            BTDevicesViewHolder(View view) {
                name = (TextView) view.findViewById(android.R.id.text1);
            }
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = View.inflate(parent.getContext(), android.R.layout.simple_spinner_dropdown_item, null);
                final DropDownViewHolder holder = new DropDownViewHolder(convertView);
                convertView.setTag(R.id.tag_view_holder, holder);
            }
            final DropDownViewHolder holder = (DropDownViewHolder) convertView.getTag(R.id.tag_view_holder);
            final BluetoothDevice e = getItem(position);
            holder.name.setText(e.getAddress());
            holder.name.setChecked((mSelected == position));
            return convertView;
        }

        private class DropDownViewHolder {
            CheckedTextView name;

            DropDownViewHolder(View view) {
                name = (CheckedTextView) view.findViewById(android.R.id.text1);
            }
        }
    }

    private class PairDeviceDetailAdapter extends RecyclerView.Adapter<PairDeviceDetailAdapter.ViewHolder> {
        private final StringBuilder mRecDataString = new StringBuilder();
        private final ArrayList<BluetoothDevice> mPairedDevices = new ArrayList<BluetoothDevice>();
        private final BroadcastReceiver mDevicePairedReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                final String action = intent.getAction();
                Log.d(TAG, "PairDeviceDetailAdapter Show get Action : " + action);
                Log.d(TAG, "PairDeviceDetailAdapter Show get intent : " + intent);
                final BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device != null) {
                    final int type = device.getType();
                    switch (type) {
                        case BluetoothDevice.DEVICE_TYPE_DUAL:
                            break;
                        case BluetoothDevice.DEVICE_TYPE_LE:

                            break;
                        case BluetoothDevice.DEVICE_TYPE_CLASSIC:
                            Log.d(TAG, "PairDeviceDetailAdapter Show add BluetoothDevice getBondState : " + device.getBondState());
                            if (device.getBondState() == BluetoothDevice.BOND_BONDING) {
//                            try {
//                                setPairingConfirmation(device.getClass(), device, true);
//                                abortBroadcast();
//                                if (setPin(device.getClass(), device, BuildConfig.BT_DEFAULT_KEY)) {
////                                    mPairedDevices = BluetoothAdapter.getDefaultAdapter().getBondedDevices();
////                                    notifyDataSetChanged();
//                                }
//
//                            } catch (Exception e) {
//                                e.printStackTrace();
//                            }
                            } else if (device.getBondState() == BluetoothDevice.BOND_BONDED) {
                                if (!mPairedDevices.contains(device)) {
                                    mPairedDevices.add(device);
                                }
                                final Set<BluetoothDevice> paireddevices = BluetoothAdapter.getDefaultAdapter().getBondedDevices();
                                for (BluetoothDevice bt : paireddevices) {
                                    if (!mPairedDevices.contains(bt)) {
                                        mPairedDevices.add(bt);
                                    }
                                }
                                notifyDataSetChanged();
                            }
                        default:
                            break;
                    }
                }

                if (action.equals("android.bluetooth.device.action.PAIRING_REQUEST")) {

                }
            }
        };

        private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
            @Override
            public void onLeScan(final BluetoothDevice device, final int rssi, byte[] scanRecord) {
                Log.d(TAG, " Show get BT　Address:" + device.getAddress());
                Log.d(TAG, " Show get BT　Name:" + device.getName());
                Log.d(TAG, " Show get BT　rssi:" + rssi);
            }
        };

        public PairDeviceDetailAdapter() {
        }

        public void startMonitor() {
            final IntentFilter bluetoothfilter = new IntentFilter();
            bluetoothfilter.addAction(BluetoothDevice.ACTION_FOUND);
            bluetoothfilter.addAction("android.bluetooth.device.action.PAIRING_REQUEST");
            getActivity().registerReceiver(mDevicePairedReceiver, bluetoothfilter);
        }

        public void stopMonitor() {
            getActivity().unregisterReceiver(mDevicePairedReceiver);
        }

        @Override
        public int getItemCount() {
            return mPairedDevices.size();
        }

        @Override
        public int getItemViewType(final int position) {
            return R.layout.item_baby_monitor;
        }

        @NonNull
        @Override
        public PairDeviceDetailAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_baby_monitor, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull final PairDeviceDetailAdapter.ViewHolder holder, int position) {
            holder.scanpower = (AnimationDrawable) holder.imagepower.getDrawable();
            holder.scanpower.start();
            holder.listener = new BluetoothSocketManager.BluetoothSocketWatcher() {
                @Override
                public void onBluetoothSocketPrepared(@NonNull BluetoothMessage bt) {

                }

                @Override
                public void onBluetoothSocketReciveMessage(@NonNull BluetoothMessage bt, String message) {
                    Log.d(TAG, "get Message : " + message);
                    mRecDataString.append(message);
                    int endOfLineIndex = mRecDataString.indexOf("~");
                    // 如果"~"前有字串就放進來
                    if (endOfLineIndex > 0) {
                        // 抓取開頭為!的字串，辨別字串用，避免干擾
                        if (mRecDataString.charAt(0) == '!') {
                            // 排除開頭字元
                            String deletchar = mRecDataString.substring(1, endOfLineIndex);
                            // 如果收到指定字串，更改指定狀態
                            // 綠色信號狀態
                            if (deletchar.equals(HC_Message_Info.MESSAGE_G)) {
                            }
                            // 紅色信號狀態，需彈跳
                            if (deletchar.equals(HC_Message_Info.MESSAGE_R)) {
                            }
                            // 電源信號狀態
                            if (deletchar.equals(HC_Message_Info.MESSAGE_P)) {
                            }
                        }
                        // 清除字串資料
                        mRecDataString.delete(0, mRecDataString.length());
                    }
                }

                @Override
                public void onBluetoothSocketStateChange(@NonNull BluetoothMessage bt, int i) {

                }

                @Override
                public void onBluetoothSocketError(@NonNull BluetoothMessage bt, @Nullable Throwable th) {

                }
            };
            holder.manager.prepare(BuildConfig.BT_HC_0506_KEY, mPairedDevices.get(position));
            holder.manager.setOnBluetoothSocketWatcher(holder.listener);
            holder.manager.connect(mPairedDevices.get(position).getAddress());
//            try {
//                holder.mBluetoothSocket = mPairedDevices.get(position).createRfcommSocketToServiceRecord(holder.BTMODULEUUID);
//                new ConnectedThread(holder.bluetoothIn, holder.mBluetoothSocket).start();
//            } catch (IOException e) {
//                Toast.makeText(holder.itemView.getContext(), "Bluetooth connection failed.", Toast.LENGTH_LONG).show();
//            }
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            private BluetoothSocketManager manager = BluetoothSocketManager.getBluetoothSocketManager();
            private BluetoothSocketManager.BluetoothSocketWatcher listener;
            private final UUID BTMODULEUUID = UUID.fromString(BuildConfig.BT_HC_0506_KEY);
            //            private BluetoothSocket mBluetoothSocket;
//            private Handler bluetoothIn;

            final int handlerState = 0;
            private boolean green = true;
            private int tsec = 20;// 代表20秒
            private int order = 0, startred = 0, stargreen = 0, stopred = 0, stopgreen = 0;
            private ImageView lineAnimation, backgroundAnimation, imagepower;
            private AnimationDrawable scanline, scangreen, scanred, scanpower;
            private SoundPool spool, spool2;
            private int sourceid, playred;

            public ViewHolder(final View itemView) {
                super(itemView);
                imagepower = (ImageView) itemView.findViewById(R.id.imagepower);
                imagepower.setImageResource(R.drawable.loadanimation_power);
                backgroundAnimation = (ImageView) itemView.findViewById(R.id.imageView1);
                lineAnimation = (ImageView) itemView.findViewById(R.id.imageView2);
                spool = new SoundPool(10, AudioManager.STREAM_MUSIC, 5);
                spool2 = new SoundPool(10, AudioManager.STREAM_MUSIC, 5);
                sourceid = spool.load(itemView.getContext(), R.raw.sound, 1);
                playred = spool2.load(itemView.getContext(), R.raw.baby, 1);

//                bluetoothIn = new Handler() {
//                    public void handleMessage(android.os.Message msg) {
//                        Log.d(TAG, " Handle get Message : " + msg);
//                        // 如果收到訊息
//                        if (msg.what == handlerState) {
//                            String readMessage = (String) msg.obj;
//
//                            // 抓取字串
//                            mRecDataString.append(readMessage);
//
//                            // text.setText(mRecDataString);
//
//                            // 抓取藍芽字串直到最後一個字元為"~"
//                            int endOfLineIndex = mRecDataString.indexOf("~");
//
//                            // 如果"~"前有字串就放進來
//                            if (endOfLineIndex > 0) {
//
//                                // 抓取開頭為!的字串，辨別字串用，避免干擾
//                                if (mRecDataString.charAt(0) == '!') {
//
//                                    // 排除開頭字元
//                                    String deletchar = mRecDataString.substring(1, endOfLineIndex);
//
//                                    // 如果收到指定字串，更改指定狀態
//                                    // 綠色信號狀態
//                                    if (deletchar.equals(HC_Message_Info.MESSAGE_G)) {
//                                        green = true;
//                                        tsec = 20;
//                                        startred = 0;
//                                        stargreen++;
//                                        if (stopred > 0) {
//                                            scanred.stop();
//                                            stopred = 0;
//                                        }
//                                        if (stargreen == 1) {
//                                            stopgreen++;
//                                            lineAnimation.setVisibility(View.VISIBLE);
//                                            lineAnimation.setImageResource(R.drawable.loadanimation_line);
//                                            scanline = (AnimationDrawable) lineAnimation.getDrawable();
//                                            scanline.start();
//                                            backgroundAnimation.setImageResource(R.drawable.loadanimation_green);
//                                            scangreen = (AnimationDrawable) backgroundAnimation.getDrawable();
//                                            scangreen.start();
//                                        }
//                                    }
//                                    // 紅色信號狀態，需彈跳
//                                    if (deletchar.equals(HC_Message_Info.MESSAGE_R)) {
//                                        green = true;
//                                        stargreen = 0;
//                                        order++;
//                                        startred++;
//                                        tsec = 7;
//                                        if (order >= 16) {
//                                            order = 0;
////                                            playSud2(0);
//                                            // 獲取電源管理器對象
////                                            PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
////                                            // 獲取PowerManager.WakeLock對象,後面的參數|表示同時傳入兩個值,最後的是LogCat裡用的Tag
////                                            PowerManager.WakeLock wl = pm.newWakeLock(
////                                                    PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.SCREEN_DIM_WAKE_LOCK,
////                                                    "bright");
////                                            // 點亮屏幕
////                                            wl.acquire();
////                                            wl.release();
////                                            Intent intent = new Intent(MainActivity.this, DialogRed.class);
////                                            startActivity(intent);
//                                        }
//                                        if (startred == 1) {
//                                            if (stopgreen == 1) {
//                                                stopgreen = 0;
//                                                scangreen.stop();
//                                                scanline.stop();
//                                                lineAnimation.setVisibility(View.INVISIBLE);
//                                            }
////                                            playSud2(0);
//                                            stopred++;
//                                            backgroundAnimation.setImageResource(R.drawable.loadanimation_red);
//                                            scanred = (AnimationDrawable) backgroundAnimation.getDrawable();
//                                            scanred.start();
//                                            // 獲取電源管理器對象
////                                            PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
////                                            // 獲取PowerManager.WakeLock對象,後面的參數|表示同時傳入兩個值,最後的是LogCat裡用的Tag
////                                            PowerManager.WakeLock wl = pm.newWakeLock(
////                                                    PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.SCREEN_DIM_WAKE_LOCK,
////                                                    "bright");
////                                            // 點亮屏幕
////                                            wl.acquire();
////                                            wl.release();
////                                            Intent intent = new Intent(MainActivity.this, DialogRed.class);
////                                            startActivity(intent);
//                                        }
//                                    }
//                                    // 電源信號狀態
//                                    if (deletchar.equals(HC_Message_Info.MESSAGE_P)) {
//                                        // if(tsec >= 0){
//
//                                        tsec = 20;
////                                        playSud(0);
//                                        if (stargreen >= 1) {
//                                            stargreen = 0;
//                                            scanline.stop();
//                                            lineAnimation.setVisibility(View.INVISIBLE);
//                                            scangreen.stop();
//                                        }
//                                        if (startred >= 1) {
//                                            startred = 0;
//                                            scanred.stop();
//                                        }
//                                        backgroundAnimation.setImageDrawable(getResources().getDrawable(R.drawable.background));
//                                        // 顯示電源圖案
//                                        imagepower.setVisibility(View.VISIBLE);
//                                        imagepower.setImageResource(R.drawable.loadanimation_power);
//                                        scanpower = (AnimationDrawable) imagepower.getDrawable();
//                                        scanpower.start();
//                                    }
//                                }
//                                // 清除字串資料
//                                mRecDataString.delete(0, mRecDataString.length());
//                            }
//                        }
//
//                        switch (msg.what) {
//                            // 沒收到訊號的狀態，需彈跳
//                            case 10:
////                                if (tsec == 0) {
////                                    // 獲取電源管理器對象
////                                    PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
////                                    // 獲取PowerManager.WakeLock對象,後面的參數|表示同時傳入兩個值,最後的是LogCat裡用的Tag
////                                    PowerManager.WakeLock wl = pm.newWakeLock(
////                                            PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.SCREEN_DIM_WAKE_LOCK, "bright");
////                                    // 點亮屏幕
////                                    wl.acquire();
////                                    wl.release();
////                                    tsec = 20;
////                                    Intent a = new Intent(MainActivity.this, DialogSignal.class);
////                                    startActivity(a);
////                                    backgroundAnimation.setImageDrawable(getResources().getDrawable(R.drawable.connect));
////                                    playSud(4);
////
////                                    if (stargreen >= 1) {
////                                        stargreen = 0;
////                                        stop_animation_line();
////                                        lineAnimation.setVisibility(View.INVISIBLE);
////                                        stop_animation_green();
////                                    }
////                                    if (startred >= 1) {
////                                        startred = 0;
////                                        stop_animation_red();
////                                    }
////                                }
//                                break;
//                        }
//                    }
//                };
            }
        }
    }

    private boolean createBond(BluetoothDevice btDevice) throws Exception {
//        Class clazz = Class.forName("android.bluetooth.BluetoothDevice");
//        Method createBondMethod = clazz.getMethod("createBond");
        Method createBondMethod = btDevice.getClass().getMethod("createBond");
        Boolean returnValue = (Boolean) createBondMethod.invoke(btDevice);
        return returnValue.booleanValue();
    }

    private boolean setPin(Class<? extends BluetoothDevice> btClass, BluetoothDevice btDevice, String str) throws Exception {
        try {
            Method removeBondMethod = btClass.getDeclaredMethod("setPin", new Class[]{byte[].class});
            Boolean returnValue = (Boolean) removeBondMethod.invoke(btDevice, new Object[]{str.getBytes()});
            Log.e("returnValue", "" + returnValue);
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    private void setPairingConfirmation(Class<?> btClass, BluetoothDevice device, boolean isConfirm) throws Exception {
        Method setPairingConfirmation = btClass.getDeclaredMethod("setPairingConfirmation", boolean.class);
        setPairingConfirmation.invoke(device, isConfirm);
    }

    @Override
    public void onResume() {
        super.onResume();
        final Context context = getContext();
        if (!context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(context, "不支持BLE", Toast.LENGTH_SHORT).show();
        } else {
            BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                btAdapter = ((BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE)).getAdapter();
            }
            if (btAdapter == null) {
                Toast.makeText(context, "裝置不支援藍芽", Toast.LENGTH_SHORT).show();
            } else {
                if (btAdapter.isEnabled()) {
                    Log.d(TAG, "...藍芽開啟...");
                    mBluetoothDevicesAdapter.startMonitor();
                    mPairedDevicesAdapter.startMonitor();
                    if (btAdapter.isDiscovering()) {
                        btAdapter.cancelDiscovery();
                    }
                    btAdapter.startDiscovery();
                } else {
                    // 如果未開啟，則開啟藍芽      btAdapter.enable()
                    final Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableBtIntent, 1);
                }
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause()");
        BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, "onStop()");
        mBluetoothDevicesAdapter.stopMonitor();
        mPairedDevicesAdapter.stopMonitor();
    }
}
