package com.example.babymonitorce600;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Set;


public class DeviceListActivity extends Activity {

	// 使用logcat模擬檢測用
	private static final String TAG = "DeviceListActivity";

	// 宣告藍芽物件
	BluetoothDevice mmDevice;
	BluetoothAdapter mBluetoothAdapter;

	// 宣告元件
	Button button16, button1, button2;
	TextView textView1;
	ListView paired_devices;
	private ImageView imageview;
	// 將獲得到的MAC位址發送到主畫面
	public static String EXTRA_DEVICE_ADDRESS = "device_address";

	// 調用BluetoothAdapter元素，藍芽才能使用
	private BluetoothAdapter mBtAdapter;
	private ArrayAdapter<String> mPairedDevicesArrayAdapter;

	// 連線用字串
	private String address, name;
	private int str = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.device_list);

		final Animation animation = new AlphaAnimation(1, 0); // 建立物件使元件閃爍

		animation.setDuration(200); // 閃爍時間

		animation.setInterpolator(new LinearInterpolator()); // 使用元件

		animation.setRepeatCount(Animation.INFINITE); // 元件顯示

		animation.setRepeatMode(Animation.REVERSE); // 元件消失

		// 取得藍芽
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

		// 定義元件位置
		button16 = (Button) findViewById(R.id.button16);
		button1 = (Button) findViewById(R.id.button1);
		button2 = (Button) findViewById(R.id.button2);
		imageview = (ImageView) findViewById(R.id.imageView1);
		paired_devices = (ListView) findViewById(R.id.paired_devices);

		// 元件開始閃爍
		button1.startAnimation(animation);

		// 定義進入按鈕
		button16.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (str < 4) {
					Toast.makeText(getBaseContext(), "Please Pair Bluetooth", Toast.LENGTH_LONG).show();
				} else {
					imageview.setImageDrawable(getResources().getDrawable(R.drawable.connect));
					// 配對成功後，進入主要畫面
					Intent i = new Intent(DeviceListActivity.this, MainActivity.class);
					// 將MAC碼存入EXTRA_DEVICE_ADDRESS
					i.putExtra(EXTRA_DEVICE_ADDRESS, address);
					startActivity(i);
				}
			}
		});
		button2.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				ConfirmExit();
			}
		});
	}

	@Override
	public void onResume() {
		super.onResume();

		// 啟用藍芽，並確認裝置藍芽
		checkBTState();
		imageview.setImageDrawable(getResources().getDrawable(R.drawable.background));
		// 將配對成功的字串寫入
		mPairedDevicesArrayAdapter = new ArrayAdapter<String>(this, R.layout.device_name);
		ListView pairedListView = (ListView) findViewById(R.id.paired_devices);
		pairedListView.setAdapter(mPairedDevicesArrayAdapter);
		pairedListView.setOnItemClickListener(mDeviceClickListener);

		// 調用藍芽配置
		mBtAdapter = BluetoothAdapter.getDefaultAdapter();

		// 得到配對裝置，並新增至pairedDevices
		Set<BluetoothDevice> pairedDevices = mBtAdapter.getBondedDevices();

		// 將已配對的藍芽裝置放入陣列並顯示出來
		if (pairedDevices.size() > 0) {
			for (BluetoothDevice device : pairedDevices) {
				if (device.getName().equals("Baby Monitor CE600")) {
					mPairedDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
					address = device.getAddress();
					name = device.getName();
					str = name.length();
				}
			}
		} else {

			// 找無配對裝置時顯示
			String noDevices = getResources().getText(R.string.none_paired).toString();
			mPairedDevicesArrayAdapter.add(noDevices);
			return;
		}
	}

	// 當列表被點選時
	private OnItemClickListener mDeviceClickListener = new OnItemClickListener() {
		public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {

			// 抓取顯示出來的字串最後17碼(MAC碼)
			String info = ((TextView) v).getText().toString();
			String a = info.substring(info.length() - 17);

			// 配對成功後，進入主要畫面
			Intent i = new Intent(DeviceListActivity.this, MainActivity.class);

			// 將MAC碼存入EXTRA_DEVICE_ADDRESS
			i.putExtra(EXTRA_DEVICE_ADDRESS, a);
			startActivity(i);
		}
	};

	private void checkBTState() {

		// 檢查裝置是否有藍芽
		mBtAdapter = BluetoothAdapter.getDefaultAdapter(); // 確認藍芽開啟
		if (mBtAdapter == null) {
			Toast.makeText(getBaseContext(), "裝置不支援藍芽", Toast.LENGTH_SHORT).show();
		} else {
			if (mBtAdapter.isEnabled()) {
				Log.d(TAG, "...藍芽開啟...");
			} else {

				// 如果未開啟，則開啟藍芽
				Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
				startActivityForResult(enableBtIntent, 1);

			}
		}
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
		AlertDialog.Builder ad = new AlertDialog.Builder(DeviceListActivity.this); // 創建訊息方塊
		ad.setTitle("EXIT");
		ad.setMessage(" Are you sure you want to close this application?");
		ad.setPositiveButton("YES", new DialogInterface.OnClickListener() { // 按"是",退出應用程式
			public void onClick(DialogInterface dialog, int i) {
				//完整退出，殺死所有程序
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
