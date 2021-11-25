package fr.insalyon.messenger.net.model;

import java.util.Date;

public class ByeMultiCastMessage extends MulticastMessage {
    public ByeMultiCastMessage(String content, String sender, Date time) {
        super(content, sender, time);
    }
}
