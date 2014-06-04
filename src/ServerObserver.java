public interface ServerObserver
{
	/**
	 * Inform that a client has disconnected from the chat
	 */
	public void notifyDisconnection();
	
	/**
	 * Update the list of registered users
	 */
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
