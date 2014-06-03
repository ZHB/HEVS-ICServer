import java.util.ArrayList;
import java.util.List;


public interface ServerObserver
{
	
	public void notifyDisconnection();
	
	public void notifyMessage(String m);
	
	public void updateRegisteredUsersList(Client c);
	
	//public void broadcastToSelectedUsers(List l, Message messages);

	public void sendMsgToUser(User selectedUser, User userFrom, Message msg);
	
}
