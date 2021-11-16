package fr.insalyon.messenger.net.model;

import java.util.Date;

public class Message {

    private final String sender;
    private final String destination;
    private final Date time;

    public Message(String sender, String destination, Date time) {
        this.sender = sender;
        this.destination = destination;
        this.time = time;
    }

    public String getSender() {
        return sender;
    }

    public String getDestination() {
        return destination;
    }

    public Date getTime() {
        return time;
    }

}
