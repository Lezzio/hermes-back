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


public class MulticastClient {

    private final String username;
    private final AppState appState;
    private MulticastSocket socket;
    private int port;
    private InetAddress group;
    private final ExecutorService executorService = Executors.newFixedThreadPool(2);
    private final boolean isTerminal;
    private List<String> nameInGroup;
    private boolean connected;
    private static final TypeToken<MulticastMessage> messageTypeToken = new TypeToken<>() {
    };

    private static final RuntimeTypeAdapterFactory<MulticastMessage> typeFactory = RuntimeTypeAdapterFactory
            .of(MulticastMessage.class, "type")
            .registerSubtype(HelloMulticastMessage.class)
            .registerSubtype(ByeMulticastMessage.class)
            .registerSubtype(MulticastMessage.class);

    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapterFactory(typeFactory)
            .create();

    public MulticastClient(String username, boolean isTerminal, AppState appState) throws IOException {
        this.username = username;
        this.isTerminal = isTerminal;
        this.nameInGroup = new ArrayList<>();
        this.appState = appState;
        this.connected = false;
    }

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
        sayHello("");
        setConnected(true);
    }

    public void disconnect() throws IOException {
        sayGoodBye();
        this.socket.leaveGroup(this.group);
        this.socket.close();
        this.nameInGroup = new ArrayList<>();
        System.out.println("Leave the group");
        setConnected(false);
    }

    public static void main(String[] args) throws IOException {
        /*System.out.println("launching multi cast client");
        if (args.length != 3) {
            System.out.println("Usage: java MultiCastClient <group ip> <port> <username>");
            System.exit(1);
        }*/

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
        System.out.println("Group Port :");
        String port = stdIn.readLine();

        try {
            MulticastClient multiCastClient = new MulticastClient(username, true, null);
            multiCastClient.connect(host, Integer.parseInt(port));
        } catch (IOException ex) {
            ex.printStackTrace();
            System.err.println("Error : could not connect to server " + host + " on port " + Integer.valueOf(port));
        } catch (NumberFormatException ex) {
            System.err.println("Error : you must set a correct port number");
        }
    }

    public void listenerThread() {
        try {
            while (true) {
                byte[] buffer = new byte[this.socket.getReceiveBufferSize()];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                this.socket.receive(packet);
                String msg = new String(buffer, 0, packet.getLength());
                //messageReceived(msg);
                MulticastMessage receivedMessage = gson.fromJson(msg, messageTypeToken.getType());

                //System.out.println(receivedMessage.getClass().getSimpleName());
                if (receivedMessage instanceof HelloMulticastMessage) {
                    HelloMulticastMessage helloMultiCastMessage = (HelloMulticastMessage) receivedMessage;
                    if (Objects.equals(helloMultiCastMessage.getDestination(), "")) {
                        System.out.println(helloMultiCastMessage.getSender() + " has joined the group");
                        if (!Objects.equals(helloMultiCastMessage.getSender(), username)) {
                            this.nameInGroup.add(helloMultiCastMessage.getSender());
                            this.appState.getConnectedGroupUsers().add(helloMultiCastMessage.getSender());
                        }
                        sayHello(helloMultiCastMessage.getSender());
                    } else if (Objects.equals(helloMultiCastMessage.getDestination(), username)) {
                        this.nameInGroup.add(helloMultiCastMessage.getSender());
                        this.appState.getConnectedGroupUsers().add(helloMultiCastMessage.getSender());
                    }
                } else if (receivedMessage instanceof ByeMulticastMessage) {
                    ByeMulticastMessage byeMultiCastMessage = (ByeMulticastMessage) receivedMessage;
                    System.out.println(byeMultiCastMessage.getSender() + " has left the group");
                    this.nameInGroup.remove(byeMultiCastMessage.getSender());
                    this.appState.getConnectedGroupUsers().remove(byeMultiCastMessage.getSender());
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
        }
    }

    private boolean isDesktopActive() {
        return !isTerminal && appState != null;
    }

    public void senderThread() {
        BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
        String line = "";
        try {
            while (true) {
                line = stdIn.readLine();
                if (line.equals("-disconnect") || line.equals("-exit")) break;
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
     * @param message
     */
    public void messageReceived(String message) {
        System.out.println(message);
    }

    public void sendMessage(String message) throws IOException {
        if (socket != null) {
            MulticastMessage fullMessage = new MulticastMessage(message, this.username, new Date(System.currentTimeMillis()));
            String stringMessage = gson.toJson(fullMessage, messageTypeToken.getType());
            DatagramPacket packetToSend = new DatagramPacket(stringMessage.getBytes(), stringMessage.length(),
                    this.group, this.port);
            this.socket.send(packetToSend);
        }
    }

    public void sayHello(String destination) throws IOException {
        if (socket != null) {
            HelloMulticastMessage fullMessage = new HelloMulticastMessage("Hello", this.username, destination, new Date(System.currentTimeMillis()));
            String stringMessage = gson.toJson(fullMessage, messageTypeToken.getType());
            DatagramPacket packetToSend = new DatagramPacket(stringMessage.getBytes(), stringMessage.length(),
                    this.group, this.port);
            this.socket.send(packetToSend);
        }
    }

    private void sayGoodBye() throws IOException {
        if (socket != null) {
            ByeMulticastMessage fullMessage = new ByeMulticastMessage("Hello", this.username, new Date(System.currentTimeMillis()));
            String stringMessage = gson.toJson(fullMessage, messageTypeToken.getType());
            DatagramPacket packetToSend = new DatagramPacket(stringMessage.getBytes(), stringMessage.length(),
                    this.group, this.port);
            this.socket.send(packetToSend);
        }
    }

    public boolean isConnected() {
        return connected;
    }

    public void setConnected(boolean connected) {
        this.connected = connected;
    }

}