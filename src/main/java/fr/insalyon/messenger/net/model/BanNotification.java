package fr.insalyon.messenger.net.model;

import java.util.Date;

public class BanNotification extends Notification{




    public BanNotification(String content,String sender, String destination, Date time) {
        super(content,sender, destination, time,"ban");
    }




}
