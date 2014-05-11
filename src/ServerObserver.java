
public interface ServerObserver {
	
	public void notifyDisconnection(Client c);
	
	public void notifyMessage(String m);
	
}
