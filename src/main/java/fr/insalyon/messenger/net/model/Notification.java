package fr.insalyon.messenger.net.model;

import java.util.Date;

public class Notification extends Message{
    private String typeNotification;
    private String content;

    public Notification(String content, String sender, String destination, Date time, String typeNotification) {
        super(sender, destination, time);
        this.typeNotification = typeNotification;
        this.content = content;
    }

    public String getType() {
        return typeNotification;
    }

    public String getContent() {
        return this.content;
    }
}
