package fr.insalyon.messenger.net.model;

import java.util.Date;

public class ByeMulticastMessage extends MulticastMessage {
    public ByeMulticastMessage(String content, String sender, Date time) {
        super(content, sender, time);
    }
}
