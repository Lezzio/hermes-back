package fr.insalyon.messenger.net.model;

import java.util.Date;

public class AddNotification extends Notification{
    private LogChat chat;

    public AddNotification(String content, String sender, String destination, Date time) {
        super(content,sender, destination, time,"add");
    }



    public void setChat(LogChat logChat) {
        this.chat = logChat;
    }

    public LogChat getChat() {
        return this.chat;
    }
}
