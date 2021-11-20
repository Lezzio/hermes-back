package fr.insalyon.messenger.net.model;

import java.util.Date;

public class Notification extends Message{
    private String type;
    private String content;

    public Notification(String content, String sender, String destination, Date time, String type) {
        super(sender, destination, time);
        this.type = type;
        this.content = content;
    }

    public String getType() {
        return type;
    }

    public String getContent() {
        return this.content;
    }
}
