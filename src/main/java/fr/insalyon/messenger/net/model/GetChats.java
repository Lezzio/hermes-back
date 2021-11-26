package fr.insalyon.messenger.net.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Message used to get the list of chats of a user
 */
public class GetChats extends Message {
    private List<LogChat> logChats;

    public GetChats(String sender, String destination, Date time) {
        super(sender, destination, time);
        this.logChats = new ArrayList<LogChat>();
    }

    public void add(LogChat chat){
        logChats.add(chat);
    }

    public void setLogChat(List<LogChat> logchats) {
        this.logChats = logchats;
    }

    public List<LogChat> getChats() {
        return logChats;
    }
}
