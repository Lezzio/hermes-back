package fr.insalyon.messenger.net.mongodb;

import java.util.Date;

/**
 * Allows to create a user with a mongoDB request
 */
public class User {
    private String username;
    private Date previous_connection;

    /**
     * Constructor to create a user
     * @param username the user name
     * @param previous_connection the previous connection
     */
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
