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

/**
 * This class allows to run the hermes server in order
 * to allow client to connect to it
 * to exchange messages in chats
 */
public class HermesServer {

    /**
     * List of users connecting in the server
     */
    private final Map<String, Socket> connections;

    /**
     * List of active chat with the corresponding user
     */
    private final Map<String, List<String>> chats;

    private ServerSocket serverSocket;
    private boolean running;
    private final ConnectionHandler connectionHandler;

    /**
     * Gives a thread to each client connection
     */
    private final ExecutorService executorService = Executors.newCachedThreadPool();

    /**
     * The associated mongoDB database
     */
    protected MongoDB mongoDB;

    /**
     * The application is usable with a desktop and a terminal interface
     * appState definines the Kotlin front-end interface
     */
    private final AppState appState;
    public AppState getAppState() {
        return appState;
    }

    /**
     * Constructor allows to create the server with the terminal or desktop interface
     * WARNING : for the moment the kotlin application is not available
     * @param appState defines if it is the desktop interface
     */
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

    /**
     * Trys to launch the server on a specific port
     * @param port the specific port
     * @throws IOException
     */
    public void init(int port) throws IOException {
        this.serverSocket = new ServerSocket(port);
        running = true;
        System.out.println("Server initialized on [IP] " + Inet4Address.getLocalHost().getHostAddress() + " [PORT]" + port);
        while (running) {
            Socket socket = serverSocket.accept();
            executorService.submit(() -> connectionHandler.handleConnection(this, socket));
        }
    }

    /**
     * Allows to display log message in the terminal
     * @param msg the msg
     */
    public void logMessage(TextMessage msg){
        if(isDesktopAppActive()){
            appState.getMessages().add(msg);
        } else {
            System.out.println("["+msg.getTime()+"] : [FROM] "+msg.getSender()+" [TO] " +msg.getDestination());
            System.out.println("[CONTENT] " +msg.getContent());
            System.out.println();
        }
    }

    /**
     * Allows to add a client in the list of connected client
     * @param name the client name
     * @param socket the client socket
     */
    public void addClient(String name, Socket socket) {
        connections.put(name, socket);
        if(isDesktopAppActive()){
            HashMap<String, Socket> newUsers = new HashMap<>(appState.getConnections().getValue());
            newUsers.put(name, socket);
            appState.getConnections().setValue(newUsers);
        }
    }

    /**
     * Allows to remove a client in the list of connected client
     * @param name the client name
     */
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

    /**
     * Allows adding clients in a chat
     * @param id the chat id = the chat name
     * @param users the list of client
     */
    public void addChat(String id, List<String> users){chats.put(id, users);}

    /**
     * Allows adding client in a chat
     * @param id the chat id = the chat name
     * @param user the client name
     */
    public void addChatUser(String id,String user){
        chats.get(id).add(user);
    }

    /**
     * Allows to get the list users in a chat
     * @param name the chatname
     * @return the list of users
     */
    public List<String> getChat(String name){
        return chats.get(name);
    }


    /**
     * Allows to get connected users
     * @return the list of connected users
     */
    public Map<String, Socket> getConnections() {
        return connections;
    }

    /**
     * Allows to launch an hermes server in the terminal
     * @param args the server port
     */
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

    /**
     * Allows to remove an user from a chat
     * @param chatName the chatname
     * @param sender the user to remove
     */
    public void removeChatUser(String chatName, String sender) {
        chats.get(chatName).removeIf(value -> Objects.equals(value, sender));
    }

    /**
     * Allows to update a chat
     * @param newChatName the new chat name
     * @param oldChatName the old chat name
     */
    public void updateChat(String newChatName, String oldChatName) {
        chats.put(newChatName, chats.get(oldChatName));
        chats.remove(oldChatName);
    }
}