package fr.insalyon.messenger.net.model;

import java.util.Date;

public class CreateChat extends Message{

    private String chatName;
    private boolean state;

    public CreateChat(String sender, String destination, Date time, String chatName) {
        super(sender, destination, time);
        this.chatName = chatName;
    }

    public String getName() {
        return this.chatName;
    }

    public void setState(boolean state){
        this.state=state;
    }

    public boolean getState(){
        return state;
    }
}
