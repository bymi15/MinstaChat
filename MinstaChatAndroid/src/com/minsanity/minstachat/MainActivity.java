package com.minsanity.minstachat;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
	
	private String nickname;
	private String address;
	private String port;
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		final Button btnConnect = (Button) findViewById(R.id.connect);
		final EditText txtNickname = (EditText) findViewById(R.id.nickname);
		final EditText txtAddress = (EditText) findViewById(R.id.address);
		final EditText txtPort = (EditText) findViewById(R.id.port);
		
		 btnConnect.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				nickname = txtNickname.getText().toString();
				address = txtAddress.getText().toString();
				port = txtPort.getText().toString();
				if(!valid(nickname, address, port)){
					Toast.makeText(getApplicationContext(), "Error! Please enter valid details.", Toast.LENGTH_SHORT).show();
					return;
				}else{
					Toast.makeText(getApplicationContext(), "Attempting to connect...", Toast.LENGTH_SHORT).show();
					Intent i = new Intent(MainActivity.this, ChatActivity.class);
					i.putExtra("nickname", nickname);
					i.putExtra("address", address);
					i.putExtra("port", port);
					startActivity(i);				
				}
			}
		});
	}
	
	private boolean valid(String name, String address, String port){
		if(name == null || name == "" || name.length() > 20) return false;
		String symbols = "!@#$%^&*()`~-=+.,/?;:}{[]|/";
		for(int i = 0; i<symbols.length() - 1; i++){
			String s = String.valueOf(symbols.charAt(i));
			if(name.contains(s)) return false;
		}
		if(address.equals(0) || address.equals(null) || address.equals("")) return false;
		if(port.equals(0) || port.equals(null) || port.equals("")) return false;
		
		return true;
	}
}