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

/**
 * Allows to handle request with the mongoDB database
 */
public class MongoDB {

    /**
     * MongoDB setting to connect into the cluster
     */
    private final ConnectionString connectionString = new ConnectionString("mongodb+srv://admin:admin@cluster0.h6mqd.mongodb.net/hermes?retryWrites=true&w=majority");
    private final MongoClientSettings SETTING = MongoClientSettings.builder()
            .applyConnectionString(connectionString)
            .build();


    private final MongoClient MONGOCLIENT = MongoClients.create(SETTING);
    private MongoDatabase database;

    /**
     * Constructor allows to initialize the database
     */
    public MongoDB() {
        this.database = MONGOCLIENT.getDatabase("hermes");
    }


    /**
     * Inserts a user in the collection
     * @param msg the connection msg that contains the user name
     */
    public void insertUser(ConnectionMessage msg) {
        MongoCollection<Document> logs = database.getCollection("users");
        Document name = new Document("userName", msg.getName());
        name.append("last_connection", (new Date().getTime()));
        logs.insertOne(name);
    }

    /**
     * Allows searching a user in the collection
     * @param name the user name
     * @return the user found
     */
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

    /**
     * Allows searching a chat in a collection
     * @param name the chat name
     * @return
     */
    public String searchChat(String name) {
        Document result = database.getCollection("chats").find(Filters.eq("chatName", name)).first();
        if (result != null) {
            return result.getString("chatName");
        }
        return null;
    }

    /**
     * Inserts a log message in a collection
     * @param msg the log message
     */
    public void insertLog(TextMessage msg) {
        MongoCollection<Document> logs = database.getCollection("log");
        Document log = new Document("content", msg.getContent());
        log.append("sender", msg.getSender());
        log.append("destination", msg.getDestination());
        log.append("time", msg.getTime().getTime());
        logs.insertOne(log);
    }

    /**
     * Inserts a message in a collection
     * @param msg the message
     */
    public void insertMessages(TextMessage msg) {
        MongoCollection<Document> logs = database.getCollection("messages");
        Document log = new Document("content", msg.getContent());
        log.append("chatName", msg.getDestination());
        log.append("sender", msg.getSender());
        log.append("destination", msg.getDestination());
        log.append("time", msg.getTime().getTime());
        logs.insertOne(log);
    }

    /**
     * Inserts a chat in a collection
     * @param chat the create chat message
     */
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

    /**
     * Allows adding users in a chat in a collection
     * @param chatName the chat name
     * @param users the list of user
     */
    public void addChatUsers(String chatName, List<String> users) {
        Bson updates = Updates.combine(Updates.addEachToSet("users", users));
        UpdateOptions options = new UpdateOptions().upsert(true);
        database.getCollection("chats").updateOne(Filters.eq("chatName", chatName), updates, options);
    }

    /**
     * Allows banning a user in a chat in a collection
     * @param chatName the chat name
     * @param user the list of user
     */
    public void banChatUser(String chatName, String user) {
        Bson updates = Updates.combine(Updates.pull("users", user));
        UpdateOptions options = new UpdateOptions().upsert(true);
        database.getCollection("chats").updateOne(Filters.eq("chatName", chatName), updates, options);
    }

    /**
     * Allows to get the messages of a chat
     * @param chatName the chatname
     * @return the chat getted
     */
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

    /**
     * Allows to get the list of chat belongs to a user in a collection
     * @param username the username
     * @return the list of chat
     */
    public List<LogChat> getChats(String username) {
        FindIterable<Document> result = database.getCollection("chats").find(Filters.eq("users", username)).sort(new BasicDBObject("date_last_messsage", -1));
        List<LogChat> chatList = new ArrayList<>();
        for (Document doc : result) {
            Document msg = database.getCollection("messages").find(Filters.eq("chatName", doc.getString("chatName"))).sort(new BasicDBObject("time", -1)).first();
            if(msg != null) {
                LogChat chat = new LogChat(doc.getString("chatName"), doc.getList("users", String.class), new TextMessage(msg.getString("content"), msg.getString("sender"), msg.getString("destination"), new Date(msg.getLong("time"))));
                chatList.add(chat);
            }
        }

        return chatList;
    }

    /**
     * Allows removing a user in a chat in a collection
     * @param name the chat name
     * @param user the user name
     */
    public void removeChatUser(String name, String user) {
        Bson updates = Updates.combine(Updates.pull("users", user));
        UpdateOptions options = new UpdateOptions().upsert(true);
        database.getCollection("chats").updateOne(Filters.eq("chatName", name), updates, options);
    }

    /**
     * Allows to update a chat in a collection
     * @param updateChat the update request
     * @param nameChanged true if we want to update a name
     * @return the result of the update
     */
    public boolean updateChat(UpdateChat updateChat, boolean nameChanged) {
        if (nameChanged) {
            Document result = database.getCollection("chats").find(Filters.eq("chatName", updateChat.getChatName())).first();
            if (result != null) {
                return false;
            }
            Bson updates = Updates.combine(Updates.set("chatName", updateChat.getChatName()));
            UpdateOptions options = new UpdateOptions().upsert(true);
            database.getCollection("messages").updateMany(Filters.eq("chatName", updateChat.getDestination()), updates, options);
        }
        Bson updates = Updates.combine(Updates.set("chatName", updateChat.getChatName()), Updates.set("admin", updateChat.getAdmin()));
        UpdateOptions options = new UpdateOptions().upsert(true);
        database.getCollection("chats").updateOne(Filters.eq("chatName", updateChat.getDestination()), updates, options);
        return true;
    }

    /**
     * Inserts a notification in a collection
     * @param notification the notification
     */
    public void insertNotification(Notification notification) {
        MongoCollection<Document> logs = database.getCollection("notifications");
        Document log = new Document("content", notification.getContent());
        log.append("sender", notification.getSender());
        log.append("destination", notification.getDestination());
        log.append("time", notification.getTime().getTime());
        log.append("type", notification.getType());
        logs.insertOne(log);
    }

    /**
     * Allows to get the list of notifications of a user
     * @param user the user name
     * @return the list of notification
     */
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

    /**
     * Allows to get the list of user available
     * @param currentUsers users we don't want in the list
     * @return the list of user available
     */
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
