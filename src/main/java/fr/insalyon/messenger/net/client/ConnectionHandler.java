package fr.insalyon.messenger.net.client;

public interface ConnectionHandler {
    public void handleConnection(Client client, String serverHost, int serverPort);

    public void handleDisconnection();


}
