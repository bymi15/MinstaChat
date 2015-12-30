package com.minstachat.server;

import java.io.IOException;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Server implements Runnable{
	
	private int port;
	public static final int MAXCLIENTS = 31;
	private boolean running = false;
	private final ClientHandler[] clients = new ClientHandler[MAXCLIENTS];
	
	public Server(int port){
		this.port = port;
		running = true;
	}

	public void run(){
		ServerSocket serverSocket = null;
		try {
			//Starts the server on specified port
			serverSocket = new ServerSocket(port);
		} catch (IOException e) { //Error handling
			System.err.println("Could not listen on port: " + port);
			System.err.println("Port is busy");
			System.exit(1);
		}
		
		ServerCLI.console("Server initialised on port " + port);
		
		while(running){ //Main server loop that handles client threads
			try {
				//Listens and accepts clients
				Socket client = serverSocket.accept();
				ServerCLI.console(client.toString());
				//Start the client manager thread for each client
				int n = 0;
				for(n = 0; n < MAXCLIENTS; n++){
					if(clients[n] == null){
						clients[n] = new ClientHandler(client, clients);
						clients[n].start(); //Starts thread
						break;
					}
				}
				if(n == MAXCLIENTS-1){
					PrintStream outputStream = new PrintStream(client.getOutputStream());
					outputStream.println("Server is full (MAX " + MAXCLIENTS+1 + " clients). Please try again later.");
					outputStream.close();
					client.close();
					serverSocket.close();
				}
			} catch (IOException e) { //Error handling
				e.printStackTrace();
				System.err.println("Could not accept client.");
			}
			
		}
	}
	
	public void broadcast(String message){
		synchronized(this){
			for(int i = 0; i < MAXCLIENTS; i++){ //loops through every client thread
				if(clients[i] != null && clients[i].getClientName() != null){ //finds the other clients (except the current client)
					clients[i].writeToStream("<SERVER> " + message); //write to stream
				}
			}
		}
		ServerCLI.print("<SERVER> " + message);
	}
	
	public void disconnect(){
		synchronized(this){
			for(int i = 0; i < MAXCLIENTS; i++){ //loops through every client thread
				if(clients[i] != null && clients[i].getClientName() != null){ //finds the other clients (except the current client)
					clients[i].writeToStream("/disconnect"); //write to stream
				}
			}
		}
		running = false;
	}
	
	public ArrayList<String> getOnlineUsers(){
		ArrayList<String> onlineUsers = new ArrayList<String>();
		synchronized(this){
			for(int i = 0; i < MAXCLIENTS; i++){ //loops through every client thread
				if(clients[i] != null && clients[i].getClientName() != null){ //finds the other clients (except the current client)
					onlineUsers.add(clients[i].getClientName());
				}
			}
		}
		return onlineUsers;
	}
}
