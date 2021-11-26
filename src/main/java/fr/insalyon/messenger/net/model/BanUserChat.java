package fr.insalyon.messenger.net.model;

import java.util.Date;

/**
 * Message used to exchange ban requets
 */
public class BanUserChat extends Message {
    private String chatName;
    private String userName;

    public BanUserChat(String sender, String destination, Date time, String chatName, String userName) {
        super(sender, destination, time);
        this.chatName = chatName;
        this.userName = userName;
    }

    public String getChatName(){
        return this.chatName;
    }

    public String getUser(){
        return this.userName;
    }


}
