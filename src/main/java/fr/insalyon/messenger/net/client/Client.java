package fr.insalyon.messenger.net.client;

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

    /**
     * main method
     * accepts a connection, receives a message from client then sends an echo to the client
     **/
    public static void main(String[] args) throws IOException {

        Socket echoSocket = null;
        PrintWriter socOut = null;
        BufferedReader stdIn = null;
        BufferedReader socIn = null;
        String clientName = null;
        Message message = null;
        final GsonBuilder builder = new GsonBuilder();
        final Gson gson = builder.create();

        if (args.length != 2) {
            System.out.println("Usage: java EchoClient <EchoServer host> <EchoServer port>");
            System.exit(1);
        }
        try {
            // creation socket ==> connexion
            echoSocket = new Socket(args[0], Integer.parseInt(args[1]));
            socIn = new BufferedReader(
                    new InputStreamReader(echoSocket.getInputStream()));
            socOut = new PrintWriter(echoSocket.getOutputStream());
            System.out.print("Enter your name to log in\n");
            clientName = registerClient();
            stdIn = new BufferedReader(new InputStreamReader(System.in));
            System.out.print("\nYou are logged in as : "+clientName);
        } catch (UnknownHostException e) {
            System.err.println("Don't know about host:" + args[0]);
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for "
                    + "the connection to:" + args[0]);
            System.exit(1);
        }

        String line;
        while (true) {
            line = stdIn.readLine();
            if (line.equals(".")) break;
            message = new Message(clientName, line, new Date(System.currentTimeMillis()));
            socOut.println(gson.toJson(message));
            System.out.println("echo: " + socIn.readLine());
        }
        closeClient(echoSocket, socOut, stdIn, socIn);
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
        return clientName;
    }

}
