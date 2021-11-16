package fr.insalyon.messenger.net.server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import fr.insalyon.messenger.net.model.AuthenticationMessage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;

public class ConnectionHandlerImpl implements ConnectionHandler {

    private static final GsonBuilder builder = new GsonBuilder();
    private static final Gson gson = builder.create();

    @Override
    public void handleConnection(HermesServer hermesServer, Socket socket) {
        try {
            System.out.println("Handle connection");
            BufferedReader socIn = null;
            socIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintStream socOut = new PrintStream(socket.getOutputStream());
            while (true) {
                System.out.println("received message");
                String line = socIn.readLine();
                hermesServer.mongoDB.insertLogMessage(line);
                AuthenticationMessage authenticationMessage = gson.fromJson(line, AuthenticationMessage.class);
                System.out.println("Authentication : username = " + authenticationMessage.getSender() + " password = " + authenticationMessage.getPassword());
                socOut.println(line);
            }
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

}
