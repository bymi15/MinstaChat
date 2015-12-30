package com.minsanity.minstachat;

public class Message {
	private String fromName, message;
    private boolean isOwner;
    private boolean isServer;
    private boolean isWhisper;

    public Message(String fromName, String message, boolean isOwner) {
        this.fromName = fromName;
        this.message = message;
        this.isOwner = isOwner;
        this.isServer = false;
        this.isWhisper = false;
    }
    
    public String getFromName() {
        return fromName;
    }
 
    public void setFromName(String fromName) {
        this.fromName = fromName;
    }
 
    public String getMessage() {
        return message;
    }
 
    public void setMessage(String message) {
        this.message = message;
    }
 
    public boolean isOwner() {
        return isOwner;
    }
 
    public void setOwner(boolean owner) {
        this.isOwner = owner;
    }
    
    public void setServer(boolean server){
    	this.isServer = server;
    }
    
    public void setWhisper(boolean whisper){
    	this.isWhisper = whisper;
    }
    
    public boolean isServer(){
    	return isServer;
    }
    
    public boolean isWhisper(){
    	return isWhisper;
    }


}
