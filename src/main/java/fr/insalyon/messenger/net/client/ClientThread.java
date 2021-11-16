package fr.insalyon.messenger.net.client;

import java.io.BufferedReader;
import java.io.IOException;

public class ClientThread extends Thread{

    /**
     * Instance of the HermesClient object
     */
    private HermesClient client;
    /**
     * Tells us if the thread is still alive
     */
    private boolean isRunning;
    /**
     * Reads information coming on inStream
     */
    private BufferedReader inStream;

    public ClientThread(HermesClient client, BufferedReader inStream){
        this.client = client;
        this.inStream = inStream;
    }

    public boolean isRunning(){
        return isRunning;
    }

    public void killThread(){
        isRunning = false;
    }
    /**
     * Allows the client to listen to the server
     */
    public void run(){
        System.out.println(4);
        isRunning = true;
        try{
            System.out.println(5);

            while(true){
                System.out.println(6);

                String message = inStream.readLine();
                System.out.println(10);

                if(message==null){
                    System.out.println(7);
                    break;
                }else{
                    System.out.println(8);
                    client.messageReceived(message);
                }
            }
        } catch (IOException e){
            System.out.println(e);
        }
        catch (Exception e ){
            System.out.println(e);
        }
        killThread();
    }


}
