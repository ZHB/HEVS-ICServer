
public interface ServerObservable
{
	
	public void addObserver(ServerObserver obs);
	
	public void removeObserver(ServerObserver obs);
	
	/**
	 * Notify the server that a disconnection occurred
	 */
	public void notifyDisconnection();
	
	public void updateRegisteredUsersList();
	
	/**
	 * Send a message to a desired user.
	 * 
	 * @param selectedUser the user to send the message
	 * @param userFrom the user from which the message comes from
	 * @param msg the message itself
	 */
	public void sendMsgToUser(User selectedUser, User userFrom, Message msg);
	
}
