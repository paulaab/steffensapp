import java.util.List;

public class Main {

	public static final int broadcastSocketPort = 45555;
	public static boolean debugMode = true;

	
	public static void main(String[] args){
	    

		ConnectionManager conMan = new ConnectionManager();
		
		new NetcatCollector(conMan).start();
				
	}
}
