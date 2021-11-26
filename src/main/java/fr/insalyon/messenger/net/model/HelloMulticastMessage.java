package fr.insalyon.messenger.net.model;

import java.util.Date;

/**
 * Message used to say to all client in the group that the current user
 * is connected
 */
public class HelloMulticastMessage extends MulticastMessage {

    private String destination;

    public HelloMulticastMessage(String content, String sender, String destination, Date time) {
        super(content, sender, time);
        this.destination =destination;
    }


    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getDestination() {
        return destination;
    }

}
