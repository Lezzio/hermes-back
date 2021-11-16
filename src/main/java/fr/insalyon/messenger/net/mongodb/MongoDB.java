package fr.insalyon.messenger.net.mongodb;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import org.bson.Document;


public class MongoDB {
    private final MongoClient MONGOCLIENT = MongoClients.create("mongodb+srv://admin:root@cluster0.h6mqd.mongodb.net/myFirstDatabase?retryWrites=true&w=majority");
    private MongoDatabase database;
    public MongoDB() {
        this.database = MONGOCLIENT.getDatabase("hermes");
    }

    public void insertLogMessage(String msg){
        MongoCollection<Document> logs = database.getCollection("log");
        logs.insertOne(Document.parse(msg));
    }

    public void insertPrivateMessage(String msg){
        MongoCollection<Document> logs = database.getCollection("privatemessages");
        logs.insertOne(Document.parse(msg));
    }

    public void insertGroupMessage(String msg){
        MongoCollection<Document> logs = database.getCollection("groupmessages");
        logs.insertOne(Document.parse(msg));
    }

    public void insertUser(String msg){
        MongoCollection<Document> logs = database.getCollection("users");
        logs.insertOne(Document.parse(msg));
    }



}
