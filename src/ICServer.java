import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.logging.Logger;

import logger.LoggerManager;

public class ICServer 
{
	private ServerSocket serverSocket = null;
	private Thread connectionThread = null;
	private Socket clientSocket = null;
	private LoggerManager loggerMgr = new LoggerManager();
	private Logger logger;
	private ArrayList<Client> clients = new ArrayList<Client>();
	
	public static final int SERVER_PORT = 1089;

	public ICServer(int logLevel) 
	{
        try {
        	// initiate a new logger with the given level
        	logger = loggerMgr.getLogger(logLevel);
        	
			//InetAddress localAddress = InetAddress.getLocalHost();
        	InetAddress localAddress = InetAddress.getByName("127.0.0.1");
			
			serverSocket = new ServerSocket(SERVER_PORT, 10, localAddress);
			logger.info("Server started");
			
			// start a new thread to accept client without blocking the server
			connectionThread = new Thread(new Connection());
			connectionThread.start();
		} 
		catch (SecurityException e1) 
		{
			logger.severe(e1.getMessage());
		} 
        catch (IOException e1) 
		{
        	logger.severe(e1.getMessage());
		}
	}
	
	/**
	 * Send a message to all connected clients
	 * 
	 * @param message
	 */
	public void broadcast(String message) {
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
	public class Connection implements Runnable
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
					
					// add a new observer to the client observers list
					c.addObserver(new ServerObserver() 
						{
							@Override
							public void notifyDisconnection(Client c) 
							{
								// remove the client from the clients list		
								clients.remove(c);
								
								// log the client disconnection
								logger.info("The client " + c.getNickname() + " has disconnected from the server");
							}

							@Override
							public void notifyMessage(String m) 
							{
								broadcast(m);
							}
						}
					);
					
					clients.add(c);
					logger.info("A new client logged in");
				} 
				catch (IOException e)
				{
					e.printStackTrace();
				}
			}
		}
	}
}
