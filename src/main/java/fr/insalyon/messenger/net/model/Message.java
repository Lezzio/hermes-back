package fr.insalyon.messenger.net.model;

import java.util.Date;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class Message {

    private String sender;
    private String content;
    private Date time;


    public Message(String sender, String content, Date time) {
        this.sender = sender;
        this.content = content;
        this.time = time;
    }

    public String getSender() {
        return sender;
    }

    public String getContent() {
        return content;
    }

    public Date getTime() {
        return time;
    }

//    public String JSONserializer(){
//        return gson.toJson(this);
//    }
}
