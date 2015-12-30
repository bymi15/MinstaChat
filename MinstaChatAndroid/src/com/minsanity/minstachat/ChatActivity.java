package com.minsanity.minstachat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import android.app.Activity;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

public class ChatActivity extends Activity{
	
	private String nickname;
	private List<String> users = new ArrayList<String>();
	
	private BufferedReader inputStream;
	private PrintStream outputStream;
	private InputStreamReader isr;
	
	private Socket socket;
	private InetAddress ip;
	private Thread listenThread, sendThread;
	private volatile boolean running = false; //flag
	
	private Button btnSend;
	private EditText txtMessage;
	private ListView chatHistory;
	private ChatAdapter adapter;
    private List<Message> listMessages;
    
    private Handler handler;
    
    private boolean connected = false;

	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_chat);
		
		handler = new Handler();
		
		btnSend = (Button) findViewById(R.id.send);
		txtMessage = (EditText) findViewById(R.id.message);
		chatHistory = (ListView) findViewById(R.id.chathistory);
		
		String nickname = "name";
		String address = "127.0.0.1";
		String port = "5678";
		
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			nickname = extras.getString("nickname");
			address = extras.getString("address");
			port = extras.getString("port");
		}
		
		listMessages = new ArrayList<Message>();
		
		adapter = new ChatAdapter(this, listMessages);
		chatHistory.setAdapter(adapter);
		
		btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage(txtMessage.getText().toString());
                txtMessage.setText("");
            }
        });
		
		initialise(nickname, address, Integer.parseInt(port));
		
	}
	
	private void initialise(String nickname, String address, int port){
		this.nickname = nickname;
		console("Attempting to connect to " + address + ":" + port + "...", 4);
		boolean connection = openConnection(address, port);
		if(connection){
			running = true;
			console("Successfully Connected!", 4);
			console(nickname + " has logged in to " + address + ":" + port, 4);
			try {
				isr = new InputStreamReader(socket.getInputStream());
				inputStream = new BufferedReader(isr);
				outputStream = new PrintStream(socket.getOutputStream());
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else{
			console("Could not connect to server...", 4);
			console("Restart the program to reconnect.", 4);
		}
		send(nickname); //Sends the username to the server	
		receive();
	}
	
	public void console(final String message, final int type){
		handler.post(new Runnable() {
			public void run(){
				if(type == 0){
					printUserMessage(message);
				}
				else if(type == 1){
					printServerMessage(message);
				}
				else if(type == 2){
					printAdminMessage(message);
				}
				else if(type == 3){
					printWhisper(message);
				}
				else if(type == 4){
					printCommand(message);
				}
			}
		});
	}
	
	private void printUserMessage(String message){
		if(message.equals(null)) return;
		int start = message.indexOf(">") + 1;
		int startName = message.indexOf("<", start);
		int endName = message.indexOf(">", startName);
		
		String username = message.substring(startName + 1, endName);
		boolean owner = username.equals(nickname);
		String msg = message.substring(endName + 1);
		Message m = new Message(username, msg, owner);
		listMessages.add(m);
		adapter.notifyDataSetChanged();
		if (!owner) {
			playBeep();
		}
	}
	
	private void printCommand(String message){
		if(message.equals(null)) return;
		Message m = new Message("<SERVER>", message, false);
		m.setServer(true);
		listMessages.add(m);
		adapter.notifyDataSetChanged();
		playBeep();
	}
	
	private void printServerMessage(String message){
		if(message.equals(null)) return;
		int start = message.indexOf("<");
		int end = message.indexOf(">");
		String name = message.substring(start, end + 1);
		String msg = message.substring(end + 1);
		Message m = new Message(name, msg, false);
		m.setServer(true);
		listMessages.add(m);
		adapter.notifyDataSetChanged();
		playBeep();
	}
	
	private void printAdminMessage(String message){
		if(message.equals(null)) return;
		int start = message.indexOf("<");
		int end = message.indexOf(">");
		String name = message.substring(start, end + 1);
		String msg = message.substring(end + 1);
		Message m = new Message(name, msg, false);
		listMessages.add(m);
		adapter.notifyDataSetChanged();
		playBeep();
	}
	
	private void printWhisper(String message){
		if(message.equals(null)) return;
		int startIndex = message.indexOf(">") + 2;
		boolean owner = message.indexOf("You") == startIndex;
		
		String fromName = message.substring(startIndex, message.indexOf(":"));
		String msg = message.substring(message.indexOf(":") + 1);
		Message m = new Message(fromName, msg, owner);
		m.setWhisper(true);
		listMessages.add(m);
		adapter.notifyDataSetChanged();
		playBeep();
	}
	
	private boolean openConnection(final String address, final int port){
		Thread t = new Thread(){
			public void run(){
				try {
					ip = InetAddress.getByName(address);
					socket = new Socket(ip, port);
				} catch (IOException e) {
					e.printStackTrace();
					connected = false;
				}
				connected = true;
			}
		};
		t.start();
		try {
			t.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return connected;
	}
	
	public void sendMessage(String message){
		if(message.trim().equals("")) return;
		
		if(getMessageType(message).equals("pm")){
			send(message);
			txtMessage.setText("");
		}else if(getMessageType(message).equals("command")){ //type: command
			send(message);
			txtMessage.setText("");
			int index = message.indexOf(" ");
			if(index > 0){
				String command = message.substring(1, index);
				console("<COMMAND> " + command + " requested to server.", 4);
				if(command.equals("logout")){
					finish();
				}
				if(command.equals("exit")){
					finishAffinity();
				}
			}
			else{
				console("<COMMAND> " + message.substring(1) + " requested to server.", 4);
			}
		}else{
			send(message);
			console("<USERMESSAGE> <" + nickname + "> " + message, 0);
			txtMessage.setText("");
		}
	}
	
	private void send(final String message){
		sendThread = new Thread(new Runnable(){
			public void run(){
				outputStream.println(message);
			}
		});
		sendThread.start();
	}
	
	private void receive(){
		listenThread = new Thread(new Runnable(){
			public void run(){
				while(running){
					try {
						String s = inputStream.readLine();
						if(s != null && s != ""){
							if(getMessageType(s).equals("userlist")) getUserList(s);
							else if(getMessageType(s).equals("whisper")) console(s, 3);
							else if(getMessageType(s).equals("server")) console(s, 1);
							else if(getMessageType(s).equals("admin")) console(s, 2);
							else if(getMessageType(s).equals("command")) parseServerCommand(s);
							else console(s, 0);
						}
					} catch (IOException e) {
						console("Disconnected from server...", 4);
						console("Restart the client to reconnect.", 4);
						running = false;
					}
				}
			}
		});
		listenThread.start();
	}
	
	private void parseServerCommand(String s){
		if(s.startsWith("/disconnect")){
			disconnect();
		}
	}
	
	private void disconnect(){
		try {
			outputStream.close();
			inputStream.close();
			isr.close();
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private String getMessageType(String msg){
		if(msg.startsWith("@")) return "pm";
		if(msg.startsWith("/")) return "command";
		if(msg.startsWith("|") && msg.endsWith("|")) return "userlist";
		if(msg.startsWith("<USERMESSAGE>")) return "message";
		if(msg.startsWith("<WHISPER>")) return "whisper";
		if(msg.startsWith("<SERVER>")) return "server";
		if(msg.startsWith("<ADMIN>")) return "admin";
		return "message";
	}
	
	private void getUserList(String str){ //decodes string containing users and adds it to users array list
		if(!str.startsWith("|") || !str.endsWith("|") || str.equals("|") || str.equals(null)) return;
		String s = str.substring(1, str.length() - 1); //eg: from "|user1|user2|user3|" to "user1|user2|user3"
		String[] arr = s.split("\\|"); //eg: from "user1|user2|user3" to "user1", "user2", "user3"
		users.clear();
		for(String usr : arr){
			users.add(usr); //adds each string in the array to the users array list
		}
		/*
		//Update GUI listbox
		DefaultListModel<String> dlm = new DefaultListModel<String>();
		for(int i = 0; i < users.size(); i++){
			dlm.addElement(users.get(i));
		}
		updateListGUI(dlm); */
	}
	
	/**
     * Plays device's default notification sound
     * */
    public void playBeep() {
 
        try {
            Uri notification = RingtoneManager
                    .getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            Ringtone r = RingtoneManager.getRingtone(getApplicationContext(),
                    notification);
            r.play();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @Override
    public void onBackPressed(){
    	try {
			running = false;
			listenThread.interrupt();
			sendThread.interrupt();
			if(outputStream != null && inputStream != null && socket != null){
				outputStream.println("/logout");
				inputStream.close();
				outputStream.close();
				socket.close();
			}
		}catch (IOException e) {
			e.printStackTrace();
		}
    	finish();
    	return;
    }
    
    @Override
    public void onDestroy(){
    	super.onDestroy();
    	try {
			running = false;
			listenThread.interrupt();
			sendThread.interrupt();
			if(outputStream != null && inputStream != null && socket != null){
				outputStream.println("/logout");
				inputStream.close();
				outputStream.close();
				socket.close();
			}
		}catch (IOException e) {
			e.printStackTrace();
		}
    	finish();
    	return;
    }
}
