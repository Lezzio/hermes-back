package fr.insalyon.messenger.net.client;

public interface ConnectionHandler {
    public void handleConnection(HermesClient client, String serverHost, int serverPort);

    public void handleDisconnection();


}
