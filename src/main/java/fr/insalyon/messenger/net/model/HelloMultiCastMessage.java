package fr.insalyon.messenger.net.model;

import java.util.Date;

public class HelloMultiCastMessage extends MulticastMessage {

    private String destination;

    public HelloMultiCastMessage(String content, String sender,String destination, Date time) {
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
