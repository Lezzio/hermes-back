package fr.insalyon.messenger.net.model;

import java.util.List;

public class LogChat {
    private String name;
    private List<String> users;
    private TextMessage message;
    private int usersNumber;
    private String admin;

    public LogChat(String name, List<String> users, TextMessage message) {
        this.name = name;
        this.users = users;
        this.message = message;
        this.usersNumber = users.size();
    }

    public String getName() {
        return this.name;
    }

    public List<String> getUsers() {
        return this.users;
    }
}
