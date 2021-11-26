package fr.insalyon.messenger.net.model;

import java.util.Date;

/**
 * Message used to send content in a group chat
 */
public class MulticastMessage {

    private final String content;
    private final String sender;
    private final Date time;

    public MulticastMessage(String content, String sender, Date time) {
        this.content = content;
        this.sender = sender;
        this.time = time;
    }

    public String getContent() {
        return content;
    }

    public String getSender() { return sender;}

    public Date getTime() {
        return time;
    }
}
