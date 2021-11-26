package fr.insalyon.messenger.net.model;

import java.util.Date;

/**
 * Message used to inform all multicast client that a user leave the group
 */
public class ByeMulticastMessage extends MulticastMessage {
    public ByeMulticastMessage(String content, String sender, Date time) {
        super(content, sender, time);
    }
}
