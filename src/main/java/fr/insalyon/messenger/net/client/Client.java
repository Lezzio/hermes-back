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
        Message message = null;

        if (args.length != 2) {
            System.out.println("Usage: java EchoClient <EchoServer host> <EchoServer port>");
            System.exit(1);
        }

        String clientName = registerClient();

        try {
            // creation socket ==> connexion
            echoSocket = new Socket(args[0], Integer.parseInt(args[1]));
            socIn = new BufferedReader(
                    new InputStreamReader(echoSocket.getInputStream()));
            socOut = new PrintWriter(echoSocket.getOutputStream());
            stdIn = new BufferedReader(new InputStreamReader(System.in));
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
            socOut.println(message.JSONserializer());
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
        System.out.print("You are logged in as : "+clientName);
        return clientName;
    }

}
