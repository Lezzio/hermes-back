package fr.insalyon.messenger.net.mongodb;

import java.util.Date;

public class User {
    private String username;
    private Date previous_connection;

    public User(String username, Date previous_connection){
        this.username = username;
        this.previous_connection = previous_connection;
    }

    public String getUsername(){
        return username;
    }

    public Date getPreviousConnection(){
        return previous_connection;
    }
}
