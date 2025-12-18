/**
 * Licensed to ESUP-Portail under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for
 * additional information regarding copyright ownership.
 *
 * ESUP-Portail licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.esupportail.esupnfctagkeyboard;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;


public class UniqueInstance {
 
    private int port;
    private String message;
    private Runnable runOnReceive;
	private final static Logger log = LoggerFactory.getLogger(UniqueInstance.class);

    
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
                            	log.warn("Attente de connexion de socket échouée.");
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
        	log.warn("Écriture sur flux de sortie de la socket échouée.");
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
        	log.warn("Lecture du flux d'entrée de la socket échoué.");
        } finally {
            if(sc != null)
                sc.close();
        }
 
    }
 
}