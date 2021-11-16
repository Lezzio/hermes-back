package fr.insalyon.messenger.net.server;

import fr.insalyon.messenger.net.mongodb.MongoDB;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HermesServer {

    public static final int SYSTEM_CORES = Runtime.getRuntime().availableProcessors();

    private final Map<String, Socket> connections;
    private ServerSocket serverSocket;
    private boolean running;
    private ConnectionHandler connectionHandler;
    private final ExecutorService executorService = Executors.newFixedThreadPool(SYSTEM_CORES);
    protected MongoDB mongoDB;

    public HermesServer() {
        connections = new HashMap<>();
        connectionHandler = new ConnectionHandlerImpl();
        mongoDB = new MongoDB();
//        final RuntimeTypeAdapterFactory<Message> typeFactory = RuntimeTypeAdapterFactory
//                .of(Message.class, "type")
//                .registerSubtype(GroupMessage.class)
//                .registerSubtype(PrivateMessage.class);
    }

    public void init(int port) throws IOException {
        this.serverSocket = new ServerSocket(port);
        running = true;
        while (running) {
            Socket socket = serverSocket.accept();
            executorService.submit(() -> connectionHandler.handleConnection(this, socket));
        }
    }

    public void broadcastMessage(String content){
        connections.forEach( (id, socket) -> {
            try {
                PrintStream socOut = new PrintStream(socket.getOutputStream());
                socOut.println(content);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
    public void addClient(String name, Socket socket) {
        connections.put(name, socket);
    }

    public void removeClient(String name) {
        connections.remove(name);
    }

    public void stop() throws IOException {
        running = false;
        for (Socket socket : connections.values()) {
            socket.close();
        }
        serverSocket.close();
    }

    public Map<String, Socket> getConnections() {
        return connections;
    }

    public static void main(String ... args) {
        if (args.length != 1) {
            System.out.println("Usage: java EchoServer <EchoServer port>");
            System.exit(1);
        }
        try {
        HermesServer listenSocket = new HermesServer(); //port
            listenSocket.init(Integer.parseInt(args[0]));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}