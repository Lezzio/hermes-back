package fr.insalyon.messenger.net.mongodb;

import com.mongodb.BasicDBObject;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.*;

import com.mongodb.client.model.Filters;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.model.Updates;
import fr.insalyon.messenger.net.model.*;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;


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

    public void insertLogMessage(String msg) {
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

    public void insertUser(ConnectionMessage msg) {
        MongoCollection<Document> logs = database.getCollection("users");
        Document name = new Document("userName", msg.getName());
        name.append("last_connection", (new Date().getTime()));
        logs.insertOne(name);
    }

    public User searchUser(String name) {
        Document result = database.getCollection("users").find(Filters.eq("userName", name)).first();
        if (result != null) {
            Bson updates = Updates.combine(Updates.set("last_connection", (new Date().getTime())));
            UpdateOptions options = new UpdateOptions().upsert(true);
            database.getCollection("users").updateOne(Filters.eq("userName", name), updates, options);

            return new User(result.getString("userName"), new Date(result.getLong("last_connection")));
        }
        return null;
    }


    public String searchChat(String name) {
        Document result = database.getCollection("chats").find(Filters.eq("chatName", name)).first();
        if (result != null) {
            return result.getString("chatName");
        }
        return null;
    }

    public void insertLog(TextMessage msg) {
        MongoCollection<Document> logs = database.getCollection("log");
        Document log = new Document("content", msg.getContent());
        log.append("sender", msg.getSender());
        log.append("destination", msg.getDestination());
        log.append("time", msg.getTime().getTime());
        logs.insertOne(log);
    }

    public void insertMessages(TextMessage msg) {
        MongoCollection<Document> logs = database.getCollection("messages");
        Document log = new Document("content", msg.getContent());
        log.append("chatName", msg.getDestination());
        log.append("sender", msg.getSender());
        log.append("destination", msg.getDestination());
        log.append("time", msg.getTime().getTime());
        logs.insertOne(log);
    }

    public void insertChat(CreateChat chat) {
        MongoCollection<Document> logs = database.getCollection("chats");
        Document chats = new Document("chatName", chat.getName());
        chats.append("date_last_messsage", chat.getTime().getTime());
        chats.append("admin", chat.getDestination());
        List<String> users = new ArrayList<>();
        users.add(chat.getDestination());
        chats.append("users", users);
        logs.insertOne(chats);
        //Insert a default message saying the group has been created successfully
        insertMessages(new TextMessage("The group has been created", chat.getName(), chat.getName(), new Date()));
    }

    public void addChatUsers(String chatName, List<String> users) {
        Bson updates = Updates.combine(Updates.addEachToSet("users", users));
        UpdateOptions options = new UpdateOptions().upsert(true);
        database.getCollection("chats").updateOne(Filters.eq("chatName", chatName), updates, options);
    }

    public void banChatUser(String chatName, String user) {
        Bson updates = Updates.combine(Updates.pull("users", user));
        UpdateOptions options = new UpdateOptions().upsert(true);
        database.getCollection("chats").updateOne(Filters.eq("chatName", chatName), updates, options);
    }


    public Chat getChat(String chatName) {
        Document result = database.getCollection("chats").find(Filters.eq("chatName", chatName)).first();
        if (result != null) {
            Chat chat = new Chat(result.getString("chatName"), result.getList("users", String.class).size(), new ArrayList<>(), result.getString("admin"));
            FindIterable<Document> resultMessage = database.getCollection("messages").find(Filters.eq("chatName", chatName)).sort(new BasicDBObject("time", 1));
            for (Document msg : resultMessage) {
                TextMessage message = new TextMessage(msg.getString("content"), msg.getString("sender"), msg.getString("destination"), new Date(msg.getLong("time")));
                chat.add(message);
            }
            return chat;
        }
        return null;
    }

    public List<LogChat> getChats(String username) {
        System.out.println("getChats: Username = " + username);
        FindIterable<Document> result = database.getCollection("chats").find(Filters.eq("users", username)).sort(new BasicDBObject("date_last_messsage", -1));
        List<LogChat> chatList = new ArrayList<>();
        for (Document doc : result) {
            Document msg = database.getCollection("messages").find(Filters.eq("chatName", doc.getString("chatName"))).sort(new BasicDBObject("time", -1)).first();
            if(msg != null) {
                LogChat chat = new LogChat(doc.getString("chatName"), doc.getList("users", String.class), new TextMessage(msg.getString("content"), msg.getString("sender"), msg.getString("destination"), new Date(msg.getLong("time"))));
                chatList.add(chat);
            }
        }
        chatList.forEach(chat -> {
            System.out.println(chat.getName());
        });
        return chatList;
    }

    public void removeChatUser(String name, String user) {
        Bson updates = Updates.combine(Updates.pull("users", user));
        UpdateOptions options = new UpdateOptions().upsert(true);
        database.getCollection("chats").updateOne(Filters.eq("chatName", name), updates, options);
    }

    public boolean updateChat(UpdateChat updateChat, boolean nameChanged) {
        if (nameChanged) {
            Document result = database.getCollection("chats").find(Filters.eq("chatName", updateChat.getChatName())).first();
            if (result != null) {
                return false;
            }
        }
        Bson updates = Updates.combine(Updates.set("chatName", updateChat.getChatName()), Updates.set("admin", updateChat.getAdmin()));
        UpdateOptions options = new UpdateOptions().upsert(true);
        database.getCollection("chats").updateOne(Filters.eq("chatName", updateChat.getChatName()), updates, options);
        return true;
    }

    public void insertNotification(Notification notification) {
        MongoCollection<Document> logs = database.getCollection("notifications");
        Document log = new Document("content", notification.getContent());
        log.append("sender", notification.getSender());
        log.append("destination", notification.getDestination());
        log.append("time", notification.getTime().getTime());
        log.append("type", notification.getType());
        logs.insertOne(log);
    }


    public List<Notification> getNotifications(String user) {
        FindIterable<Document> result = database.getCollection("notifications").find(Filters.eq("destination", user)).sort(new BasicDBObject("time", 1));
        List<Notification> notifications = new ArrayList<Notification>();
        for (Document doc : result) {
            notifications.add(
                    new Notification(doc.getString("content"),
                            doc.getString("sender"),
                            doc.getString("destination"),
                            new Date(doc.getLong("time")),
                            doc.getString("type"))
            );
        }
        return notifications;
    }

    public List<String> getUsersAddable(List<String> currentUsers) {
        FindIterable<Document> result = database.getCollection("users").find();
        List<String> users = new ArrayList<String>();
        for (Document doc : result) {
            if (!currentUsers.contains(doc.getString("userName"))) {
                users.add(doc.getString("userName"));
            }
        }
        return users;
    }
}
