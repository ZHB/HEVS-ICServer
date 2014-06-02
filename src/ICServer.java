import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import logger.LoggerManager;

public class ICServer 
{
	private ServerSocket serverSocket = null;
	private Thread connectionThread = null;
	private Socket clientSocket = null;
	private LoggerManager loggerMgr = new LoggerManager();
	private Logger logger;
	
	private HashMap<String, Client> clients = new HashMap<String, Client>();

	private UserManager userMgr = new UserManager();
	
	public static final int SERVER_PORT = 1096;

	public ICServer(int logLevel) 
	{
		
				
        try
        {
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
	public void broadcast(String message)
	{
		
		for (String key : clients.keySet()) 
		{
			clients.get(key).sendMessage(message);
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
					Client c  = new Client(clientSocket, userMgr);
					
					// add a new observer to the client observers list
					c.addObserver(new SrvObserver());
					clients.put(c.getId().toString(), c);
					
					logger.info("A new client logged in");
				} 
				catch (IOException e)
				{
					e.printStackTrace();
				}
			}
		}
	}
	
	/**
	 * Internal class that observe client actions. 
	 * 
	 * @author Vince
	 *
	 */
	public class SrvObserver implements ServerObserver
	{
		@Override
		public void notifyDisconnection(Client c) 
		{
			// remove the client from the clients list		
			clients.remove(c.getId().toString());
			
			// log the client disconnection
			logger.info("The client " + c.getId() + " has disconnected from the server");
		}

		@Override
		public void notifyMessage(String m) 
		{
			broadcast(m);
		}

		@Override
		public void broadcastRegistration(Client c) 
		{
			
			HashMap<String, User> users = userMgr.getUsers();

			for (String key : clients.keySet()) 
			{
				// update registered users list for all connected clients
				clients.get(key).sendRegisteredUsers();
			}
		}

		/*
		@Override
		public void broadcastToSelectedUsers(List l, Message message) 
		{
			// save conversation to a file on the server only for oneToOne conversations
			
			// Send messages only to connected clients !
			for (String key : clients.keySet()) 
			{
				//if(l.contains(clients.get(key).getUser().getLogin())) 
				//{
					clients.get(key).sendMessage(message);
				//}
			}
		}
		*/
		
		@Override
		public void sendMsgToUser(User u, Message msg)
		{
			
			// register conversation to a file
			
			
			// Check is destination client is available (connected)
			for (Map.Entry<String, Client> e : clients.entrySet())
			{
				Client c = e.getValue();
				if (c.getUser().getLogin().equals(u.getLogin()))
				{
					//System.out.println("Envoi a : " + c.getUser().getLogin());
					
					c.sendMessage(msg);
				}
			}
			
			
		}

	}
}
