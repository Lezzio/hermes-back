package fr.insalyon.messenger.net.model;

import java.util.Date;

/**
 * Message used to ask to leave a chat
 */
public class LeaveChat extends Message {

    private String chatName;

    public LeaveChat(String sender, String destination, Date time, String chatName) {
        super(sender, destination, time);
        this.chatName = chatName;
    }

    public String getName() {
        return chatName;
    }
}
