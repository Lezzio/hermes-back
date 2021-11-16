package fr.insalyon.messenger.net.model;

import java.util.Date;

public class UpdateChat extends Message{

    private String chatName;
    private boolean allAsAdmin;

    public UpdateChat(String sender, String destination, Date time, String chatName, boolean allAsAdmin) {
        super(sender, destination, time);

        this.chatName = chatName;
        this.allAsAdmin = allAsAdmin;

    }
}
