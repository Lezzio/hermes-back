package fr.insalyon.messenger.net.server;

import fr.insalyon.messenger.net.model.TextMessage;
import fr.insalyon.messenger.net.mongodb.MongoDB;

import java.io.*;
import java.net.Inet4Address;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HermesServer {

    public static final int SYSTEM_CORES = Runtime.getRuntime().availableProcessors();

    private final Map<String, Socket> connections;
    private final Map<String, List<String>> chats;

    private ServerSocket serverSocket;
    private boolean running;
    private ConnectionHandler connectionHandler;
    private final ExecutorService executorService = Executors.newFixedThreadPool(SYSTEM_CORES);
    protected MongoDB mongoDB;

    public HermesServer() {
        connections = new HashMap<>();
        chats = new HashMap<>();
        connectionHandler = new ConnectionHandlerImpl();
        mongoDB = new MongoDB();
    }

    public void init(int port) throws IOException {
        this.serverSocket = new ServerSocket(port);
        running = true;
        System.out.println("Server initialized on [IP] "+ Inet4Address.getLocalHost().getHostAddress()+ " [PORT] "+port);
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

    public void logMessage(TextMessage msg){
        System.out.println("["+msg.getTime()+"] : [FROM] "+msg.getSender()+" [TO] " +msg.getDestination());
        System.out.println("[CONTENT] " +msg.getContent());
        System.out.println();
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

    public void addChat(String id, List<String> users){chats.put(id, users);}

    public void removeChat(String id) {chats.remove(id);}

    public void addChatUser(String id,String user){
        chats.get(id).add(user);
    }

    public List<String> getChat(String name){
        return chats.get(name);
    }



    public Map<String, Socket> getConnections() {
        return connections;
    }

    public static void main(String ... args) {
        //if (args.length != 1) {
        //    System.out.println("Usage: java EchoServer <EchoServer port>");
        //    System.exit(1);
        //}
        BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));


        try {
            System.out.println("Select the server port :");
            int serverPort = Integer.parseInt(stdIn.readLine());
            while(serverPort < 1024 || serverPort> 65535){
                System.err.println("Error, the port must be an integer between 1024 and 65535");
                System.out.println("Select the server port :");
                serverPort = Integer.parseInt(stdIn.readLine());
            }

            HermesServer listenSocket = new HermesServer(); //port
            listenSocket.init(serverPort);
        } catch (IOException e) {
            System.err.println("Error :"+e.getMessage());
        } catch (NumberFormatException e) {
            System.err.println("Error, the port number must be an integer");
        }
    }

    public void removeChatUser(String chatName, String sender) {
        chats.get(chatName).removeIf(value -> Objects.equals(value, sender));
    }

    public void updateChat(String newChatName, String oldChatName) {
        chats.put(newChatName, chats.get(oldChatName));
        chats.remove(oldChatName);
    }
}