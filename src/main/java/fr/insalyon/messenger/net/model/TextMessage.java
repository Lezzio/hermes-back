package fr.insalyon.messenger.net.model;

import java.util.Date;

/**
 * Message used to exchange text message
 */
public class TextMessage extends Message {

    private final String content;

    private boolean isSpecialEvent;

    public TextMessage(String content, String sender, String destination, Date time) {
        super(sender, destination, time);
        this.content = content;
        this.isSpecialEvent = false;
    }

    public String getContent() {
        return content;
    }

    public void setSpecialEvent(){
        this.isSpecialEvent = true;
    }

    public boolean isSpecialEvent() {
        return this.isSpecialEvent;
    }


}
