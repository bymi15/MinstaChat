package com.minstachat.server;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.util.ArrayList;

//A thread class that handles clients
public class ClientHandler extends Thread{
	private Socket clientSocket = null;
	private final ClientHandler[] t;
	private int maxClients;
	private String clientName;
	private InputStreamReader isr = null;
	private BufferedReader inputStream = null;
	private PrintStream outputStream = null;

	private static ArrayList<String> clients = new ArrayList<String>();
	
	public ClientHandler(Socket s, ClientHandler[] t){
		this.clientSocket = s;
		this.t = t;
		maxClients = t.length;
	}
	
	public void run(){
		try{
			//Creates the input and output streams
			isr = new InputStreamReader(clientSocket.getInputStream());
			inputStream = new BufferedReader(isr);
			outputStream = new PrintStream(clientSocket.getOutputStream());
			String username = inputStream.readLine();
			if(!clients.contains(username)) {
				clients.add(username); //Adds user to array list
				this.clientName = username;
				ServerCLI.console(" <" + username + "> has logged in.");
				sendOnlineUsers(); //Sends string containing online users to every client
				sendMessage("<SERVER> " + username + " has logged in.");
			} 
			else {
				this.outputStream.println("<SERVER> Sorry! You cannot connect due to an already existing username.");
				this.outputStream.println("<SERVER> Please restart your client and try another username."); //write to stream
				disconnect();
				clientSocket.close();
				inputStream.close();
				outputStream.close();
				ServerCLI.console("User: <" + username + "> failed to connect due to existance of duplicate username.");
				return;
			}
			
			//Start of server-client communication
			mainLoop:
			while(true){
				String message = inputStream.readLine(); 
				String flag = getMessageType(message); //Determines message type
				if(flag.equals("logout") || flag.equals("exit")){
					for(int i = 0; i < clients.size(); i++){ //Loops through client array
						if(clients.get(i).equals(username)){ //if it finds matching client username
							clients.remove(i); //remove client from list
							ServerCLI.console(" <" + username + "> has logged out.");
							sendOnlineUsers(); //Sends string containing online users to every client
							sendMessage("<SERVER> " + username + " has logged out.");
							
							disconnect();
							clientSocket.close();
							inputStream.close();
							outputStream.close();
							break mainLoop;
						}
					}
				}
				if(flag.equals("message")){ //if the data received is a message
					sendMessage("<"+ username + "> " + message);
					ServerCLI.console(" <"+ username + "> " + message);
				}
				if(flag.equals("pm")){ //if the data received is a private message
					String recipient = getUserToPM(message);
					String msg = getMessageToPM(message);
					synchronized(this){
						boolean found = false;
						for(int i = 0; i < maxClients; i++){
							if(t[i] != null && t[i] != this && t[i].clientName.equals(recipient) && !t[i].clientName.equals(username)){
								t[i].outputStream.println(username + " whispered to you: " + msg);
								this.outputStream.println("You whispered to " + recipient + ": " + msg); //echo to current client
								found = true;
								break; //breaks for loop
							}
						}
						if(!found) {
							this.outputStream.println("Could not whisper to: " + recipient + ", please try again.");
						}
					}
				}
			}
			
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}

	private String getUserToPM(String str){
		int index = str.indexOf(" ");
		String user = str.substring(1, index);
		return user;
	}
	
	private String getMessageToPM(String str){
		int index = str.indexOf(" ");
		String message = str.substring(index + 1);
		return message;
	}
	private String getMessageType(String s) {
		if(s.startsWith("/")){
			if(s.substring(1).equals("exit")) return "exit";
			if(s.substring(1).contains("ban")) return "ban";
			if(s.substring(1).contains("kick")) return "kick";
			if(s.substring(1).equals("logout")) return "logout";
			if(s.substring(1).contains("setadmin")) return "setadmin";
			if(s.substring(1).equals("help")) return "help";

			return "message";
		}
		else if(s.startsWith("@")){
			return "pm";
		}
		else{
			return "message";
		}
	}
	private void sendOnlineUsers(){
		String userlist = "|"; //Userlist structure: "|user1|user2|user3|user4|..etc"
		for(int j = 0; j < clients.size(); j++){
			userlist += clients.get(j) + "|";				
		}
		
		synchronized(this){
			for(int i = 0; i < maxClients; i++){
				if(t[i] != null && t[i].clientName != null){
					t[i].outputStream.println(userlist);
				}
			}
		}
	}
	
	private void disconnect(){
		synchronized(this){
			for(int k = 0; k < maxClients; k++){
				if(t[k] == this){
					t[k] = null;
				}
			}
		}
	}
	
	public void sendMessage(String message){
		synchronized(this){
			for(int i = 0; i < maxClients; i++){ //loops through every client thread
				if(t[i] != null && t[i] != this && t[i].clientName != null){ //finds the other clients (except the current client)
					t[i].outputStream.println(message); //write to stream
				}
			}
		}
	}
	
	public void writeToStream(String message){
		outputStream.println(message);
	}
	
	public String getClientName(){
		return clientName;
	}
	
}