package fr.insalyon.messenger.net.client;

import fr.insalyon.messenger.net.model.AuthenticationMessage;
import fr.insalyon.messenger.net.model.Message;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.Scanner;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class Client {
    protected Socket echoSocket = null;
    protected PrintWriter socOut = null;
    protected BufferedReader stdIn = null;
    protected BufferedReader socIn = null;
    private Message message = null;

    private boolean running = true;

    public Client(){

    }

    public void init(String serverHost, int serverPort) throws IOException {
        String clientName = registerClient();
        running = false;
        try {
            // creation socket ==> connexion
            echoSocket = new Socket(serverHost, serverPort);
            socIn = new BufferedReader(
                    new InputStreamReader(echoSocket.getInputStream()));
            socOut = new PrintWriter(echoSocket.getOutputStream());
            stdIn = new BufferedReader(new InputStreamReader(System.in));
            running = true;
            System.out.print("You are logged in as : "+clientName);
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
            message = new AuthenticationMessage(clientName, "to someone", new Date(System.currentTimeMillis()), line, "some password");
            socOut.println(gson.toJson(message));
            System.out.println("echo: " + socIn.readLine());
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

    private static void closeClient(Socket echoSocket, PrintWriter socOut, BufferedReader stdIn, BufferedReader socIn) throws IOException {
        socOut.close();
        socIn.close();
        stdIn.close();
        echoSocket.close();
    }

    private static String registerClient() {
        Scanner sc= new Scanner(System.in);
        System.out.print("Please enter your name");
        String clientName= sc.nextLine();
        System.out.print("You are logged in as : "+clientName);
        return clientName;
    }

}
