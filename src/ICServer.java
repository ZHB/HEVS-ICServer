import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;
import java.util.logging.Logger;

import javax.swing.SwingUtilities;


public class ICServer
{
	private int logLevel;
	
	private InetAddress localAddress;
	private ServerSocket serverSocket = null;
	private Thread connectionThread = null;
	private Socket clientSocket = null;
	
	private ArrayList<Client> clients = new ArrayList<Client>();
	
	
	public static final int SERVER_PORT = 1078;
	
	public ICServer(int logLevel) 
	{
		this.logLevel = logLevel;
		
		try 
		{
			//localAddress = InetAddress.getLocalHost();
			//serverSocket = new ServerSocket(SERVER_PORT, 10, localAddress);
			serverSocket = new ServerSocket(SERVER_PORT);
			System.out.println("Server started");
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
		
		// start a new thread to accept client without blocking the server
		connectionThread = new Thread(new Connection());
		connectionThread.start();
	}
	
	/**
	 * Close current connection
	 */
	public void close() {
		try {
			clients.remove(clientSocket);
			clientSocket.close();
			connectionThread.interrupt();
		}
		catch (IOException e) 
		{
			
		};
	}
	
	/**
	 * Send a message to all connected clients
	 * 
	 * @param message
	 */
	public void broadcast(String message) {
		System.out.println("Boadcast : " + message);
		for(Client c : clients) 
		{
			c.sendMessage(message);
		}
	}
	
	
	/**
	 * Accept new clients to the server
	 * 
	 * @author Vince
	 *
	 */
	public class Connection implements Runnable, Observer
	{
		@Override
		public void run() 
		{
			// infinite loop to accept multiple client
			while(true) 
			{
				try
				{
					// accept a new client and create a socket
					clientSocket = serverSocket.accept();
					Client c  = new Client(clientSocket);
					c.addObserver(this);
					clients.add(c);
					
					System.out.println("A new client just logged in");

				} 
				catch (IOException e)
				{
					e.printStackTrace();
				}
			}
		}
		
		// Retrieve notifications from server Client class and broadcast to all connected Client clients
		@Override
		public void update(Observable o, Object arg) {
			broadcast(arg.toString());
		}
	}
	
}
