package de.vodafone.innogarage.sfcdi_monitor;

import android.util.Log;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.json.*;

public class Connection {

    private Socket socket;
    private DataOutputStream outgoingStream;
    private InputStream incomingStream;
    private List<JSONObject> incomingData, outgoingData;
    private boolean isAlive, close, focus, online;
    private String name;

    /**
     * Constructor
     *
     * @param socket - Instanz darf nur mit laufenden Socket erzeugt werden
     */
    public Connection(Socket socket) {

        name = socket.getInetAddress().getHostName();
        close = false;
        isAlive = true;
        focus = false;
        online = false;
        this.socket = socket;
        incomingData = new CopyOnWriteArrayList<JSONObject>();
        outgoingData = new CopyOnWriteArrayList<JSONObject>();

        try {
            outgoingStream = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            incomingStream = socket.getInputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }

        new InputStreamThread().start();
        new OutputStreamThread().start();
        new ConTimer().start();

    }

    /**
     * Thread der permanent pr�ft ob die Ausgabeliste �ber neue Nachrichten verf�gt
     * sendet alle Nachrichten die in der AList outgoingData liegen bis die Liste leer ist
     */
    private class OutputStreamThread extends Thread {

        JSONObject jMsg = null;

        public void run() {

            while (!close) {

                if (!outgoingDataListisEmpty()) {

                    for(JSONObject jobj : outgoingData) {
                        jMsg = jobj;
                        outgoingData.remove(jobj);

                        String msgString = jMsg.toString();      //Parse JSON zu STRING
                        msgString += "\n";                       //Markiere ENDE der Nachricht

                        //Absenden des Strings
                        PrintStream printStream = new PrintStream(outgoingStream);
                        printStream.print(msgString);
                        if (Main.debugMode)
                            Log.e("Connection: ", socket.getLocalAddress().getHostAddress() + " Nachricht wurde gesendet! Inhalt: " + msgString);
                    }
                }
                try {
                    Thread.sleep(25);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Dieser Thread liest permanent den InputStream aus
     * und schreibt die Informationen in eine verf�gbare Datenstruktur
     * Der Thread muss beim erzeugen des Objekts gestartet werden und darf
     * erst bei der Zerst�rung der Instanz geschlossen werden
     */
    private class InputStreamThread extends Thread {

        BufferedReader breader = new BufferedReader(new InputStreamReader(incomingStream));

        public void run() {

            String line = null;
            JSONObject jObj = null;

            while (!close) {

                try {
                    line = breader.readLine();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if (line != null) {

                    String msgType = null;
                    try {
                        jObj = new JSONObject(line);
                        msgType= jObj.getString("msgType");

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    if(msgType.equalsIgnoreCase("areualive")){

                        JSONObject msg = new JSONObject();
                        try {
                            msg.put("msgType","iamalive");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        sendMessage(msg);

                    }else if(msgType.equalsIgnoreCase("iamalive")){

                        isAlive = true;

                    }else if(removeJSONShit(msgType).equalsIgnoreCase("gstatus")){

                        String mode = null;
                        try {
                            mode = removeJSONShit(jObj.getString("Mode"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        if(mode != null){

                            if(mode.equalsIgnoreCase("online")){
                                online = true;
                            }else{
                                online = false;
                            }

                        }else{
                            online = true;
                        }

                        overwriteGstatus(jObj);
                        if (Main.debugMode)
                            Log.e("Connection: " , socket.getInetAddress() + " Eine Nachricht wurde empfangen! Inhalt: " + jObj.toString() + " => In incomingData abgelegt!");

                    }else if(removeJSONShit(msgType).equalsIgnoreCase("lteinfo")){

                        overwriteLteInfo(jObj);
                        if (Main.debugMode)
                            Log.e("Connection: " , socket.getInetAddress() + " Eine Nachricht wurde empfangen! Inhalt: " + jObj.toString() + " => In incomingData abgelegt!");

                    }else{
                        incomingData.add(jObj);
                        if (Main.debugMode)
                            Log.e("Connection: " , socket.getInetAddress() + " Eine Nachricht wurde empfangen! Inhalt: " + jObj.toString() + " => In incomingData abgelegt!");

                    }

                }
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void overwriteLteInfo(JSONObject jobj){

        for(JSONObject msg : incomingData){

            String msgType = null;
            try {

                msgType= removeJSONShit(msg.getString("msgType"));

            } catch (JSONException e) {
                e.printStackTrace();
            }

            if(msgType.equalsIgnoreCase("lteinfo")){

                incomingData.remove(msg);

            }
        }
        incomingData.add(jobj);
    }

    private void overwriteGstatus(JSONObject jobj){

        for(JSONObject msg : incomingData){

            String msgType = null;
            try {

                msgType= removeJSONShit(msg.getString("msgType"));

            } catch (JSONException e) {
                e.printStackTrace();
            }

            if(msgType.equalsIgnoreCase("gstatus")){

                incomingData.remove(msg);

            }
        }
        incomingData.add(jobj);
    }

    private String removeJSONShit(String mitShit){

        String ohneShit = "";
        ohneShit = mitShit.substring(2,mitShit.length()-2);
        return ohneShit;
    }

    public class ConTimer extends Thread{

        public void run(){

            while(!close) {

                isAlive = false;

                try {
                    Thread.sleep(2500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                if (!isAlive) {

                    System.out.println("KILL CON");
                    close = true;
                }
            }
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isOnline() {
        return online;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    public void setIncomingData(List<JSONObject> incomingData) {
        this.incomingData = incomingData;
    }

    public List<JSONObject> getOutgoingData() {
        return outgoingData;
    }

    public void setOutgoingData(List<JSONObject> outgoingData) {
        this.outgoingData = outgoingData;
    }

    public boolean isAlive() {
        return isAlive;
    }

    public void setAlive(boolean alive) {
        isAlive = alive;
    }

    public boolean isClose() {
        return close;
    }

    public void setClose(boolean close) {
        this.close = close;
    }

    public boolean isFocus() {
        return focus;
    }

    public void setFocus(boolean focus) {
        this.focus = focus;
    }

    public InputStream getIncomingStream() {
        return incomingStream;
    }


    public void setIncomingStream(InputStream incomingStream) {
        this.incomingStream = incomingStream;
    }

    public DataOutputStream getOutgoingStream() {
        return outgoingStream;
    }

    public void setOutgoingStream(DataOutputStream outgoingStream) {
        this.outgoingStream = outgoingStream;
    }


    public void sendMessage(JSONObject jObj) {
        outgoingData.add(jObj);
    }

    private boolean incomingDataListIsEmpty() {
        return incomingData.isEmpty();
    }

    private boolean outgoingDataListisEmpty() {
        return outgoingData.isEmpty();
    }

    public List<JSONObject> getIncomingData() {
        return incomingData;
    }

    public Socket getSocket() {
        return socket;
    }
}