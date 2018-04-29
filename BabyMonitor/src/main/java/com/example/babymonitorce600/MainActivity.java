package com.example.babymonitorce600;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

public class MainActivity extends Activity {
    private final static String TAG = "MainActivity";
    // 宣告元件
    Handler bluetoothIn;
    Button button1;
    TextView text;
    ImageView lineAnimation, backgroundAnimation, imagepower;
    private AnimationDrawable scanline, scangreen, scanred, scanpower;

    // 接收字串
    private String g = "g", r = "r", p = "p";

    // 辨別藍芽用
    final int handlerState = 0;

    // 藍芽啟用參數設定
    private BluetoothAdapter btAdapter = null;
    private BluetoothSocket btSocket = null;

    // 收到訊息時存放在這裡
    private StringBuilder recDataString = new StringBuilder();

    // 定義連線用的Thread
    private ConnectedThread mConnectedThread;

    // HC-05、HC-06專用識別碼，連至其他裝置則無效
    private static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    // MAC位址
    private static String address;

    // 時間、次數設定
    private boolean green = true;
    private int tsec = 20;// 代表20秒
    private int order = 0, startred = 0, stargreen = 0, stopred = 0, stopgreen = 0;

    // 設定提示音參數
    private SoundPool spool, spool2;
    private int sourceid, playred;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // 定義元件位置
        imagepower = (ImageView) findViewById(R.id.imagepower);
        backgroundAnimation = (ImageView) findViewById(R.id.imageView1);
        lineAnimation = (ImageView) findViewById(R.id.imageView2);
        button1 = (Button) findViewById(R.id.button1);

        // 聲音庫的最大音頻流數目為10，聲音品質為5
        spool = new SoundPool(10, AudioManager.STREAM_MUSIC, 5);
        spool2 = new SoundPool(10, AudioManager.STREAM_MUSIC, 5);
        sourceid = spool.load(this, R.raw.sound, 1);
        playred = spool2.load(this, R.raw.baby, 1);

        start_animation_power(R.drawable.loadanimation_power);
        // 測試用
        // text = (TextView) findViewById(R.id.textView1);


        // -----------------------------------分隔線--------------------------------------------
// 使用Handler物件
        bluetoothIn = new Handler() {
            public void handleMessage(android.os.Message msg) {
                Log.d(TAG," Handle get Message : "+msg);
                // 如果收到訊息
                if (msg.what == handlerState) {
                    String readMessage = (String) msg.obj;

                    // 抓取字串
                    recDataString.append(readMessage);

                    // text.setText(recDataString);

                    // 抓取藍芽字串直到最後一個字元為"~"
                    int endOfLineIndex = recDataString.indexOf("~");

                    // 如果"~"前有字串就放進來
                    if (endOfLineIndex > 0) {

                        // 抓取開頭為!的字串，辨別字串用，避免干擾
                        if (recDataString.charAt(0) == '!') {

                            // 排除開頭字元
                            String deletchar = recDataString.substring(1, endOfLineIndex);

                            // 如果收到指定字串，更改指定狀態
                            // 綠色信號狀態
                            if (deletchar.equals(g)) {
                                green = true;
                                tsec = 20;
                                startred = 0;
                                stargreen++;
                                if (stopred > 0) {
                                    stop_animation_red();
                                    stopred = 0;
                                }
                                if (stargreen == 1) {
                                    stopgreen++;
                                    lineAnimation.setVisibility(View.VISIBLE);
                                    start_animation_line(R.drawable.loadanimation_line);
                                    start_animation_green(R.drawable.loadanimation_green);
                                }
                            }
                            // 紅色信號狀態，需彈跳
                            if (deletchar.equals(r)) {
                                green = true;
                                stargreen = 0;
                                order++;
                                startred++;
                                tsec = 7;
                                if (order >= 16) {
                                    order = 0;
                                    playSud2(0);
                                    // 獲取電源管理器對象
                                    PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
                                    // 獲取PowerManager.WakeLock對象,後面的參數|表示同時傳入兩個值,最後的是LogCat裡用的Tag
                                    PowerManager.WakeLock wl = pm.newWakeLock(
                                            PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.SCREEN_DIM_WAKE_LOCK,
                                            "bright");
                                    // 點亮屏幕
                                    wl.acquire();
                                    wl.release();
                                    Intent intent = new Intent(MainActivity.this, DialogRed.class);
                                    startActivity(intent);
                                }
                                if (startred == 1) {
                                    if (stopgreen == 1) {
                                        stopgreen = 0;
                                        stop_animation_green();
                                        stop_animation_line();
                                        lineAnimation.setVisibility(View.INVISIBLE);
                                    }
                                    playSud2(0);
                                    stopred++;
                                    start_animation_red(R.drawable.loadanimation_red);
                                    // 獲取電源管理器對象
                                    PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
                                    // 獲取PowerManager.WakeLock對象,後面的參數|表示同時傳入兩個值,最後的是LogCat裡用的Tag
                                    PowerManager.WakeLock wl = pm.newWakeLock(
                                            PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.SCREEN_DIM_WAKE_LOCK,
                                            "bright");
                                    // 點亮屏幕
                                    wl.acquire();
                                    wl.release();
                                    Intent intent = new Intent(MainActivity.this, DialogRed.class);
                                    startActivity(intent);
                                }
                            }
                            // 電源信號狀態
                            if (deletchar.equals(p)) {
                                // if(tsec >= 0){

                                tsec = 20;
                                playSud(0);
                                if (stargreen >= 1) {
                                    stargreen = 0;
                                    stop_animation_line();
                                    lineAnimation.setVisibility(View.INVISIBLE);
                                    stop_animation_green();
                                }
                                if (startred >= 1) {
                                    startred = 0;
                                    stop_animation_red();
                                }
                                backgroundAnimation.setImageDrawable(getResources().getDrawable(R.drawable.background));
                                // 顯示電源圖案
                                imagepower.setVisibility(View.VISIBLE);
                                start_animation_power(R.drawable.loadanimation_power);
                                // }else if(tsec == -1){
                                // try
                                // {
                                // 連線關閉
                                // btSocket.close();
                                // } catch (IOException e) {
                                // MainActivity.this.finish();
                                // }
                                // }
                            }
                        }
                        // 清除字串資料
                        recDataString.delete(0, recDataString.length());
                    }
                }

                switch (msg.what) {
                    // 沒收到訊號的狀態，需彈跳
                    case 10:
                        if (tsec == 0) {
                            // 獲取電源管理器對象
                            PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
                            // 獲取PowerManager.WakeLock對象,後面的參數|表示同時傳入兩個值,最後的是LogCat裡用的Tag
                            PowerManager.WakeLock wl = pm.newWakeLock(
                                    PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.SCREEN_DIM_WAKE_LOCK, "bright");
                            // 點亮屏幕
                            wl.acquire();
                            wl.release();
                            tsec = 20;
                            Intent a = new Intent(MainActivity.this, DialogSignal.class);
                            startActivity(a);
                            backgroundAnimation.setImageDrawable(getResources().getDrawable(R.drawable.connect));
                            playSud(4);
                            if (stargreen >= 1) {
                                stargreen = 0;
                                stop_animation_line();
                                lineAnimation.setVisibility(View.INVISIBLE);
                                stop_animation_green();
                            }
                            if (startred >= 1) {
                                startred = 0;
                                stop_animation_red();
                            }
                        }
                        break;
                }
            }
        };
        // 出現的圖案隱藏
        imagepower.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imagepower.setVisibility(View.INVISIBLE);
            }
        });

        button1.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                ConfirmExit();
            }
        });
        // 取得BluetoothAdapter
        btAdapter = BluetoothAdapter.getDefaultAdapter();
        checkBTState();
    }

    // -----------------------------------分隔線--------------------------------------------
    // 創建BluetoothSocket
    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {
        // 利用UUID來連接藍芽
        return device.createRfcommSocketToServiceRecord(BTMODULEUUID);
    }

    // 程式開啟
    @Override
    public void onResume() {
        super.onResume();

        // 宣告Timer
        Timer timer01 = new Timer();

        // 設定Timer(task為執行內容，0代表立刻開始,間格1秒執行一次，1000為一秒、2000為兩秒，類推)
        timer01.schedule(task, 0, 1000);

        // Toast.makeText(getBaseContext(), "star", Toast.LENGTH_LONG).show();
        // 定義intent
        Intent intent = getIntent();
        // 取得前一頁獲得的藍芽MAC位址
        address = intent.getStringExtra(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
        Log.d(TAG,"get BT　macaddress : "+address);
        // 設定MAC位址
        BluetoothDevice device = btAdapter.getRemoteDevice(address);

        // 建立藍芽通道
        try {
            btSocket = createBluetoothSocket(device);
        } catch (IOException e) {
            Toast.makeText(getBaseContext(), "Bluetooth connection failed.", Toast.LENGTH_LONG).show();
        }
        // 建立藍芽連線
        try {
            // 連線
            btSocket.connect();
        } catch (IOException e) {
            try {
                btSocket.close();
            } catch (IOException e2) {
                Toast.makeText(getBaseContext(), "Bluetooth connection error.\nPlease confirm the Bluetooth connection.", Toast.LENGTH_LONG).show();
                finish();
            }
        }
        mConnectedThread = new ConnectedThread(btSocket);
        mConnectedThread.start();
    }

    // 程式暫停
    @Override
    public void onPause() {
        super.onPause();
        // Toast.makeText(getBaseContext(), "push", Toast.LENGTH_LONG).show();
        // 定義intent
        Intent intent = getIntent();
        // 取得前一頁獲得的藍芽MAC位址
        address = intent.getStringExtra(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
        // 設定MAC位址
        BluetoothDevice device = btAdapter.getRemoteDevice(address);

        // 建立藍芽通道
        try {
            btSocket = createBluetoothSocket(device);
        } catch (IOException e) {
            Toast.makeText(getBaseContext(), "Bluetooth connection failed.", Toast.LENGTH_LONG).show();
        }
        try {
            btSocket.connect();
        } catch (IOException e2) {

        }
        mConnectedThread = new ConnectedThread(btSocket);
        mConnectedThread.start();
    }

    // 程式重啟
    @Override
    public void onRestart() {
        super.onRestart();
        // Toast.makeText(getBaseContext(), "rest", Toast.LENGTH_LONG).show();
        // 定義intent
        Intent intent = getIntent();
        // 取得前一頁獲得的藍芽MAC位址
        address = intent.getStringExtra(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
        // 設定MAC位址
        BluetoothDevice device = btAdapter.getRemoteDevice(address);

        // 偵測並顯示錯誤提示
        try {
            btSocket = createBluetoothSocket(device);
        } catch (IOException e) {
            Toast.makeText(getBaseContext(), "Bluetooth connection failed.", Toast.LENGTH_LONG).show();
        }
        // 建立藍芽連線
        try {
            // 連線
            btSocket.connect();
        } catch (IOException e) {
            try {
                btSocket.close();
            } catch (IOException e2) {
                // Toast.makeText(getBaseContext(), "藍芽連線錯誤，請確認藍芽連線rt",
                // Toast.LENGTH_LONG).show();
                finish();
            }
        }
        mConnectedThread = new ConnectedThread(btSocket);
        mConnectedThread.start();
    }

    public void onDestroy() {
        super.onDestroy();
        MainActivity.this.finish();
        try {
            // 藍芽關閉
            btSocket.close();
        } catch (IOException e2) {
            MainActivity.this.finish();
        }
    }

    // 檢查裝置藍芽狀態
    private void checkBTState() {
        if (btAdapter == null) {
            Toast.makeText(getBaseContext(), "裝置不支援藍芽", Toast.LENGTH_LONG).show();
        } else {
            if (btAdapter.isEnabled()) {
            } else {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, 1);
            }
        }
    }

    // 創建連線的Thread
    private class ConnectedThread extends Thread {
        // 藍芽的輸出/入設定
        private final InputStream mmInStream;

        // 執行ConnectedThread
        public ConnectedThread(BluetoothSocket socket) {
            InputStream tmpIn = null;
            // 輸入連接
            try {
                tmpIn = socket.getInputStream();
            } catch (IOException e) {
            }
            // 定義輸入輸出
            mmInStream = tmpIn;
        }

        public void run() {
            byte[] buffer = new byte[256];
            int bytes;

            // 不斷監聽收到的訊息
            while (true) {
                try {
                    // 讀取藍芽發出的訊息
                    bytes = mmInStream.read(buffer);
                    String readMessage = new String(buffer, 0, bytes);
                    Log.d(TAG, "Get BT info　" + readMessage);
                    // 將收到的訊息存入bluetoothIn
                    bluetoothIn.obtainMessage(handlerState, bytes, -1, readMessage).sendToTarget();
                } catch (IOException e) {

                    break;
                }
            }
        }
    }

    // 定義時間參數
    private TimerTask task = new TimerTask() {
        @Override
        public void run() {
            // TODO Auto-generated method stub
            if (green) {
                // 如果是true則每秒tsec+1
                tsec--;

                Message message = new Message();
                // 傳送訊息1
                message.what = 10;
                bluetoothIn.sendMessage(message);

            }
        }
    };

    // 聲音控制-power、lost
    public void playSud(int repeatTime) {
        AudioManager am = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
        // 獲取目前音量
        float audCurrentVolumn = am.getStreamVolume(AudioManager.STREAM_MUSIC);
        // 獲取最大音量
        float audMaxVolumn = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        // 左右聲道值範圍為 0.0 - 1.0
        float volRatio = audCurrentVolumn / audMaxVolumn;
        // 播放音頻，左右音量，設置優先級，重撥次數，速率(速率最低0.5，最高為2，1代表正常速度)
        spool.play(sourceid, volRatio, volRatio, 1, repeatTime, 1);
    }

    // 聲音控制-red
    public void playSud2(int repeatTime) {
        AudioManager am = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
        // 獲取目前音量
        float audCurrentVolumn = am.getStreamVolume(AudioManager.STREAM_MUSIC);
        // 獲取最大音量
        float audMaxVolumn = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        // 左右聲道值範圍為 0.0 - 1.0
        float volRatio = audCurrentVolumn / audMaxVolumn;
        // 播放音頻，左右音量，設置優先級，重撥次數，速率(速率最低0.5，最高為2，1代表正常速度)
        spool2.play(playred, volRatio, volRatio, 1, repeatTime, 1);
    }

    // 動畫-line開始
    private void start_animation_line(int id) {
        lineAnimation.setImageResource(id);
        scanline = (AnimationDrawable) lineAnimation.getDrawable();
        scanline.start();
    }

    // 動畫-line停止
    private void stop_animation_line() {
        scanline.stop();
    }

    // 動畫-green開始
    private void start_animation_green(int id) {
        backgroundAnimation.setImageResource(id);
        scangreen = (AnimationDrawable) backgroundAnimation.getDrawable();
        scangreen.start();
    }

    // 動畫-green停止
    private void stop_animation_green() {
        scangreen.stop();
    }

    // 動畫-red開始
    private void start_animation_red(int id) {
        backgroundAnimation.setImageResource(id);
        scanred = (AnimationDrawable) backgroundAnimation.getDrawable();
        scanred.start();
    }

    // 動畫-red停止
    private void stop_animation_red() {
        scanred.stop();
    }

    // 動畫-power開始
    private void start_animation_power(int id) {
        imagepower.setImageResource(id);
        scanpower = (AnimationDrawable) imagepower.getDrawable();
        scanpower.start();
    }

    // 防止按BACK鍵跳出
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            ConfirmExit(); // 呼叫ConfirmExit()函數
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    // 跳出時出現的判斷式
    public void ConfirmExit() {
        AlertDialog.Builder ad = new AlertDialog.Builder(MainActivity.this); // 創建訊息方塊
        ad.setTitle("EXIT");
        ad.setMessage(" Are you sure you want to close this application?");
        ad.setPositiveButton("YES", new DialogInterface.OnClickListener() { // 按"是",退出應用程式
            public void onClick(DialogInterface dialog, int i) {
                //完整退出，殺死所有程序
                Intent home = new Intent(Intent.ACTION_MAIN);
                home.addCategory(Intent.CATEGORY_HOME);
                startActivity(home);
                android.os.Process.killProcess(android.os.Process.myPid());
                System.exit(0);

            }
        });
        ad.setNegativeButton("NO", new DialogInterface.OnClickListener() { // 按"否",不執行任何操作
            public void onClick(DialogInterface dialog, int i) {
            }
        });
        ad.show();// 顯示訊息視窗
    }
}
