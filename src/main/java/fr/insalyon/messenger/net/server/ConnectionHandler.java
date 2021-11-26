package fr.insalyon.messenger.net.server;

import java.net.Socket;

/**
 * Allows the server to handle a connection
 */
public interface ConnectionHandler {

    /**
     * This method allows to handle the socket of a client
     * @param hermesServer the hermes server
     * @param socket the socket received
     */
    public void handleConnection(HermesServer hermesServer, Socket socket);

}