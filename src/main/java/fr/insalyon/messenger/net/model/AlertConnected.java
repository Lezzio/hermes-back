package fr.insalyon.messenger.net.model;

import java.util.Date;

/**
 * Message used to alert that someone is connected in the chat application
 */
public class AlertConnected extends Message{
    private final String userConnected;
    private Date previousConnection;

    public AlertConnected(String userConnected,String sender, String destination, Date time) {
        super(sender, destination, time);
        this.userConnected = userConnected;
        previousConnection = null;
    }

    public String getUserConnected() {
        return userConnected;
    }

    public void setPreviousConnection(Date previousConnection) {
        this.previousConnection = previousConnection;
    }

    public Date getPreviousConnection() {
        return this.previousConnection;
    }
}
