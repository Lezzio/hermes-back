package fr.insalyon.messenger.net.model;

import java.util.Date;

public class AuthenticationMessage extends Message {

    private final String password;

    public AuthenticationMessage(String username, String password, Date time) {
        super(username, "auth", time);
        this.password = password;
    }

    public String getPassword() {
        return password;
    }

}
