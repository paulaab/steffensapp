package de.vodafone.innogarage.sfcdi_monitor;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by Innovation on 17/11/2016.
 * Diese Klasse hat die Aufgabe auf eingehende TCP Verbindungen im Sinne eines Servers zu reagieren
 * diese zu authentifizieren und im Anschluss einem SFCD zuzuordnen
 * Au�erdem ist diese Klasse daf�r verantwortlich bestehende Verbindungen zu �berwachen
 * bei Verbindungensabbruch wieder herzustellen oder bei Timeout dauerfhaft zu schlie�en
 */

public class ConnectionManager {


    private ServerSocket serverSocketForSFCD = null;
    private ServerSocket serverSocketForOtherClients = null;
    private List<Connection> connections;


    /**
     * Constructor
     */
    public ConnectionManager() {

        connections = new CopyOnWriteArrayList<Connection>();

        try {
            serverSocketForSFCD = new ServerSocket(Main.socketServerPortForSFCD);
            if (Main.debugMode)
                Log.e("ConnectionManager "," Constructor SFCD : serverSocket-Bindung erfolgreich!");
        } catch (IOException e) {
            e.printStackTrace();

            if (Main.debugMode)
                Log.e("ConnectionManager "," Constructor SFCD : serverSocket konnte nicht zugewiesen werden");
        }

        new ConnectionListenerForSFCD().start();
        new ConnectionCheckerThread().start();
    }

    public void sendInvitation() {

        new Broadcaster().start();
    }


    public void sendToAll(JSONObject msg){

        for(Connection con : connections){

            con.sendMessage(msg);
        }
    }



    /**
     * Thread Class
     * Akzeptiert eingehende TCP Verbindungen und sichert diese Verbindung
     * in einer ConnectionListe
     *
     * @author Steffen.Ryll
     */
    private class ConnectionListenerForSFCD extends Thread {

        public void run() {

            while (true) {

                Socket socket = null;

                try {
                    if (Main.debugMode)
                        Log.e("ConnectionManager "," ConListener : warte auf Verbindungen!");
                    socket = serverSocketForSFCD.accept();
                    if (Main.debugMode)
                        Log.e("ConnectionManager ","ConListener : Verbindung akzeptiert!");

                } catch (IOException e) {
                    e.printStackTrace();
                }

                connections.add(new Connection(socket));
                if (Main.debugMode)
                    Log.e("ConnectionManager "," ConListener : New SFCD added. IP= " + socket.getInetAddress().toString());

            }
        }
    }

    /**
     * Thread Class
     *
     * @author Steffen.Ryll
     */
    private class ConnectionCheckerThread extends Thread {

        public void run() {
            while (true) {

                if(!connections.isEmpty()){

                    for(Connection con : connections){

                        if(con.isClose()){

                            connections.remove(con);
                        }
                    }

                    JSONObject msg = new JSONObject();
                    try {
                        msg.put("msgType","areualive");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    sendToAll(msg);
                }

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Getter function
     *
     * @return the List of connections
     */
    public List<Connection> getConnections() {

        return connections;
    }
}