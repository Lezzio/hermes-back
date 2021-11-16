package fr.insalyon.messenger.net.model;

import java.util.Date;

public class ConnectionMessage extends Message{
    private String password;


    public ConnectionMessage(String sender, String password, Date time) {
        super(sender, "server", time);
        this.password = password;
    }
}
