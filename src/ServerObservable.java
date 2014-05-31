import java.util.ArrayList;
import java.util.List;


public interface ServerObservable {
	
	public void addObserver(ServerObserver obs);
	
	public void removeObserver(ServerObserver obs);
	
	/**
	 * Notify the server that a disconnection occurred
	 */
	public void notifyDisconnection();
	
	public void broadcastRegistration();
	
	public void notifyMessage(String m);
	
	public void broadcastToSelectedUsers(List l, Message messages);
	
}
