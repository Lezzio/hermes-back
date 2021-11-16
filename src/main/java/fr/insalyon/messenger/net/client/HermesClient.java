package fr.insalyon.messenger.net.client;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import fr.insalyon.messenger.net.model.ConnectionMessage;
import fr.insalyon.messenger.net.model.Message;
import fr.insalyon.messenger.net.model.TextMessage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Client permettant l'interaction avec le serveur
 */
public class HermesClient {

    private static final TypeToken<Message> messageTypeToken = new TypeToken<>() {};
    private static final RuntimeTypeAdapterFactory<Message> typeFactory = RuntimeTypeAdapterFactory
            .of(Message.class, "type")
            .registerSubtype(GroupMessage.class)
            .registerSubtype(PrivateMessage.class)
            .registerSubtype(AuthenticationMessage.class)
            .registerSubtype(TextMessage.class)
            .registerSubtype(ConnectionMessage.class);
    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapterFactory(typeFactory)
            .create();

    private String username;
    /**
     * Socket used to communicate
     * between client and server
     */
    private Socket socket;
    /**
     * Stream to get infos server => client
     */
    private BufferedReader inStream;
    /**
     * Stream to send infos client => server
     */
    private PrintStream outStream;
    /**
     * Thread allowing continuous listening to server
     */
    private final ExecutorService executorService = Executors.newFixedThreadPool(2);


    /**
     * HermesClient constructor
     *
     */
    public HermesClient(String username) {
        this.username = username;
    }

    /**
     *
     * @param args 0 => server address 1=> server port 2=> client username
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        System.out.println("launching hermesClient");
        if (args.length != 3) {
            System.out.println("Usage: java EchoClient <EchoServer host> <EchoServer port> <EchoClient username>");
            System.exit(1);
        }
        HermesClient hClient = new HermesClient(args[2]);
        try{
            hClient.connect(args[0], Integer.parseInt(args[1]));
        }catch(Exception e ){
            System.out.println(e);
        }
    }
    /**
     * Permet de connecter le client au serveur Hermes.
     * @param serverHost IP du serveur
     * @param serverPort Port du serveur
     * @throws IOException
     */
    public void connect(String serverHost, int serverPort) throws IOException {
        socket = new Socket(serverHost, serverPort);
        inStream = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        outStream = new PrintStream(socket.getOutputStream());
        executorService.submit(() -> listenerThread(this,inStream));
        executorService.submit(() -> senderThread(this,outStream));
        sendConnection();
    }

    public void listenerThread (HermesClient hClient, BufferedReader inStream){
        try {
            String message;
            while ((message = inStream.readLine()) != null) {
                System.out.println("message received");
                hClient.messageReceived(message);
            }
        } catch (IOException e) {
            System.out.println(e);
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public void senderThread(HermesClient hClient, PrintStream outStream){
        BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
        String line;
        try {
            while (true) {
                line = stdIn.readLine();
                if (line.equals(".")) break;
                sendMessage(line);
            }
        }catch(Exception e){
            System.out.println(e);
        }
    }

    /**
     * Event triggered on message received
     * Used in the listening thread
     * @param message
     */
    public void messageReceived(String message) {
        System.out.println("showing message" + message);
    }

    /**
     * Allows the client to send messages to the server
     * Wraps the content of the message with other
     * informations
     * @param message
     */
    public void sendMessage(String message) {
        if(socket != null){
            TextMessage fullMessage = new TextMessage(message,this.username,"use3", new Date(System.currentTimeMillis()));
            outStream.println(gson.toJson(fullMessage, messageTypeToken.getType()));
        }
    }

    public void sendConnection(){
        if(socket != null){
            ConnectionMessage msg = new ConnectionMessage(this.username,"", new Date(System.currentTimeMillis()));
            outStream.println(gson.toJson(msg, messageTypeToken.getType()));
        }
    }

    public void sendDisconnection(){
        if(socket != null){
            DisconnectionMessage msg = new DisconnectionMessage(this.username, new Date(System.currentTimeMillis()));
            outStream.println(gson.toJson(msg, messageTypeToken.getType())));
        }
    }

    /**
     * Permet de fermet les flux et de terminer la
     * connexion avec le serveur
     * @throws IOException
     */
    public void closeClient() throws IOException {
        try{
            socket.close();
            inStream.close();
            outStream.close();
        }catch(IOException e){
            System.out.println(e);
        }
    }
}
