import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Observable;
import java.util.Observer;


public class Client extends Observable  {
	
	private Socket clientSocket = null;
	private BufferedReader inputFromClient;
	private PrintWriter outputToClient;
	private String nickname;

	/**
     * Client Constructor
     * 
     * @param clientSocket
     */
	public Client(Socket clientSocket) {
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
	
	public String getNickname() {
		return nickname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	/**
	 * Send a message to the output stream
	 * 
	 * @param message is a message String
	 * @return void
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
			System.out.println("Connection closed.");
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
	}
	
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
						// notify observer (the server class) so message can be send to all or specific users
						setChanged();
						notifyObservers(getNickname()+" : " +line);
					}
				}
			} 
			catch (IOException e)  
			{
				e.printStackTrace();
			}
			finally
			{
				// close socket, input and ouput stream for the client
				closeConnections();
			}
		}
	}
}
