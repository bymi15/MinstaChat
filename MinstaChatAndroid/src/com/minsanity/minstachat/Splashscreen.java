package com.minsanity.minstachat;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class Splashscreen extends Activity{
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_splashscreen);
		

		Thread t = new Thread(){
			public void run(){
				try {
					sleep(3000);
					Intent i = new Intent(Splashscreen.this, MainActivity.class);
					startActivity(i);
					finish();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		};
		t.start();
	}
}
