package fr.insalyon.messenger.net.model;

import java.util.Date;

public class AuthenticationMessage extends Message {

    private final String name;
    private final String password;

    public AuthenticationMessage(String sender, String destination, Date time, String name, String password) {
        super(sender, destination, time);
        this.name = name;
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public String getPassword() {
        return password;
    }

}