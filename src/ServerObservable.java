
public interface ServerObservable {
	
	public void addObserver(ServerObserver obs);
	
	public void removeObserver(ServerObserver obs);
	
	/**
	 * Notify the server that a disconnection occurred
	 */
	public void notifyDisconnection();
	
	public void notifyMessage(String m);
	
}
