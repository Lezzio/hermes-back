package fr.insalyon.messenger.net.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;

/**
 * Client permettant l'interaction avec le serveur
 */
public class HermesClient {


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
    private ClientThread client;

    /**
     * HermesClient constructor
     *
     */
    public HermesClient() {

    }

    public static void main(String[] args) throws IOException {
        System.out.println("launching hermesClient");
        if (args.length != 2) {
            System.out.println("Usage: java EchoClient <EchoServer host> <EchoServer port>");
            System.exit(1);
        }
        System.out.println(1);
        HermesClient hClient = new HermesClient();
        System.out.println(2);
        try{
            hClient.connect(args[0], Integer.parseInt(args[1]));
        }catch(Exception e ){
            System.out.println(e);
        }
        hClient.sendMessage("lol");

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
        client = new ClientThread(this, inStream);
        System.out.println(3);
        client.run();
    }

    /**
     * Event triggered on message received
     * Used in the listening thread
     * @param message
     */
    public void messageReceived(String message) {
        System.out.println(10);
        System.out.println(message);
    }

    /**
     * Allows the client to send messages to the server
     * @param message
     */
    public void sendMessage(String message) {
        if(socket != null){
            outStream.println(message);
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
