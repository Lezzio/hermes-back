package fr.insalyon.messenger.net.model;

import java.util.Date;

public class AlertDisconnected extends Message{
    private final String userDisconnected;

    public AlertDisconnected(String userConnected,String sender, String destination, Date time) {
        super(sender, destination, time);
        this.userDisconnected = userConnected;
    }

    public String getUserDisconnected() {
        return userDisconnected;
    }
}
