import java.util.ArrayList;
import java.util.List;


public interface ServerObserver
{
	
	public void notifyDisconnection();
	
	public void notifyMessage(String m);
	
	public void updateRegisteredUsersList();
	
	//public void broadcastToSelectedUsers(List l, Message messages);

	/**
	 * Send a message to a desired user.
	 * 
	 * @param selectedUser the user to send the message
	 * @param userFrom the user from which the message comes from
	 * @param msg the message itself
	 */
	public void sendMsgToUser(User selectedUser, User userFrom, Message msg);
	
}
