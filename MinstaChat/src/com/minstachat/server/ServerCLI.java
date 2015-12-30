package com.minstachat.server;

import java.util.ArrayList;
import java.util.Scanner;

public class ServerCLI implements Runnable{
	
	private Server server;
	private boolean running = false;
	
	public static void main(String[] args){
		int port;
		if(args.length != 1) {
			System.out.println("Usage: java -jar MinstaChatServer.jar [port]");
			return;
		}
		
		port = Integer.parseInt(args[0]);
		
		new ServerCLI(port);
	}
	
	public ServerCLI(int port){
		server = new Server(port);
		Thread serverThread = new Thread(server);
		serverThread.start();
		
		running = true;
		
		Thread uiThread = new Thread(this);
		uiThread.start();
	}
	
	public void run(){
		Scanner scan = new Scanner(System.in);
		
		while(running){
			String line = scan.nextLine();
			if(!line.startsWith("/") && !line.trim().equals("")){
				server.broadcast(line);
			}else{
				if(line.equals("/quit") || line.equals("/stop") || line.equals("/exit")){
					server.broadcast("The server is shutting down...");
					server.disconnect();
					running = false;
				}else if(line.equals("/help")){
					printHelpMessage();
				}else if(line.equals("/users")){
					printOnlineUsers();
				}else{
					print("Unknown command: " + line);
					print("");
					printHelpMessage();
				}
			}
		}
		
		scan.close();
		System.exit(0);
	}
	
	private void printHelpMessage(){
		print("List of server commands:");
		print("=======================================");
		print("/quit - shut down server");
		print("/users - lists all the connected users");
		print("/help - displays the help message");
		print("");
	}
	
	private void printOnlineUsers(){
		print("Online Users:");
		ArrayList<String> onlineUsers = server.getOnlineUsers();
		if(onlineUsers.size() == 0){
			print("===No users online===");
			print("");
		}
		for(int i = 0; i < onlineUsers.size(); i++){
			console(onlineUsers.get(i));						
		}
		print("");
	}
	public static void console(String msg){
		System.out.println(">>" + msg);
	}
	
	public static void print(String msg){
		System.out.println(msg);
	}
	
	
	
}