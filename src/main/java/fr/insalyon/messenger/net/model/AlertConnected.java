package fr.insalyon.messenger.net.model;

import java.util.Date;

public class AlertConnected extends Message{
    private final String userConnected;

    public AlertConnected(String userConnected,String sender, String destination, Date time) {
        super(sender, destination, time);
        this.userConnected = userConnected;
    }

    public String getUserConnected() {
        return userConnected;
    }
}
