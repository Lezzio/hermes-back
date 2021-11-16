package fr.insalyon.messenger.net.model;

import java.util.Date;

public class DisconnectionMessage extends Message {



    public DisconnectionMessage(String sender,  Date time) {
        super(sender, "server",time);

    }
}
