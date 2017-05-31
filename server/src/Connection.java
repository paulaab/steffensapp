import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.json.JSONException;
import org.json.JSONObject;


/************************************************************
 * Diese Klasse repräsentiert eine Verbindung zu einem
 * Endgerät das Daten dieses SFCDs Anzeigen oder Änderungen
 * an rudimentären Konfigurationen vornehmen möchte.
 * Die Verbindung wird über eine Einladung (UDP) eines
 * mobilen Endgeräts angestoßen und vom SFCD mit einer TCP
 * Verbindung beantwortet. 
 * @author Steffen Ryll
 * @date   23.01.2017
 */
public class Connection {

	//IV*****************************************************
	private Socket socket;
	private DataOutputStream outgoingStream;
	private InputStream incomingStream;
	private List<JSONObject> incomingData, outgoingData;
	public boolean isAlive;
	public boolean close;

	
	/********************************************************
	 * Constructor
	 * Initialisierung der Instanzvariablen 
	 * Öffnen des Input und Output Streams via Threads
	 * @param socket - TCP Socket Verbindung zu einem Device
	 */
	public Connection(Socket socket){
		
		this.socket = socket; 
		incomingData = new CopyOnWriteArrayList<JSONObject>();
		outgoingData = new CopyOnWriteArrayList<JSONObject>();
		
		try {
			outgoingStream = new DataOutputStream(socket.getOutputStream());
			incomingStream = socket.getInputStream();
		} catch (IOException e) {			
			e.printStackTrace();
		}
		
		isAlive = true;
		close = false;
		
		new InputStreamThread().start();
		new OutputStreamThread().start();	
		new ConTimer().start();
	}
	
	/********************************************************
	 * Dieser Thread sendet Nachrichten über das Socket
	 * Dazu wird die Datenstruktur outgoingData abgearbeitet
	 * Zum senden einer nachricht muss als nur ein neuer
	 * Datensatz "outgoingData" hinzugefügt werden
	 * @author Steffen Ryll
	 * 
	 */
	private class OutputStreamThread extends Thread{
		
		JSONObject jMsg = null;
		
		public void run(){
			
			while(!close){
				
				if(!outgoingDataListisEmpty()){
					
					jMsg = outgoingData.remove(0);
					
					String msgString = jMsg.toString();
					msgString += "\n"; //<<< SIGNALISIERT DAS ENDE DER NACHRICHT UND ERMÖGLICHT "readLine()"
					
					//Absenden des Strings
					PrintStream ps = new PrintStream(outgoingStream);
					ps.print(msgString);
					if(Main.debugMode) System.out.print("Connection: "+ socket.getLocalAddress().getHostAddress()+ "Nachricht wurde gesendet! Inhalt: " + msgString);
					
				}
				try {
					Thread.sleep(30);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	
	/********************************************************
	 * Dieser Thread liesst auf dem Socket alle eingehenden
	 * Nachrichten aus und fügt diese der Datenstruktur
	 * incomingData in Form von JSONObjekten hinzu.
	 * @author Steffen Ryll
	 *
	 */
	private class InputStreamThread extends Thread{
		
		BufferedReader breader = new BufferedReader(new InputStreamReader(incomingStream));
		
		public void run(){
			
			while(!close){
				String line = null;
				JSONObject jObj = null;
				
				try {
					//lese eine neue Zeile 
					line = breader.readLine(); // <<< EINGEHENDE NACHRICHTEN MÜSSEN MIT "\n" ENDEN!!!!
				} catch (IOException e) {
					e.printStackTrace();
				}
				
				if(line != null){
					
					try {
						jObj = new JSONObject(line);
					} catch (JSONException e) {
						e.printStackTrace();
					}
					
					String msgType = null;
					
					try {
						msgType = jObj.getString("msgType");
					} catch (JSONException e) {
						e.printStackTrace();
					}
					if(Main.debugMode) System.out.println("Connection: "+socket.getInetAddress() + " Eine Nachricht wurde empfangen! Inhalt: " + jObj.toString() + " => In incomingData abgelegt!");
					if(msgType.equalsIgnoreCase("areualive")){
					
						JSONObject msg = new JSONObject();
						try {
							msg.put("msgType", "iamalive");
						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						sendMessage(msg);
				
					}else if(msgType.equalsIgnoreCase("iamalive")){
						
						isAlive = true;
				
					}else{
						
						incomingData.add(jObj);
						
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

	public class ConTimer extends Thread{
		
		public void run(){
			
			while(!close){
				
				
				isAlive = false;
				
				
				try {
					Thread.sleep(2500);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				if(!isAlive){
					
					System.out.println("KILL CON*************");	
					close = true;
				}								
			}
		}
	}
	
	/********************************************************
	 * Fügt ein Nachrichtenobjekt dem Outputstack hinzu der
	 * in seiner eigenen Abfolge automatisch gesendet wird.
	 * @param jObj - JSONObject 
	 */
	public void sendMessage(JSONObject jObj){
		
		outgoingData.add(jObj);
	}

	/********************************************************
	 * Gibt an ob es weitere Nachrichten zum senden gibt.
	 * @return true - if there are no messages to send
	 */
	private boolean outgoingDataListisEmpty(){
		
		return outgoingData.isEmpty();
	}

	/********************************************************
	 * Getter function
	 * @return the (TCP)Socket form this Connection
	 */
	public Socket getSocket(){
		return socket;
	}
}







































