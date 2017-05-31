package de.vodafone.innogarage.sfcdi_monitor;
import android.util.Log;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Innovation on 15/11/2016.
 */

public class Broadcaster extends Thread {
    //UDP-Socket
    DatagramSocket broadcastSocket;

    //Hier funktioniert alles
    //JSON-Object bauen, zum String casten, L�nge ermitteln, IP ermittlen,
    //Paket zusammenbauen und per send() Funktion des Socket absenden
    public void run() {
        try {
            broadcastSocket = new DatagramSocket();
            JSONObject msgObject = new JSONObject();
            NetworkInterface wlanInterface = null;

            //Ermittle das WLAN Interface f�r Windows und Linux     				
            try {
                Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();

                while (interfaces.hasMoreElements() && wlanInterface == null) {

                    NetworkInterface iface = interfaces.nextElement();
                    if (iface.isLoopback() || !iface.isUp()) continue;
                    String ifaceName = iface.getName();
                    if (ifaceName.contains("wlan") ||
                            ifaceName.contains("wifi") ||
                            ifaceName.contains("wi-fi") ||
                            ifaceName.contains("wlp")) {

                        //INTERFACE BESTIMMT!
                        wlanInterface = iface;
                    }
                }
            } catch (SocketException e) {
                // TODO Auto-generated catch block

                e.printStackTrace();
            }

            //Ermittle die korrekte IPv4 aus dem wlanInterface (kann mehrere haben!)
            String[] ipParts = null;
            String wlanIP = null, broadcastIP = null;
            InetAddress broadcastAddress = null;
            List<InterfaceAddress> wlanInterfaceAddresses = wlanInterface.getInterfaceAddresses();

            for (InterfaceAddress ifaceAddr : wlanInterfaceAddresses) {

                ipParts = ifaceAddr.getAddress().getHostAddress().split("\\.");
                if (ipParts.length == 4) {

                    wlanIP = ifaceAddr.getAddress().getHostAddress();
                    broadcastIP = ipParts[0] + "." + ipParts[1] + "." + ipParts[2] + ".255";
                    broadcastAddress = InetAddress.getByName(broadcastIP);
                    break;
                }
            }

            //Erzeuge das JSONObj das versendet werden soll
            try {
                msgObject.put("msgType", "invitation");
                msgObject.put("ip4", wlanIP);
                msgObject.put("port", Main.socketServerPortForSFCD);

            } catch (JSONException e) {
                e.printStackTrace();
            }

            //Verpacke das JSONObj so das es versendet werden kann
            String msgString = msgObject.toString();
            int msg_length = msgString.length();
            byte[] message = msgString.getBytes();

            //BroadcastPaket zusammensetzen
            DatagramPacket packet = new DatagramPacket(message, msg_length, broadcastAddress, Main.socketPortForBroadcast);

            //Packet versenden
            broadcastSocket.send(packet);
            if (Main.debugMode)
                Log.e("Broadcaster ","Invitation gesendet. Adresse#" + broadcastIP + " Port#" + Main.socketPortForBroadcast + " Inhalt:" + new String(message));


        } catch (UnknownHostException | SocketException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
