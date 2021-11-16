package fr.insalyon.messenger.net.model;

import java.util.Date;

public class TextMessage extends Message {

    private final String content;

    public TextMessage(String content, String sender, String destination, Date time) {
        super(sender, destination, time);
        this.content = content;
    }

    public String getContent() {
        return content;
    }
}
