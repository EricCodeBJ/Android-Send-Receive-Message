package com.kidevstudio.sendreceivemessage.events;

public class NewMessageEvent {
    public String message;
    public String sender;

    public NewMessageEvent(String sender, String message){
        this.sender = sender;
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }
}