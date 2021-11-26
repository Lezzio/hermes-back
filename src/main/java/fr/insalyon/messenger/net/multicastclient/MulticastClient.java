package fr.insalyon.messenger.net.multicastclient;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import fr.insalyon.messenger.net.model.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.google.gson.GsonBuilder;
import fr.insalyon.messenger.net.serializer.RuntimeTypeAdapterFactory;
import fr.insalyon.multicast.AppState;
import kotlin.Pair;

/**
 * This class allows users to interact with other users by connecting to a group
 * with a multicast based on socket UDP method
 */
public class MulticastClient {

    /**
     * In our application the username is used as a unique identifier
     */
    private final String username;

    /**
     * The application is usable with a desktop and a terminal interface
     * appState defines the Kotlin front-end interface
     */
    private final AppState appState;
    private MulticastSocket socket;
    private int port;
    private InetAddress group;

    /**
     * Executor used to handle sender thread and listener thread
     */
    private final ExecutorService executorService = Executors.newFixedThreadPool(2);
    private final boolean isTerminal;

    /**
     * List of user in the group
     */
    private List<String> nameInGroup;
    private boolean connected;

    /**
     * This attribute allows to know the type of the message received or send
     */
    private static final TypeToken<MulticastMessage> messageTypeToken = new TypeToken<>() {
    };

    /**
     * Allows to define all types of exchangeable messages
     */
    private static final RuntimeTypeAdapterFactory<MulticastMessage> typeFactory = RuntimeTypeAdapterFactory
            .of(MulticastMessage.class, "type")
            .registerSubtype(HelloMulticastMessage.class)
            .registerSubtype(ByeMulticastMessage.class)
            .registerSubtype(MulticastMessage.class);

    /**
     * Json Parser
     */
    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapterFactory(typeFactory)
            .create();

    /**
     * Constructor to create a mutlicast client
     * @param username the client name
     * @param isTerminal true if it is a terminal application
     * @param appState defines if it is a Kotlin application
     * @throws IOException Error of connection
     */
    public MulticastClient(String username, boolean isTerminal, AppState appState) throws IOException {
        this.username = username;
        this.isTerminal = isTerminal;
        this.nameInGroup = new ArrayList<>();
        this.appState = appState;
        this.connected = false;
    }

    /**
     * Allows to connect into a group chat defines with the ip
     * @param ip the ip of the group
     * @param port the port we want to use
     * @throws IOException Error of connection
     */
    public void connect(String ip, int port) throws IOException {
        this.port = port;
        this.socket = new MulticastSocket(port);
        this.group = InetAddress.getByName(ip);
        this.socket.joinGroup(this.group);
        executorService.submit(this::listenerThread);
        if (isTerminal) {
            executorService.submit(this::senderThread);
            System.out.println("Join the group " + this.group.getHostAddress() + " on the port " + this.port);
        }

        //Inform all users that the client is connected
        sayHello("");
        setConnected(true);
    }

    /**
     * Allows disconnecting from a group
     * @throws IOException disconnection error
     */
    public void disconnect() throws IOException {
        //Inform all users that the client is disconnected
        sayGoodBye();
        this.socket.leaveGroup(this.group);
        this.socket.close();
        this.nameInGroup = new ArrayList<>();
        System.out.println("Leave the group");
        setConnected(false);
    }

    /**
     * Allows to launch the terminal application
     * @param args
     * @throws IOException Error of format and connection
     */
    public static void main(String[] args) throws IOException {
        BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
        System.out.println("Username :");
        String username = stdIn.readLine();
        while (username.length() <= 2) {
            System.err.println("Wrong format, the username should have a length >2");
            System.out.println("Username :");
            username = stdIn.readLine();
        }
        System.out.println("Group IP (IP addresses are in the range 224.0.0.1 to 239.255.255.255):");
        String host = stdIn.readLine();
        System.out.println("Port :");
        String port = stdIn.readLine();

        try {
            MulticastClient multiCastClient = new MulticastClient(username, true, null);
            multiCastClient.connect(host, Integer.parseInt(port));
        } catch (IOException ex) {
            ex.printStackTrace();
            System.err.println("Error : could not connect to server " + host + " with port " + Integer.valueOf(port));
        } catch (NumberFormatException ex) {
            System.err.println("Error : you must set a correct port number");
        }
    }

    /**
     * Allows interpreting udp messages received by the client that have been sent in the mutlicast group
     * Allows to recover the different data received and to assign the appropriate
     * application processing to make them interpretable by the user of the application
     * The data will be stored in the various attributes provided for this purpose of the
     * current client
     */
    public void listenerThread() {
        try {
            while (true) {
                byte[] buffer = new byte[this.socket.getReceiveBufferSize()];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

                //Reads the udp packet
                this.socket.receive(packet);
                String msg = new String(buffer, 0, packet.getLength());

                MulticastMessage receivedMessage = gson.fromJson(msg, messageTypeToken.getType());

                //Adds a user in the group when the client receives a Hello
                if (receivedMessage instanceof HelloMulticastMessage) {
                    HelloMulticastMessage helloMultiCastMessage = (HelloMulticastMessage) receivedMessage;
                    if (Objects.equals(helloMultiCastMessage.getDestination(), "")) {
                        String userJoinedStr = helloMultiCastMessage.getSender() + " has joined the group";
                        System.out.println(userJoinedStr);
                        if (!Objects.equals(helloMultiCastMessage.getSender(), username)) {
                            this.nameInGroup.add(helloMultiCastMessage.getSender());
                            if (isDesktopActive()) {
                                this.appState.getConnectedGroupUsers().add(helloMultiCastMessage.getSender());
                                MulticastMessage convertedHelloMessage = new MulticastMessage(userJoinedStr, "*", helloMultiCastMessage.getTime());
                                this.appState.getMessages().add(convertedHelloMessage);
                            }
                        }
                        sayHello(helloMultiCastMessage.getSender());
                    } else if (Objects.equals(helloMultiCastMessage.getDestination(), username)) {
                        this.nameInGroup.add(helloMultiCastMessage.getSender());
                        if (isDesktopActive()) {
                            this.appState.getConnectedGroupUsers().add(helloMultiCastMessage.getSender());
                        }
                    }

                //Deletes a user in the group when the client receives a Bye
                } else if (receivedMessage instanceof ByeMulticastMessage) {
                    ByeMulticastMessage byeMultiCastMessage = (ByeMulticastMessage) receivedMessage;
                    String userLeftStr = byeMultiCastMessage.getSender() + " has left the group";
                    System.out.println(userLeftStr);
                    this.nameInGroup.remove(byeMultiCastMessage.getSender());
                    if (isDesktopActive()) {
                        this.appState.getConnectedGroupUsers().remove(byeMultiCastMessage.getSender());
                        MulticastMessage convertedByeMessage = new MulticastMessage(userLeftStr, "*", byeMultiCastMessage.getTime());
                        this.appState.getMessages().add(convertedByeMessage);
                    }
                } else {
                    if (Objects.equals(receivedMessage.getSender(), username)) {
                        System.out.println("Message from me - " + receivedMessage.getTime());
                    } else {
                        System.out.println("Message from " + receivedMessage.getSender() + " - " + receivedMessage.getTime());
                    }
                    if (isDesktopActive()) {
                        appState.getMessages().add(receivedMessage);
                    }
                    System.out.println("Content : " + receivedMessage.getContent());
                    System.out.println();
                }

            }
        } catch (IOException e) {
            if (!e.getMessage().equals("socket closed") && !e.getMessage().equals("Socket is closed")) {
                e.printStackTrace();
            }
            if (isDesktopActive()) {
                appState.getNotification().setValue(new Pair<>("An error occured : " + e.getMessage(), true));
            }
        }
    }

    /**
     * Allows to now the type of the app used
     * @return true if the app used is the kotlin app
     */
    private boolean isDesktopActive() {
        return !isTerminal && appState != null;
    }

    /**
     * Allows you to send different types of events to the multicast group
     * depending on the user's interaction with the application and its purpose
     * Allows displaying data by the user in the terminal
     * This thread is only used with the terminal interface by reading the System.in messages
     */
    public void senderThread() {
        BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
        String line = "";
        try {
            while (true) {
                line = stdIn.readLine();

                //Allows to leave the group
                if (line.equals("-disconnect") || line.equals("-exit")) break;

                //Allows to get the users connected in the group
                if (line.equals("?users")) {
                    displayUsersInChat();
                } else {
                    sendMessage(line);
                }
            }

            if (line.equals("-disconnect") || line.equals("-exit")) {
                disconnect();
                executorService.shutdownNow();
            }
            if (line.equals("-disconnect")) {
                System.out.println("Do you want to join another channel (-join <ip> <port>) or exit (-exit)");
                line = stdIn.readLine();
                String[] args = line.split(" ");
                boolean valid = false;
                while (!valid) {
                    //If the user reconnect to a group we need to create a new multicast client to handle a new connection
                    if (!args[0].equals("-exit")) {
                        if (args.length != 3) {
                            System.err.println("Error : USAGE -join <ip> <port>");
                        } else {
                            valid = true;
                            try {
                                MulticastClient multiCastClient = new MulticastClient(username, true, null);
                                String ip = args[1];
                                int port = Integer.parseInt(args[2]);
                                multiCastClient.connect(ip, port);
                            } catch (IOException ex) {
                                System.err.println("Error : could not connect to server " + args[1] + " on port " + Integer.valueOf(args[2]));
                            } catch (NumberFormatException ex) {
                                System.err.println("Error : you must set a correct port number");
                            }
                        }
                    }

                    if (args[0].equals("-exit")) {
                        valid = true;
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * Allows displaying in the terminal users connected in the current group
     */
    private void displayUsersInChat() {
        System.out.println("****Chat group composition*****");
        for (String user : nameInGroup) {
            System.out.println(user);
        }
        System.out.println("*******************************");
    }

    /**
     * Event triggered on message received
     * Used in the listening thread
     *
     * @param message the message to display
     */
    public void messageReceived(String message) {
        System.out.println(message);
    }

    /**
     * Allows sending in the group a message with an udp packet
     * @param message the message to send
     * @throws IOException Packet error
     */
    public void sendMessage(String message) throws IOException {
        if (socket != null) {
            MulticastMessage fullMessage = new MulticastMessage(message, this.username, new Date(System.currentTimeMillis()));
            String stringMessage = gson.toJson(fullMessage, messageTypeToken.getType());
            DatagramPacket packetToSend = new DatagramPacket(stringMessage.getBytes(), stringMessage.length(),
                    this.group, this.port);
            this.socket.send(packetToSend);
        }
    }

    /**
     * Allows sending hello in the group to warn of our precariousness
     * with an udp packet
     * @param destination the destination is a specific user or the group chat
     * @throws IOException Packet error
     */
    public void sayHello(String destination) throws IOException {
        if (socket != null) {
            HelloMulticastMessage fullMessage = new HelloMulticastMessage("Hello", this.username, destination, new Date(System.currentTimeMillis()));
            String stringMessage = gson.toJson(fullMessage, messageTypeToken.getType());
            DatagramPacket packetToSend = new DatagramPacket(stringMessage.getBytes(), stringMessage.length(),
                    this.group, this.port);
            this.socket.send(packetToSend);
        }
    }

    /**
     * Allows sending bye in the group to warn of our disconnection
     * with an udp packet
     * @throws IOException Packet error
     */
    private void sayGoodBye() throws IOException {
        if (socket != null) {
            ByeMulticastMessage fullMessage = new ByeMulticastMessage("Hello", this.username, new Date(System.currentTimeMillis()));
            String stringMessage = gson.toJson(fullMessage, messageTypeToken.getType());
            DatagramPacket packetToSend = new DatagramPacket(stringMessage.getBytes(), stringMessage.length(),
                    this.group, this.port);
            this.socket.send(packetToSend);
        }
    }

    /**
     * Allows to now if the user is connected
     * @return true if connected
     */
    public boolean isConnected() {
        return connected;
    }

    public void setConnected(boolean connected) {
        this.connected = connected;
    }

}