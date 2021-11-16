package fr.insalyon.messenger.net.model;

import java.util.Date;

public class AccessChat extends Message{
    private String chatName;

    public AccessChat(String sender, String destination, Date time, String chatName) {
        super(sender, destination, time);
        this.chatName = chatName;
    }
}
