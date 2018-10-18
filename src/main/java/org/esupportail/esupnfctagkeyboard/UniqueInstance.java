package org.esupportail.esupnfctagkeyboard;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;
import java.util.logging.Logger;
 

public class UniqueInstance {
 
    private int port;
 
    private String message;
 
    private Runnable runOnReceive;

    public UniqueInstance(int port, String message, Runnable runOnReceive) {
        if (port == 0 || (port & 0xffff0000) != 0)
            throw new IllegalArgumentException("Le port doit être compris entre 1 et 65535 : " + port + ".");
        if (runOnReceive != null && message == null)
            throw new IllegalArgumentException("runOnReceive != null ==> message == null.");
 
        this.port = port;
        this.message = message;
        this.runOnReceive = runOnReceive;
    }

    public UniqueInstance(int port) {
        this(port, null, null);
    }

    public boolean launch() {

    	boolean unique;
 
        try {
        	final ServerSocket server = new ServerSocket(port);
            unique = true;
            if(runOnReceive != null) {
            Thread portListenerThread = new Thread("UniqueInstance-PortListenerThread") {
                    {
                        setDaemon(true);
                    }
 
                    @Override public void run() {
                        while(true) {
                            try {
                            	final Socket socket = server.accept();
                                new Thread("UniqueInstance-SocketReceiver") {
 
                                    {
                                        setDaemon(true);
                                    }
 
                                    @Override public void run() {
                                        receive(socket);
                                    }
                                }.start();
                            } catch(IOException e) {
                                Logger.getLogger("UniqueInstance").warning("Attente de connexion de socket échouée.");
                            }
                        }
                    }
                };
 
                portListenerThread.start();
            }
        } catch(IOException e) {
            unique = false;
            if(runOnReceive != null) {
                send();
            }
        }
        return unique;
    }
 
    private void send() {
        PrintWriter pw = null;
        try {
            Socket socket = new Socket("localhost", port);
            pw = new PrintWriter(socket.getOutputStream());
            pw.write(message);
        } catch(IOException e) {
            Logger.getLogger("UniqueInstance").warning("Écriture sur flux de sortie de la socket échouée.");
        } finally {
            if(pw != null)
                pw.close();
        }
    }
 

    private synchronized void receive(Socket socket) {
        Scanner sc = null;
        try {
            socket.setSoTimeout(5000);
            sc = new Scanner(socket.getInputStream());
            String s = sc.nextLine();
            if(message.equals(s)) {
                runOnReceive.run();
            }
        } catch(IOException e) {
            Logger.getLogger("UniqueInstance").warning("Lecture du flux d'entrée de la socket échoué.");
        } finally {
            if(sc != null)
                sc.close();
        }
 
    }
 
}