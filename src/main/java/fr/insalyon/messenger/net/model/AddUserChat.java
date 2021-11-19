package fr.insalyon.messenger.net.model;

import java.util.Date;
import java.util.List;

public class AddUserChat extends Message {
    private String chatName;
    private List<String> userName;

    public AddUserChat(String sender, String destination, Date time, String chatName, List<String> userName) {
        super(sender, destination, time);
        this.chatName = chatName;
        this.userName = userName;
    }

    public String getChatName(){
        return this.chatName;
    }

    public List<String> getUsers(){
        return this.userName;
    }
}
