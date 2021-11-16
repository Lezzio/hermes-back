package fr.insalyon.messenger.net.server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import fr.insalyon.messenger.net.model.*;
import fr.insalyon.messenger.net.serializer.RuntimeTypeAdapterFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.util.Date;

public class ConnectionHandlerImpl implements ConnectionHandler {

    private static final TypeToken<Message> messageTypeToken = new TypeToken<>() {};
    private static final RuntimeTypeAdapterFactory<Message> typeFactory = RuntimeTypeAdapterFactory
            .of(Message.class, "type")
            .registerSubtype(GroupMessage.class)
            .registerSubtype(PrivateMessage.class)
            .registerSubtype(AuthenticationMessage.class)
            .registerSubtype(TextMessage.class)
            .registerSubtype(ConnectionMessage.class);
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
