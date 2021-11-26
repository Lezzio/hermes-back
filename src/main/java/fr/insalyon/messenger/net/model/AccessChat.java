package fr.insalyon.messenger.net.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Messages used to acces a chat and get all messages of the chat
 */
public class AccessChat extends Message {
    private String chatName;
    private int users;
    private List<TextMessage> messages;
    private String admin;

    public AccessChat(String sender, String destination, Date time, String chatName) {
        super(sender, destination, time);
        this.chatName = chatName;
        this.users = 0;
        this.messages = new ArrayList<TextMessage>();
        this.admin="";
    }

    public String getChatName(){
        return this.chatName;
    }


    public void setUsers(int users) {
        this.users = users;
    }

    public void setMessages(List<TextMessage> messages) {
        this.messages = messages;
    }

    public void setAdmin(String admin) {
        this.admin = admin;
    }

    public void add(TextMessage textMessage) {
        messages.add(textMessage);
    }

    public void setName(String chatName) {
        this.chatName = chatName;
    }
}
