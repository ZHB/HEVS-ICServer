import java.util.ArrayList;
import java.util.List;


public interface ServerObserver {
	
	public void notifyDisconnection(Client c);
	
	public void notifyMessage(String m);
	
	public void broadcastRegistration(Client c);
	
	public void broadcastToSelectedUsers(List l, Message messages);
	
}
