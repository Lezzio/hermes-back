package fr.insalyon.messenger.net.model;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class GetUsers extends Message {
    private Map<String, Boolean> connected;


    public GetUsers(String sender, String destination, Date time) {
        super(sender, destination, time);
        connected = new HashMap<String, Boolean>();
    }

    public void setConnected(Map<String, Boolean> connected) {
        this.connected = connected;
    }
}
