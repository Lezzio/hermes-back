package fr.insalyon.messenger.net.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;

public class ConnectionHandlerImpl implements ConnectionHandler {

    @Override
    public void handleConnection(HermesServer hermesServer, Socket socket) {
        try {
            System.out.println("Handle connection");
            BufferedReader socIn = null;
            socIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintStream socOut = new PrintStream(socket.getOutputStream());
            while (true) {
                System.out.println("received info");
                String line = socIn.readLine();
                hermesServer.saveMessage(line);
                socOut.println(line);
            }
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

}
