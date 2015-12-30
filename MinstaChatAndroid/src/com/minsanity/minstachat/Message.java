package com.minsanity.minstachat;

public class Message {
	private String fromName, message;
    private boolean isOwner;

    public Message(String fromName, String message, boolean isOwner) {
        this.fromName = fromName;
        this.message = message;
        this.isOwner = isOwner;
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


}
