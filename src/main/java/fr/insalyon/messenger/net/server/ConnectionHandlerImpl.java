package fr.insalyon.messenger.net.server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import fr.insalyon.messenger.net.model.*;
import fr.insalyon.messenger.net.model.Chat;
import fr.insalyon.messenger.net.mongodb.User;
import fr.insalyon.messenger.net.serializer.RuntimeTypeAdapterFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.util.*;

public class ConnectionHandlerImpl implements ConnectionHandler {

    private static final TypeToken<Message> messageTypeToken = new TypeToken<>() {
    };
    private static final RuntimeTypeAdapterFactory<Message> typeFactory = RuntimeTypeAdapterFactory
            .of(Message.class, "type")
            .registerSubtype(GroupMessage.class)
            .registerSubtype(PrivateMessage.class)
            .registerSubtype(AuthenticationMessage.class)
            .registerSubtype(TextMessage.class)
            .registerSubtype(ConnectionMessage.class)
            .registerSubtype(AlertConnected.class)
            .registerSubtype(DisconnectionMessage.class)
            .registerSubtype(GetChats.class)
            .registerSubtype(CreateChat.class)
            .registerSubtype(AccessChat.class)
            .registerSubtype(GetNotifications.class)
            .registerSubtype(GetUsersAddable.class)
            .registerSubtype(AddUserChat.class)
            .registerSubtype(AddNotification.class)
            .registerSubtype(BanUserChat.class)
            .registerSubtype(BanNotification.class)
            .registerSubtype(UpdateChat.class)
            .registerSubtype(AlertDisconnected.class)
            .registerSubtype(LeaveChat.class)
            .registerSubtype(GetUsers.class);
    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapterFactory(typeFactory)
            .create();

    @Override
    public void handleConnection(HermesServer hermesServer, Socket socket) {
        String client = "";
        try {
            System.out.println("Handle connection");
            BufferedReader socIn = null;
            socIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintStream socOut = new PrintStream(socket.getOutputStream());
            String message;
            while ((message = socIn.readLine()) != null) {
                //System.out.println("received message");
                Message receivedMessage = gson.fromJson(message, messageTypeToken.getType());

                //System.out.println("Message = " + message);
                //System.out.println("Deserialized = " + receivedMessage + " name = " + receivedMessage.getClass().getSimpleName());

                switch (receivedMessage.getClass().getSimpleName()) {
                    case "ConnectionMessage":
                        ConnectionMessage msg = (ConnectionMessage) receivedMessage;
                        User user = hermesServer.mongoDB.searchUser(msg.getName());
                        String userName = "";
                        if (user == null) {
                            hermesServer.mongoDB.insertUser(msg);
                            userName = msg.getName();
                        } else {
                            userName = user.getUsername();
                        }
                        client = userName;
                        hermesServer.addClient(userName, socket);
                        TextMessage logMessage = new TextMessage("Connection", userName, "server", new Date(System.currentTimeMillis()));
                        hermesServer.mongoDB.insertLog(logMessage);
                        hermesServer.logMessage(logMessage);
                        AlertConnected alertConnected = new AlertConnected(userName, "server", userName, new Date(System.currentTimeMillis()));
                        if (user != null) {
                            alertConnected.setPreviousConnection(user.getPreviousConnection());
                        }
                        socOut.println(gson.toJson(alertConnected, messageTypeToken.getType()));
                        break;
                    case "DeconnectionMessage":
                        DisconnectionMessage decoMsg = (DisconnectionMessage) receivedMessage;
                        disconnectionFromUser(decoMsg.getSender(), hermesServer);
                        break;
                    case "GetUsersAddable":
                        GetUsersAddable getUsersAddable = (GetUsersAddable) receivedMessage;
                        List<String> users = hermesServer.mongoDB.getUsersAddable(hermesServer.getChat(getUsersAddable.getDestination()));
                        getUsersAddable.setUsers(users);
                        socOut.println(gson.toJson(getUsersAddable, messageTypeToken.getType()));
                        break;
                    case "GetChats":
                        GetChats getChats = (GetChats) receivedMessage;
                        List<LogChat> logChats = hermesServer.mongoDB.getChats(getChats.getSender());
                        //System.out.println("Log chat size = " + logChats.size());
                        for (LogChat logChat : logChats) {
                            hermesServer.addChat(logChat.getName(), logChat.getUsers());
                            for (String userInChat : logChat.getUsers()) {
                                if (hermesServer.getConnections().containsKey(userInChat)) {
                                    alertConnected = new AlertConnected(getChats.getSender(), logChat.getName(), userInChat, new Date(System.currentTimeMillis()));
                                    new PrintStream(hermesServer.getConnections().get(userInChat).getOutputStream()).println(gson.toJson(alertConnected, messageTypeToken.getType()));
                                }
                            }
                        }
                        logMessage = new TextMessage("asks chats", getChats.getSender(), "server", new Date(System.currentTimeMillis()));
                        hermesServer.mongoDB.insertLog(logMessage);
                        hermesServer.logMessage(logMessage);
                        getChats = new GetChats("server", getChats.getSender(), new Date(System.currentTimeMillis()));
                        getChats.setLogChat(logChats);
                        socOut.println(gson.toJson(getChats, messageTypeToken.getType()));
                        break;
                    case "AccessChat":
                        AccessChat accessChat = (AccessChat) receivedMessage;
                        Chat chatGet = hermesServer.mongoDB.getChat(accessChat.getChatName());
                        if (chatGet != null) {
                            accessChat = new AccessChat("server", accessChat.getSender(), new Date(System.currentTimeMillis()), accessChat.getChatName());
                            accessChat.setMessages(chatGet.getMessages());
                            accessChat.setUsers(chatGet.getUsers());
                            accessChat.setAdmin(chatGet.getAdmin());
                            socOut.println(gson.toJson(accessChat, messageTypeToken.getType()));
                        }
                        logMessage = new TextMessage("access chat " + accessChat.getChatName(), accessChat.getDestination(), "server", new Date(System.currentTimeMillis()));
                        hermesServer.mongoDB.insertLog(logMessage);
                        hermesServer.logMessage(logMessage);
                        break;
                    case "AddUserChat":
                        AddUserChat addUserChat = (AddUserChat) receivedMessage;
                        hermesServer.mongoDB.addChatUsers(addUserChat.getChatName(), addUserChat.getUsers());
                        for (String newuser : addUserChat.getUsers()) {
                            hermesServer.addChatUser(addUserChat.getChatName(), newuser);
                            AddNotification addNotification = new AddNotification("You have been added to the chat " + addUserChat.getChatName(),
                                    addUserChat.getChatName(),
                                    newuser,
                                    new Date(System.currentTimeMillis()));
                            addNotification.setChat(new LogChat(
                                    addUserChat.getChatName(), hermesServer.getChat(addUserChat.getChatName()),
                                    new TextMessage("You have been added by " + addUserChat.getSender(), addUserChat.getDestination(), newuser, new Date(System.currentTimeMillis()))
                            ));
                            hermesServer.mongoDB.insertNotification(addNotification);
                            if (hermesServer.getConnections().containsKey(newuser)) {
                                new PrintStream(hermesServer.getConnections().get(newuser).getOutputStream()).println(gson.toJson(addNotification, messageTypeToken.getType()));
                            }
                        }

                        boolean first = true;
                        for (String newuser : addUserChat.getUsers()) {
                            TextMessage fullMessage = new TextMessage(newuser + " added",
                                    addUserChat.getChatName(),
                                    addUserChat.getChatName(),
                                    new Date(System.currentTimeMillis()));
                            hermesServer.mongoDB.insertMessages(fullMessage);
                            for (String userInChat : hermesServer.getChat(addUserChat.getChatName())) {
                                if (hermesServer.getConnections().containsKey(userInChat)) {
                                    new PrintStream(hermesServer.getConnections().get(userInChat).getOutputStream()).println(gson.toJson(fullMessage, messageTypeToken.getType()));
                                }
                            }
                        }
                        logMessage = new TextMessage("adds users to chat " + addUserChat.getChatName(), addUserChat.getSender(), addUserChat.getChatName(), new Date(System.currentTimeMillis()));
                        hermesServer.mongoDB.insertLog(logMessage);
                        hermesServer.logMessage(logMessage);

                        break;
                    case "BanUserChat":
                        BanUserChat banUserChat = (BanUserChat) receivedMessage;
                        hermesServer.mongoDB.removeChatUser(banUserChat.getChatName(), banUserChat.getUser());
                        hermesServer.removeChatUser(banUserChat.getChatName(), banUserChat.getUser());
                        BanNotification banNotification = new BanNotification("You have been banned from the chat " + banUserChat.getChatName(),
                                banUserChat.getChatName(),
                                banUserChat.getUser(),
                                new Date(System.currentTimeMillis()));
                        hermesServer.mongoDB.insertNotification(banNotification);
                        if (hermesServer.getConnections().containsKey(banUserChat.getUser())) {
                            new PrintStream(hermesServer.getConnections().get(banUserChat.getUser()).getOutputStream()).println(gson.toJson(banNotification, messageTypeToken.getType()));
                        }

                        TextMessage fullMessage = new TextMessage(banUserChat.getUser() + " banned",
                                banUserChat.getChatName(),
                                banUserChat.getChatName(),
                                new Date(System.currentTimeMillis()));
                        hermesServer.mongoDB.insertMessages(fullMessage);
                        for (String userInChat : hermesServer.getChat(banUserChat.getChatName())) {
                            if (hermesServer.getConnections().containsKey(userInChat)) {
                                new PrintStream(hermesServer.getConnections().get(userInChat).getOutputStream()).println(gson.toJson(fullMessage, messageTypeToken.getType()));
                            }
                        }
                        logMessage = new TextMessage("deletes users to chat " + banUserChat.getChatName(), banUserChat.getSender(), banUserChat.getChatName(), new Date(System.currentTimeMillis()));
                        hermesServer.mongoDB.insertLog(logMessage);
                        hermesServer.logMessage(logMessage);
                        break;
                    case "CreateChat":
                        CreateChat createChat = (CreateChat) receivedMessage;
                        String chatName = hermesServer.mongoDB.searchChat(createChat.getName());
                        createChat = new CreateChat(createChat.getDestination(), createChat.getSender(), new Date(System.currentTimeMillis()), createChat.getName());
                        if (chatName == null) {
                            hermesServer.mongoDB.insertChat(createChat);
                            chatName = createChat.getName();
                            ArrayList<String> chatUsers = new ArrayList<>();
                            chatUsers.add(createChat.getDestination());
                            hermesServer.addChat(chatName, chatUsers);
                            createChat.setState(true);
                        } else {
                            createChat.setState(false);
                        }
                        socOut.println(gson.toJson(createChat, messageTypeToken.getType()));
                        logMessage = new TextMessage("creates chat " + createChat.getName(), createChat.getDestination(), "server", new Date(System.currentTimeMillis()));
                        hermesServer.mongoDB.insertLog(logMessage);
                        hermesServer.logMessage(logMessage);

                        break;
                    case "LeaveChat":
                        LeaveChat leaveChat = (LeaveChat) receivedMessage;
                        hermesServer.removeChatUser(leaveChat.getName(), leaveChat.getSender());
                        hermesServer.mongoDB.removeChatUser(leaveChat.getName(), leaveChat.getSender());
                        fullMessage = new TextMessage(leaveChat.getSender() + " has left", leaveChat.getName(), leaveChat.getName(), new Date(System.currentTimeMillis()));
                        hermesServer.mongoDB.insertMessages(fullMessage);

                        socOut.println(gson.toJson(leaveChat, messageTypeToken.getType()));

                        for (String userInChat : hermesServer.getChat(leaveChat.getName())) {
                            if (hermesServer.getConnections().containsKey(userInChat)) {
                                new PrintStream(hermesServer.getConnections().get(userInChat).getOutputStream()).println(gson.toJson(fullMessage, messageTypeToken.getType()));
                            }
                        }
                        logMessage = new TextMessage("leaves chat " + leaveChat.getName(), leaveChat.getSender(), leaveChat.getName(), new Date(System.currentTimeMillis()));
                        hermesServer.mongoDB.insertLog(logMessage);
                        hermesServer.logMessage(logMessage);
                        break;
                    case "UpdateChat":
                        UpdateChat updateChat = (UpdateChat) receivedMessage;
                        boolean nameChanged = true;
                        if (Objects.equals(updateChat.getChatName(), updateChat.getDestination())) {
                            nameChanged = false;
                        }

                        boolean result = hermesServer.mongoDB.updateChat(updateChat, nameChanged);
                        if (result) {
                            hermesServer.mongoDB.insertMessages(new TextMessage("Chat parameters update", updateChat.getChatName(), updateChat.getChatName(), new Date(System.currentTimeMillis())));
                            for (String userInChat : hermesServer.getChat(updateChat.getDestination())) {
                                if (hermesServer.getConnections().containsKey(userInChat)) {
                                    new PrintStream(hermesServer.getConnections().get(userInChat).getOutputStream()).println(gson.toJson(updateChat, messageTypeToken.getType()));
                                    new PrintStream(hermesServer.getConnections().get(userInChat).getOutputStream()).println(gson.toJson(
                                            new TextMessage("Chat parameters update", updateChat.getChatName(), updateChat.getChatName(), new Date(System.currentTimeMillis())),
                                            messageTypeToken.getType()));
                                }
                            }
                            if (nameChanged) {
                                hermesServer.updateChat(updateChat.getChatName(), updateChat.getDestination());
                            }
                            logMessage = new TextMessage("updates chat " + updateChat.getDestination(), updateChat.getSender(), updateChat.getDestination(), new Date(System.currentTimeMillis()));
                            hermesServer.mongoDB.insertLog(logMessage);
                            hermesServer.logMessage(logMessage);
                        } else {
                            AlertMessage alert = new AlertMessage("Modification error", updateChat.getDestination(), updateChat.getSender(), new Date(System.currentTimeMillis()));
                            logMessage = new TextMessage("Failed to update chat " + updateChat.getDestination(), updateChat.getSender(), updateChat.getDestination(), new Date(System.currentTimeMillis()));
                            hermesServer.mongoDB.insertLog(logMessage);
                            hermesServer.logMessage(logMessage);
                            socOut.println(gson.toJson(alert, messageTypeToken.getType()));
                        }
                        break;
                    case "GetUsers":
                        GetUsers getUsers = (GetUsers) receivedMessage;
                        Map<String, Boolean> connectedUsers = new HashMap<>();
                        for (String userInChat : hermesServer.getChat(getUsers.getDestination())) {
                            boolean connected = hermesServer.getConnections().containsKey(userInChat);
                            connectedUsers.put(userInChat, connected);
                        }
                        getUsers = new GetUsers(getUsers.getDestination(), getUsers.getSender(), new Date(System.currentTimeMillis()));
                        getUsers.setConnected(connectedUsers);
                        connectedUsers.forEach((usera, connected) -> {
                            System.out.println("Username = " + usera + " connected = " + connected);
                        });
                        socOut.println(gson.toJson(getUsers, messageTypeToken.getType()));
                        logMessage = new TextMessage("asks for connected users in chat " + getUsers.getSender(), getUsers.getDestination(), getUsers.getSender(), new Date(System.currentTimeMillis()));
                        hermesServer.mongoDB.insertLog(logMessage);
                        hermesServer.logMessage(logMessage);
                        break;
                    case "GetNotifications":
                        GetNotifications getNotifications = (GetNotifications) receivedMessage;
                        List<Notification> notifications = hermesServer.mongoDB.getNotifications(getNotifications.getSender());
                        getNotifications.setNotifications(notifications);
                        socOut.println(gson.toJson(getNotifications, messageTypeToken.getType()));
                        break;
                    case "TextMessage":
                        TextMessage textMessage = (TextMessage) receivedMessage;
                        if (Objects.equals(textMessage.getDestination(), "server")) {
                            socOut.println(message);
                        } else {
                            for (String relatedUser : hermesServer.getChat(textMessage.getDestination())) {
                                if (hermesServer.getConnections().containsKey(relatedUser)) {
                                    new PrintStream(hermesServer.getConnections().get(relatedUser).getOutputStream()).println(gson.toJson(textMessage, messageTypeToken.getType()));
                                }
                            }
                        }
                        hermesServer.mongoDB.insertLog(textMessage);
                        hermesServer.logMessage(textMessage);
                        hermesServer.mongoDB.insertMessages(textMessage);
                        break;
                    default:
                        socOut.println(message);
                        break;
                }
            }
            //No more input stream awaited : disconnect user
            disconnectionFromUser(client, hermesServer);
        } catch (Exception exception) {
            System.out.println("Exception caught : " + exception.getMessage());
            if (!Objects.equals(client, "")) {
                disconnectionFromUser(client, hermesServer);
            }
        }
    }

    private void disconnectionFromUser(String username, HermesServer hermesServer) {
        System.out.println("Disconnecting " + username);
        try {
            hermesServer.removeClient(username);
            hermesServer.mongoDB.insertLog(new TextMessage("Deconnection", username, "server", new Date(System.currentTimeMillis())));

            List<LogChat> logChats = hermesServer.mongoDB.getChats(username);
            for (LogChat logChat : logChats) {
                for (String userInChat : logChat.getUsers()) {
                    if (hermesServer.getConnections().containsKey(userInChat)) {
                        AlertDisconnected alertDisconnected = new AlertDisconnected(username, logChat.getName(), userInChat, new Date(System.currentTimeMillis()));
                        new PrintStream(hermesServer.getConnections().get(userInChat).getOutputStream()).println(gson.toJson(alertDisconnected, messageTypeToken.getType()));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
