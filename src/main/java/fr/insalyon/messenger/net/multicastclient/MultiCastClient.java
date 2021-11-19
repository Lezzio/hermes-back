package fr.insalyon.messenger.net.multicastclient;

import fr.insalyon.messenger.net.client.HermesClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MultiCastClient {

    private MulticastSocket socket;
    private int port;
    private InetAddress group;
    private final ExecutorService executorService = Executors.newFixedThreadPool(2);


    public MultiCastClient(String ip, int port) throws IOException {
        this.port = port;
        this.socket = new MulticastSocket(port);
        this.group = InetAddress.getByName(ip);

    }

    public void connect() throws IOException {
        this.socket.joinGroup(this.group);
        executorService.submit(() -> listenerThread());
        executorService.submit(() -> senderThread());
    }

    public static void main(String[] args) throws IOException {
        System.out.println("launching multi cast client");
        if (args.length != 2) {
            System.out.println("Usage: java MultiCastClient <group ip> <port>");
            System.exit(1);
        }
        MultiCastClient multiCastClient = new MultiCastClient(args[0],Integer.parseInt(args[1]));
        try {
            multiCastClient.connect();
        } catch (Exception e) {
            System.out.println(e);
        }
    }


    public void listenerThread(){
        try {
            for(;;) {
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
                if (line.equals(".")) break;
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
        DatagramPacket hi = new DatagramPacket(message.getBytes(), message.length(),
                this.group, this.port);
        this.socket.send(hi);
    }
}
