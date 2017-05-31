import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Diese Klasse bezieht aus dem Netcat Server von Tactilo
 * ein vorgefertigtes Datenpaket bestehend aus den AT Commands
 * AT+GSTATUS? und AT+LTEINFO?
 * Der Netcat Serverport lautet 1235
 * Der Netcat Server wird aktiviert durch die Installation der ModemAPI
 * siehe "Description extended telematic platform V1" von Tactilo
 * das File: "2016_10_14_ModemAPI.tar" wird dazu benötigt 
 * 
 * @author Steffen Ryll	
 *
 */

public class NetcatCollector extends Thread{
	
	ConnectionManager conMan;
	
	public NetcatCollector(ConnectionManager conMan){
		
		this.conMan = conMan;
	}
	
	public void run(){		
	
		while(true){
			
			//TCP Aufbau
			Socket clientSocket = null;
			try {
				clientSocket = new Socket("127.0.0.1" , 1235);
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			
			//Rückgabe auffangen
			char[] buffer = new char[4096];
			int charsRead = 0;
			String msg = null;
			try {
				BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
				while((charsRead = in.read(buffer)) != -1){
					
					msg = new String(buffer).substring(0,charsRead);
				}
			
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			//Rückgabe strukturieren und formatieren
			
			//Daten parsen : GSTATUS

			

			int keyStart = msg.indexOf("\n", msg.indexOf("!GSTATUS", 0)) + 1;
			int keyEnd = 0;
			int valueStart = 0;
			int valueEnd = 0;
			
			JSONObject jobj_gstatus = new JSONObject();
			try {
				jobj_gstatus.append("msgType", "gstatus");
			} catch (JSONException e) {				
				e.printStackTrace();
			}
			

			
			String key = "";
			String value = "";
			String[] split = msg.split("!LTEINFO: ");
			String gstatus = split[0];
			
			//TODO
			String lteinfo = split[1];
			
			String[] lines = gstatus.split("\\n");
			
			String line = "";
			
			for(int i = 3 ; i < lines.length; i++){
					
				line = lines[i];
				keyStart = -1;
				keyEnd = -1;
				valueStart = -1;
				valueEnd = -1;
				int secKeyEnd = -1;
				
				keyEnd = line.indexOf(":");
				secKeyEnd = line.indexOf(":",keyEnd+1);
				
				if(keyEnd != -1){
					if(secKeyEnd != -1){
						//System.out.println("Line "+i+" hat zwei Parameter »>> " + lines[i]);
						
						//erster Key fertig
						key = line.substring(0,keyEnd);
						
						//suche ersten Value
						valueStart = keyEnd;
						char ch = ' ';			
						do{
							ch = line.charAt(++valueStart);
						}while((valueStart < lines.length)&&(ch == ' ' || ch == '\t'|| ch =='\r'));
						
						int temp1 = line.indexOf("\t", valueStart);
						int temp2 = line.indexOf("  ", valueStart);

						
						if(temp2 == -1 || temp1 < temp2) 	
							valueEnd = temp1;
						else
							valueEnd = temp2;
						
						//erster Value fertig
						value = line.substring(valueStart, valueEnd);
						
						//***********MAKE JSON*******************
						try {
							jobj_gstatus.append(key, value);
						} catch (JSONException e) {							
							e.printStackTrace();
						}					
						//***************************************
						
						//suche zweiten Key
						keyStart = secKeyEnd;
	
						do{
							ch = line.charAt(--keyStart);
						}while(ch != '\t');
						
						//zweiter Key fertig
						key = line.substring(keyStart+1, secKeyEnd);
						
						//suche zweiten Value
						valueStart=secKeyEnd;
						do{
							ch = line.charAt(++valueStart);
						}while(ch == ' ' || ch == '\t'|| ch =='\r');
						
						valueEnd = line.length()-1;

						//zweiter Value Fertig
						value = line.substring(valueStart, valueEnd);
						
						//hier müssen ggf. noch whitespace hinter dem Wort entfernt werden
						value = value.trim();
						
						//***********MAKE JSON*******************
						try {
							jobj_gstatus.append(key, value);
						} catch (JSONException e) {							
							e.printStackTrace();
						}
						//***************************************

						
					}else{
						//System.out.println("Line "+i+" hat einen Parameter »>> " + lines[i]);
						//erster Key fertig
						key = line.substring(0,keyEnd);
						
						//suche ersten Value
						valueStart = keyEnd;
						char ch = ' ';			
						do{
							ch = line.charAt(++valueStart);
						}while((valueStart < lines.length)&&(ch == ' ' || ch == '\t'|| ch =='\r'));
						
						valueEnd = line.length();
						
						//erster Value fertig
						value = line.substring(valueStart, valueEnd);
						//hier müssen ggf. noch whitespace hinter dem Wort entfernt werden
						value = value.trim();
						value = value.replace("  ", "");
						value = value.replace("\t", "");
						
						//***********MAKE JSON*******************
						try {
							jobj_gstatus.append(key, value);
						} catch (JSONException e) {							
							e.printStackTrace();
						}
						//***************************************
											
					}						
					
				}
				
			}//END FOR
			
			//Daten direkt senden
			if(!conMan.getConnenctions().isEmpty()){
				
				conMan.sendToAll(jobj_gstatus);
			}
			
			
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}//end WHILE
	}
}

/*
 * --GSTATUS JSON OBJECT--
{	
	"Temperature":["46"],
	"LTE Rx chan":["2850"],
	"msgType":["at:gstatus"],
	"IMS reg state":["No Srv"],
	"EMM state":["Registered Normal Service"],
	"Current Time":["79558"],
	"SINR (dB)":["29.0"],
	"Mode":["ONLINE"],
	"RRC state":["RRC Idle"],
	"RSRP (dBm)":["-88","-85"],
	"Cell ID":["00B2FC0D (11729933)"],
	"LTE band":["B7"],
	"Tx Power":["0"],
	"PCC RxD RSSI":["-59"],
	"LTE Tx chan":["20850"],
	"Reset Counter":["1"],
	"LTE bw":["20 MHz"],
	"TAC":["ABE7 (44007)"],
	"LTE CA state":["NOT ASSIGNED"],
	"RSRQ (dB)":["-6.1"],
	"System mode":["LTE"],
	"PS state":["Attached"],
	"PCC RxM RSSI":["-62"]
}
*/

