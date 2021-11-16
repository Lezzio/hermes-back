package fr.insalyon.messenger.net.model;

import java.util.Date;

public class AddUserChat extends Message{
    private String chatName;
    private String userName;

    public AddUserChat(String sender, String destination, Date time, String chatName, String userName) {
        super(sender, destination, time);
        this.chatName = chatName;
        this.userName = userName;
    }
}
