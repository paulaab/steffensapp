import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.json.JSONException;
import org.json.JSONObject;

/************************************************************
 *
 * @author Steffen Ryll
 * @mail   steffen_ryll@outlook.de
 * @date   23.01.2017
 */
public class ConnectionManager {
	
	private List<Connection> connections;

	
	public ConnectionManager(){
		
		connections = new CopyOnWriteArrayList<>();
		new InvitationListener(connections).start();
		new conCheckerThread().start();
	}
	
	public List<Connection> getConnenctions(){
		
		return connections;
	}
	
	public void sendToAll(JSONObject jobj){
		
		for(Connection con : connections){
			
			con.sendMessage(jobj);
		}
	}
	
	/**
	 * Sendet jede Sekunde eine Mitteilung an alle Connections 
	 * um festzustellen ob das Socket noch eine Verbindung hat
	 * @author Steffen Ryll
	 *
	 */
	private class conCheckerThread extends Thread{
		
		public void run(){
			
			while(true){				
				
				if(!connections.isEmpty()){		
					
					for(Connection con : connections){
						
						if(con.close){
							
							connections.remove(con);
							
							
						}
					}
					
					JSONObject msg = new JSONObject();
					try {
						msg.put("msgType", "areualive");
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
}
