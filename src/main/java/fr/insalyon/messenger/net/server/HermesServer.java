package fr.insalyon.messenger.net.server;

import fr.insalyon.messenger.net.model.TextMessage;
import fr.insalyon.messenger.net.mongodb.MongoDB;

import java.io.*;
import java.net.Inet4Address;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import fr.insalyon.hermes.AppState;

public class HermesServer {

    public static final int SYSTEM_CORES = Runtime.getRuntime().availableProcessors();

    private final Map<String, Socket> connections;
    private final Map<String, List<String>> chats;

    private ServerSocket serverSocket;
    private boolean running;
    private ConnectionHandler connectionHandler;
    private final ExecutorService executorService = Executors.newFixedThreadPool(SYSTEM_CORES);
    protected MongoDB mongoDB;

    /**
     * The application is usable with a desktop and a terminal interface
     * appState definines the Kotlin front-end interface
     */
    private final AppState appState;


    public AppState getAppState() {
        return appState;
    }

    public HermesServer(AppState appState) {
        connections = new HashMap<>();
        chats = new HashMap<>();
        connectionHandler = new ConnectionHandlerImpl();
        mongoDB = new MongoDB();
        this.appState = appState;
    }

    /**
     * Allows you to know if you are using the terminal interface or the Kotlin desktop app
     * @return true if the desktop interface is used
     */
    public boolean isDesktopAppActive() {
        return appState != null;
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
        if(isDesktopAppActive()){
            appState.getMessages().add(msg);
        } else {
            System.out.println("["+msg.getTime()+"] : [FROM] "+msg.getSender()+" [TO] " +msg.getDestination());
            System.out.println("[CONTENT] " +msg.getContent());
            System.out.println();
        }
    }

    public void addClient(String name, Socket socket) {
        connections.put(name, socket);
        if(isDesktopAppActive()){
            HashMap<String, Socket> newUsers = new HashMap<>(appState.getConnections().getValue());
            newUsers.put(name, socket);
            appState.getConnections().setValue(newUsers);
        }
    }

    public void removeClient(String name) {
        connections.remove(name);
        if(isDesktopAppActive()){
            HashMap<String, Socket> newUsers = new HashMap<>(appState.getConnections().getValue());
            newUsers.remove(name);
            appState.getConnections().setValue(newUsers);
        }
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
        if (args.length != 1) {
            System.out.println("Usage: java EchoServer <EchoServer port>");
            System.exit(1);
        }


        try {
            int serverPort = Integer.parseInt(args[0]);
            if(serverPort < 1024 || serverPort> 65535){
                System.err.println("Error, the port must be an integer between 1024 and 65535");
                System.exit(1);
            }

            HermesServer listenSocket = new HermesServer(null);
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