package fr.insalyon.messenger.net.mongodb;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.*;

import org.bson.Document;


public class MongoDB {

    private final ConnectionString connectionString = new ConnectionString("mongodb+srv://admin:admin@cluster0.h6mqd.mongodb.net/hermes?retryWrites=true&w=majority");
    private final MongoClientSettings SETTING = MongoClientSettings.builder()
            .applyConnectionString(connectionString)
            .build();


    private final MongoClient MONGOCLIENT = MongoClients.create(SETTING);
    private MongoDatabase database;
    public MongoDB() {
        this.database = MONGOCLIENT.getDatabase("hermes");
    }

    public void insertLogMessage(String msg){
        System.out.println(msg);
        MongoCollection<Document> logs = database.getCollection("test");
        System.out.println(msg);
        Document doc = Document.parse(msg);
        System.out.println(msg);
        System.out.println(doc.toString());
        logs.insertOne(doc);
        try {
            logs.insertOne(doc);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println(msg);
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
