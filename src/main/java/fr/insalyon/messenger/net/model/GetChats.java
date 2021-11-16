package fr.insalyon.messenger.net.model;

import java.util.Date;

public class GetChats extends Message{


    public GetChats(String sender, String destination, Date time) {
        super(sender, destination, time);

    }
}
