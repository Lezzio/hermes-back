package fr.insalyon.messenger.net.server;

import java.net.Socket;

public interface ConnectionHandler {

    public void handleConnection(Socket socket);

}