package fr.insalyon.messenger.net.model;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Message used to get users that belong to a specific chat
 * All users are associated with a boolean -> true if the user is connected
 */
public class GetUsers extends Message {
    private Map<String, Boolean> connected;


    public GetUsers(String sender, String destination, Date time) {
        super(sender, destination, time);
        connected = new HashMap<String, Boolean>();
    }

    public void setConnected(Map<String, Boolean> connected) {
        this.connected = connected;
    }

    public Map<String, Boolean> getUsersConnected() {
        return connected;
    }
}
