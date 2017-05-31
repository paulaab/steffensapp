import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

/************************************************************
* Dieser Thread wird zum Start des Programms geöffnet und wird
* erst beim schließen der Anwendung wieder geschlossen. Er 
* reagiert auf Endgeräte die eine Einladung über UDP senden,
* initiiert eine TCP Verbindung und erzeugt daraus eine 
* Connection. Bekannte Verbindungen werden nicht
* erneut verbunden. Damit dieser Thread seine Arbeit machen
* kann, müssen alle entsprechenden Geräte im gleichen Netz-
* werk und gegenseitig ansprechbar sein. Voraussetzung ist
* dass das Adressschema des Netzwerks auf der Subnetzmaske:
* 255.255.255.0 beruht. Alle weiteren IP-Settings sind ok.
* 
* @author Steffen Ryll
* @date   23.01.2017
*/
public class InvitationListener extends Thread{

	//IV*********************************************************
	private List<Connection> cons = null;
	
	/************************************************************
	 * Constructor
	 * @param connections - In diese Liste werden alle initiierten
	 * TCP Verbindungen aufgenommen
	 */
	public InvitationListener(List<Connection> connections){
		
		this.cons = connections;		
	}
	
	/************************************************************
	 * Diese Funktion wird beim starten des Threads ausgeführt.
	 * Ermittelt automatisch das vorherschende Adressschema.
	 * Hört auf dem Broadcastkanal und reagiert auf Einladungen.
	 * 
	 */	
	public void run(){
		
		//Finde die BroadcastAdresse des aktuellen Netzwerks auf Basis einer /24 Netzmaske		
		
        //>>Ermittle das das WLAN Interface für Windows und Linux     				
		NetworkInterface wlanInterface = getWifiInterface();
		List<InterfaceAddress> wlanInterfaceAddresses = null;
  
		//>>Ermittle die korrekte IPv4 aus dem Wifi Interface
		if(wlanInterface != null){
			wlanInterfaceAddresses = wlanInterface.getInterfaceAddresses();		    
		}else{
			
			System.out.println("InvitationListener : WlanInterface ist NULL - warum auch immer!");
		}
		InetAddress broadcastAddress = null;
		String[] ipParts = null;

		String broadcastIP = null;
		String broadcastIp = null;
		
		for(InterfaceAddress ifaceAddr : wlanInterfaceAddresses){
			
			ipParts = ifaceAddr.getAddress().getHostAddress().split("\\.");
			if(ipParts.length == 4){
				
				broadcastIP = ipParts[0]+"."+ipParts[1]+"."+ipParts[2]+".255";
				try {
					broadcastAddress = InetAddress.getByName(broadcastIP);
				} catch (UnknownHostException e) {					
					e.printStackTrace();
				}
				break;
			}
		} 
        
		//erzeuge das Socket
		DatagramSocket socket = null;
		try {
			socket = new DatagramSocket(Main.broadcastSocketPort, broadcastAddress);
			socket.setBroadcast(true);
		} catch (SocketException e) {
			e.printStackTrace();
		} 
	   
	    //ab hier dauerhaftes hören auf dem Socket
	    while (true) {

	    	byte[] buffer = new byte[512];
		    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
		    String msgString = null;
	    	
	    	try {	
	    	    if(Main.debugMode) System.out.println("InvitationListener: Thread hört auf Broadcast-IP#"+broadcastIp+":"+Main.broadcastSocketPort);
	    		socket.receive(packet);
	    		msgString = new String(buffer);
	    		if(Main.debugMode) System.out.println("InvitationListener: Datagramm empfangen: "+ msgString);
			}catch (IOException e) {				
				e.printStackTrace();
			}
	    	
	    	
	    	//auslesen der Datagramm Message
	    	JSONObject msg = null;
			try {
				msg = new JSONObject(msgString);
			} catch (JSONException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			}

	    	String msgType = null;
	    	String invitationIP = null;
	    	int invitationPort = -1;
	    	
			if(msg.has("msgType")){
    			
				try {    	
	    			msgType = msg.getString("msgType");	
			  		if(msgType.equalsIgnoreCase("invitation")){	    			
		    			invitationIP = msg.getString("ip4");
		    			invitationPort = msg.getInt("port");
		    		}
		  			
				} catch (JSONException e) {
					e.printStackTrace();
				}
				
			}else{
		    		if(Main.debugMode) System.out.println("InvitationListener: inc MSG hat keinen msgType");
	    	}
    			    	
	    	//Wenn die Nachricht eine Einladung eines Servers war, findet nun ein Anmeldeversuch statt
    		Socket newSocket = new Socket();
    		InetAddress inetAddr = null;
    		
			try {
				inetAddr = InetAddress.getByName(invitationIP);
			} catch (UnknownHostException e1) {
				e1.printStackTrace();
			}
	    	
	    	if(	invitationPort != -1 &&
	    		invitationIP != null && 
	    		!addrIsKnown(invitationIP)){
	    		
	    		SocketAddress endpoint = new InetSocketAddress(inetAddr, invitationPort);
	    		
	    		try { 
	    			if(Main.debugMode) System.out.println("InvitationListener: Try to connect Socket to " + inetAddr.getHostAddress()+":"+invitationPort);
					newSocket.connect(endpoint);
					cons.add(new Connection(newSocket));
					if(Main.debugMode) System.out.println("InvitationListener: Verbindung steht:" + newSocket.isConnected() + "CONS#" + cons.size());
				} catch (IOException e) {
					e.printStackTrace();
				}	    		
	    		
	    	}else{
	    		if(Main.debugMode) System.out.println("InvitationListener: inc MSG != invitation | or Host is known");
    		}
	    	
	    	try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    }
	}

	/************************************************************
	 * Ermittelt das Netzwerkinterface des Systems
	 * @return 	the Wifi NetworkInterface 
	 * 			or null if there is no Wifi Interface
	 */
	private NetworkInterface getWifiInterface(){
		
		NetworkInterface wifiInterface = null;
		
		Enumeration<NetworkInterface> interfaces = null;
		try {
			interfaces = NetworkInterface.getNetworkInterfaces();		
			
			while(interfaces.hasMoreElements()){
	
				NetworkInterface iface = interfaces.nextElement(); 
	
				if(Main.debugMode) {
					System.out.println("interface: " + iface.getName() + " isup?: " + iface.isUp() + " isVirtual?: " + iface.isVirtual() + "ip: " + iface.getInetAddresses());
				}
					
				//Ausschließen einiger Interfaces
				if(iface.isLoopback() || !iface.isUp())	 continue; 	

				if( networkInterfaceIsWifiInterface(iface)){
					
					//INTERFACE BESTIMMT!
					return iface;    					
				}
			}
		} catch (SocketException e) {
			e.printStackTrace();
		}
		
		//Kein WifiInterface gefunden
		return wifiInterface;
	}
	
	/************************************************************
	 * Untersucht den Namen des Interfaces und gibt an ob es sich
	 * um ein Wifi Interface handelt oder nicht. Da es keine ein-
	 * heitliche Zuordnung gibt müssen hier ggf. weitere Bezeichner
	 * ergänzt werden.
	 * @param iface - a NetworkInterface
	 * @return true - if param Interface is a Wifi - Interface
	 */
	private boolean networkInterfaceIsWifiInterface(NetworkInterface iface){		
		String ifaceName = iface.getName();
		
		return (ifaceName.contains("wlan") 	||
				ifaceName.contains("wifi") 	||
				ifaceName.contains("wi-fi") ||
				ifaceName.contains("wlp") 	);
	}
	
	/**************************************************************
	 * Gibt zurück ob eine IP Adresse bereits in den Verbindungen 
	 * aufgenommen wurde oder nicht.
	 * @param addr - An InetAddress
	 * @param port - An int Port
	 * @return true - if the InetAddress Port combination is 
	 * 				  is contained in the connectionList
	 */
	private boolean addrIsKnown(String addr){
		
		if(cons != null){
			for(Connection c : cons){
				
				if(c.getSocket().getInetAddress().getHostAddress().equalsIgnoreCase(addr)){
					
					return true;
				}
			}
		}		
		return false;
	}
}
