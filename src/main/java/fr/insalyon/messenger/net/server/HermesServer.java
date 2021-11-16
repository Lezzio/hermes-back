package fr.insalyon.messenger.net.server;

import java.io.IOException;
import java.net.ServerSocket;

public class HermesServer {

    private ServerSocket serverSocket;

    public HermesServer() {
    }

    public void init(int port) throws IOException {
        this.serverSocket = new ServerSocket(port);
    }

}
