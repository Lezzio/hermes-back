package fr.insalyon.messenger.net.client;

import com.google.gson.reflect.TypeToken;
import fr.insalyon.messenger.net.model.AuthenticationMessage;
import fr.insalyon.messenger.net.model.Message;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.Scanner;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import fr.insalyon.messenger.net.model.TextMessage;

public class Client {
    private Socket echoSocket = null;
    private PrintStream socOut = null;
    private BufferedReader stdIn = null;
    private BufferedReader socIn = null;
    private String clientName;

    private Message message = null;
    final static GsonBuilder builder = new GsonBuilder();
    final static Gson gson = builder.create();
    final TypeToken<Message> messageTypeToken = new TypeToken<>() {};

    private boolean running = true;

    public Client(){

    }

    public void init(String serverHost, int serverPort) throws IOException {
        running = false;
        try {
            // creation socket ==> connexion
            echoSocket = new Socket(serverHost, serverPort);
            socIn = new BufferedReader(
                    new InputStreamReader(echoSocket.getInputStream()));
            socOut = new PrintStream(echoSocket.getOutputStream());
            System.out.print("Enter your name to log in\n");
            stdIn = new BufferedReader(new InputStreamReader(System.in));
            clientName = registerClient(stdIn, socOut);
            running = true;
            System.out.print("\nYou are logged in as : "+clientName);
        } catch (UnknownHostException e) {
            System.err.println("Don't know about host:" + serverHost);
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for "
                    + "the connection to:" + serverHost);
            System.exit(1);
        }


        String line;
        while (true) {
            line = stdIn.readLine();
            if (line.equals(".")) break;
            message = new TextMessage("Hello", clientName,"user2", new Date(System.currentTimeMillis()));
            socOut.println(gson.toJson(message, messageTypeToken.getType()));
            try {
                System.out.println("echo: " + socIn.readLine());
            } catch (SocketException e) {
                System.err.println(e.getMessage());
                System.exit(1);
            }

        }
        closeClient(echoSocket, socOut, stdIn, socIn);
    }

    /**
     * main method
     * accepts a connection, receives a message from client then sends an echo to the client
     **/
    public static void main(String[] args) throws IOException {



        if (args.length != 2) {
            System.out.println("Usage: java EchoClient <EchoServer host> <EchoServer port>");
            System.exit(1);
        }

        Client client = new Client();
        client.init(args[0], Integer.parseInt(args[1]));


    }

    private static void closeClient(Socket echoSocket, PrintStream socOut, BufferedReader stdIn, BufferedReader socIn) throws IOException {
        socOut.close();
        socIn.close();
        stdIn.close();
        echoSocket.close();
    }

    private String registerClient(BufferedReader stdIn, PrintStream socOut) throws IOException {
        System.out.print("Please enter your name\n");
        String clientName= stdIn.readLine();
        message = new AuthenticationMessage(clientName, "to someone", new Date(System.currentTimeMillis()));
        socOut.println(gson.toJson(message, messageTypeToken.getType()));
        System.out.print("User connected as \n" + clientName);
        return clientName;
    }

}
