package fr.insalyon.messenger.net.multicastclient;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import fr.insalyon.messenger.net.client.HermesClient;
import fr.insalyon.messenger.net.model.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.google.gson.GsonBuilder;
import fr.insalyon.messenger.net.serializer.RuntimeTypeAdapterFactory;


public class MultiCastClient {

    private final String username;
    private MulticastSocket socket;
    private int port;
    private InetAddress group;
    private final ExecutorService executorService = Executors.newFixedThreadPool(2);

    private static final TypeToken<MultiCastMessage> messageTypeToken = new TypeToken<>() {
    };

    private static final RuntimeTypeAdapterFactory<MultiCastMessage> typeFactory = RuntimeTypeAdapterFactory
            .of(MultiCastMessage.class, "type")
            .registerSubtype(MultiCastMessage.class);

    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapterFactory(typeFactory)
            .create();

    public MultiCastClient(String ip, int port, String username) throws IOException {
        this.port = port;
        this.socket = new MulticastSocket(port);
        this.group = InetAddress.getByName(ip);
        this.username = username;

    }

    public void connect() throws IOException {
        this.socket.joinGroup(this.group);
        executorService.submit(() -> listenerThread());
        executorService.submit(() -> senderThread());
    }

    public void disconnect() {
        this.socket.disconnect();
        executorService.shutdown();
    }

    public static void main(String[] args) throws IOException {
        System.out.println("launching multi cast client");
        if (args.length != 3) {
            System.out.println("Usage: java MultiCastClient <group ip> <port> <username>");
            System.exit(1);
        }
        MultiCastClient multiCastClient = new MultiCastClient(args[0], Integer.parseInt(args[1]), args[2]);
        try {
            multiCastClient.connect();
        } catch (Exception e) {
            System.out.println(e);
        }
    }


    public void listenerThread() {
        try {
            for (; ; ) {
                byte[] buffer = new byte[256];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                this.socket.receive(packet);
                String msg = new String(buffer, 0, packet.getLength());
                messageReceived(msg);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void senderThread() {
        BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
        String line;
        try {
            while (true) {
                line = stdIn.readLine();
                if (line.equals(".")) disconnect();
                sendMessage(line);
            }
        } catch (Exception e) {
            System.out.println(e);
        }
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
            MultiCastMessage fullMessage = new MultiCastMessage(message, this.username, new Date(System.currentTimeMillis()));
            String stringMessage = gson.toJson(fullMessage, messageTypeToken.getType());
            DatagramPacket packetToSend = new DatagramPacket(stringMessage.getBytes(), stringMessage.length(),
                    this.group, this.port);
            this.socket.send(packetToSend);
        }
    }
}


