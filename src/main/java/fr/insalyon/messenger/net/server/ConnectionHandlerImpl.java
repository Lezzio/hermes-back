package fr.insalyon.messenger.net.server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import fr.insalyon.messenger.net.model.*;
import fr.insalyon.messenger.net.model.Chat;
import fr.insalyon.messenger.net.serializer.RuntimeTypeAdapterFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.util.*;

public class ConnectionHandlerImpl implements ConnectionHandler {

    private static final TypeToken<Message> messageTypeToken = new TypeToken<>() {};
    private static final RuntimeTypeAdapterFactory<Message> typeFactory = RuntimeTypeAdapterFactory
            .of(Message.class, "type")
            .registerSubtype(GroupMessage.class)
            .registerSubtype(PrivateMessage.class)
            .registerSubtype(AuthenticationMessage.class)
            .registerSubtype(TextMessage.class)
            .registerSubtype(ConnectionMessage.class)
            .registerSubtype(DisconnectionMessage.class);
    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapterFactory(typeFactory)
            .create();

    @Override
    public void handleConnection(HermesServer hermesServer, Socket socket) {
        try {
            System.out.println("Handle connection");
            BufferedReader socIn = null;
            socIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintStream socOut = new PrintStream(socket.getOutputStream());
            String message;
            while ((message = socIn.readLine()) != null) {
                System.out.println("received message");
                Message receivedMessage = gson.fromJson(message, messageTypeToken.getType());

                System.out.println("Message = " + message);
                System.out.println("Deserialized = " + receivedMessage + " name = " + receivedMessage.getClass().getSimpleName());

                //TODO :: save in log
                switch (receivedMessage.getClass().getSimpleName()){
                    case "ConnectionMessage" :
                        ConnectionMessage msg = (ConnectionMessage) receivedMessage;
                        String user = hermesServer.mongoDB.searchUser(msg.getName());
                        if(user == null){
                            hermesServer.mongoDB.insertUser(msg);
                            user = msg.getName();
                        }
                        hermesServer.addClient(user, socket);

                        TextMessage fullMessage = new TextMessage("Connection Success","server",user, new Date(System.currentTimeMillis()));
                        socOut.println(gson.toJson(fullMessage, messageTypeToken.getType()));
                        break;
                    case "DeconnectionMessage":
                        DisconnectionMessage decoMsg = (DisconnectionMessage) receivedMessage;
                        user = decoMsg.getSender();
                        hermesServer.removeClient(user);
                        DisconnectionMessage deco = new DisconnectionMessage("sender", new Date(System.currentTimeMillis()));
                        socOut.println(gson.toJson(deco, messageTypeToken.getType()));
                        break;
                    case "GetChats":
                        GetChats getChats = (GetChats) receivedMessage;
                        List<LogChat> logchats =  hermesServer.mongoDB.getChats(getChats.getSender());
                        for(LogChat logChat : logchats){
                            hermesServer.addChat(logChat.getName(), logChat.getUsers());
                        }
                        getChats = new GetChats("server", getChats.getSender(), new Date(System.currentTimeMillis()));
                        getChats.setLogChat(logchats);
                        socOut.println(gson.toJson(getChats, messageTypeToken.getType()));
                        //TODO: alert others
                        break;
                    case "AccessChat":
                        AccessChat accessChat = (AccessChat) receivedMessage;
                        Chat chatGet = hermesServer.mongoDB.getChat(accessChat.getChatName());
                        if(chatGet != null){
                            accessChat = new AccessChat("server", accessChat.getSender(), new Date(System.currentTimeMillis()), accessChat.getChatName());
                            accessChat.setMessages(chatGet.getMessages());
                            accessChat.setUsers(chatGet.getUsers());
                            accessChat.setAdmin(chatGet.getAdmin());
                            socOut.println(gson.toJson(accessChat, messageTypeToken.getType()));
                        }
                        break;
                    case "AddUserChat":
                        AddUserChat addUserChat = (AddUserChat) receivedMessage;
                        hermesServer.mongoDB.addChatUsers(addUserChat.getChatName(), addUserChat.getUsers());
                        for(String newuser : addUserChat.getUsers()){
                            hermesServer.addChatUser(addUserChat.getChatName(), newuser);
                            if(hermesServer.getConnections().containsKey(newuser)){
                                fullMessage = new TextMessage("You have been added to the chat "+addUserChat.getChatName(),addUserChat.getChatName(),newuser , new Date(System.currentTimeMillis()));
                                new PrintStream(hermesServer.getConnections().get(newuser).getOutputStream()).println(gson.toJson(fullMessage, messageTypeToken.getType()));
                            }
                        }
                        //TODO:: envoyer une notif à tout le monde
                        break;
                    case "CreateChat":
                        CreateChat createChat = (CreateChat) receivedMessage;
                        String chat = hermesServer.mongoDB.searchChat(createChat.getName());
                        if(chat == null){
                            hermesServer.mongoDB.insertChat(createChat);
                            chat = createChat.getName();
                            hermesServer.addChatUser(chat, createChat.getSender());
                            fullMessage = new TextMessage("Creation chat success","server", createChat.getSender(), new Date(System.currentTimeMillis()));
                        }  else {
                            fullMessage = new TextMessage("Creation chat failed","server", createChat.getSender(), new Date(System.currentTimeMillis()));
                        }
                        socOut.println(gson.toJson(fullMessage, messageTypeToken.getType()));
                        break;
                    case "LeaveChat":
                        LeaveChat leaveChat = (LeaveChat) receivedMessage;
                        hermesServer.removeChatUser(leaveChat.getName(), leaveChat.getSender());
                        hermesServer.mongoDB.removeChatUser(leaveChat.getName(), leaveChat.getSender());
                        //TODO:: alert all user
                        break;
                    case "DeleteChat":
                        DeleteChat deleteChat = (DeleteChat) receivedMessage;
                        break;
                    case "UpdateChat":
                        UpdateChat updateChat = (UpdateChat) receivedMessage;
                        boolean result = hermesServer.mongoDB.updateChat(updateChat);
                        //TODO : deal with the result
                        break;
                    case "GetUsers":
                        GetUsers getUsers = (GetUsers) receivedMessage;
                        Map<String, Boolean> connectedUsers = new HashMap<String, Boolean>();
                        for(String userInChat : hermesServer.getChat(getUsers.getDestination())){
                            boolean connected;
                            if(hermesServer.getConnections().containsKey(userInChat)){
                                connected = true;
                            } else {
                                connected = false;
                            }
                            connectedUsers.put(userInChat, connected);
                        }
                        getUsers = new GetUsers(getUsers.getDestination(), getUsers.getSender(), new Date(System.currentTimeMillis()));
                        getUsers.setConnected(connectedUsers);
                        socOut.println(gson.toJson(getUsers, messageTypeToken.getType()));
                        break;
                    case "TextMessage" :
                        TextMessage textMessage = (TextMessage) receivedMessage;
                        if(Objects.equals(textMessage.getDestination(), "server")){
                            socOut.println(message);
                        } else {
                            for(String relatedUser: hermesServer.getChat(textMessage.getDestination())){
                                if(hermesServer.getConnections().containsKey(relatedUser)){
                                    new PrintStream(hermesServer.getConnections().get(relatedUser).getOutputStream()).println(gson.toJson(textMessage, messageTypeToken.getType()));
                                }
                            }
                        }
                        break;
                    default:
                        socOut.println(message);
                        break;
                }
            }
        } catch (IOException exception) {
            exception.printStackTrace();
        } catch (Exception e) {
            System.out.println(e);
        }
    }




}
