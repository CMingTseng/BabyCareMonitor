package com.example.babymonitorce600;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;

public class DialogRed extends Activity {
	// 宣告元件
	Button btn_confirm;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.dialog_red);

		btn_confirm = (Button) findViewById(R.id.btn_confirm);

		btn_confirm.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(DialogRed.this, MainActivity.class);
				startActivity(intent);
			}
		});

	}

}
