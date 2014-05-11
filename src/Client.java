import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;


public class Client implements ServerObservable  {
	
	private Socket clientSocket = null;
	private BufferedReader inputFromClient;
	private PrintWriter outputToClient;
	private String nickname;
	private ArrayList<ServerObserver> serverObservers = new ArrayList<ServerObserver>();

	/**
     * Client Constructor
     * 
     * @param clientSocket
     */
	public Client(Socket clientSocket) 
	{

		this.clientSocket = clientSocket;

		try 
		{
			// start input and output streams to communicate with connected users
			this.inputFromClient = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			this.outputToClient = new PrintWriter(clientSocket.getOutputStream());
			
			// start a communication thread for each client.
			Thread listenerThread = new Thread(new ClientListener());
			listenerThread.start();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * 
	 * @return the nickname of the person
	 */
	public String getNickname() {
		return nickname;
	}

	/**
	 * It's the nickname of a connected client. Without this nickname, a user can't send
	 * message through socket, so it's important to set it at first
	 * 
	 * @param nickname is an arbitrary String
	 */
	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	/**
	 * Send a message to the output stream
	 * 
	 * @param message is a message String
	 */
	public void sendMessage(String message) {
		outputToClient.println(message);
		outputToClient.flush();
	}
	
	/**
	 * Close all opened connections for the current client
	 * 
	 * @param void
	 * @return void
	 */
	public void closeConnections() {
		try 
		{
			inputFromClient.close();
			outputToClient.close();
			clientSocket.close();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * 
	 * @author Vince
	 *
	 */
	public class ClientListener implements Runnable {
		private String line;
		
		@Override
		public void run() {
			try {
				// read input stream from client
				while((line = inputFromClient.readLine().trim()) != null) 
				{	
					// si le message commence par ":user"
					if(line.trim().startsWith(":user")) {
						setNickname(line.substring(5));
					}
					else if(getNickname() != null)
					{
						notifyMessage(getNickname()+" : " +line);
					}
				}
			} 
			catch (IOException e)  {}
			finally
			{			
				// notify observers that the client disconnected
				notifyDisconnection();
				
				// close socket, input and ouput stream for the client
				closeConnections();
			}
		}
	}

	@Override
	public void addObserver(ServerObserver obs) 
	{
		serverObservers.add(obs);
	}

	@Override
	public void removeObserver(ServerObserver obs) 
	{
		serverObservers.remove(obs);
	}

	@Override
	public void notifyDisconnection() 
	{
		for(ServerObserver obs : serverObservers) 
		{
			obs.notifyDisconnection(this);
		}
	}

	@Override
	public void notifyMessage(String m) 
	{
		for(ServerObserver obs : serverObservers) 
		{
			obs.notifyMessage(m);
		}
	}
}
