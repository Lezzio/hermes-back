package fr.insalyon.messenger.net.model;

import java.util.Date;

/**
 * Message used to try tu update the parameters of a chat
 */
public class UpdateChat extends Message {

    private String chatName;
    private String admin;

    public UpdateChat(String sender, String destination, Date time, String chatName, String admin) {
        super(sender, destination, time);

        this.chatName = chatName;
        this.admin = admin;

    }


    public String getChatName(){
        return this.chatName;
    }

    public String getAdmin(){
        return this.admin;
    }
}
