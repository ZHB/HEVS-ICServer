import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.logging.Logger;

import logger.LoggerManager;

public class ICServer 
{
	private ServerSocket serverSocket = null;
	private Thread connectionThread = null;
	private Socket clientSocket = null;
	private LoggerManager loggerMgr = new LoggerManager();
	private Logger logger;
	
	private String servIp = "127.0.0.1";
	private int servPort = 1096;
	
	private Client c;
	private UserManager userMgr;
	private HashMap<String, Client> clients = new HashMap<String, Client>();

	public ICServer(int logLevel) 
	{
        init(logLevel); // Parameter to the init method for log level
	}
	
	public void init(int logLevel)
	{
		userMgr = new UserManager();
		userMgr.load();
		
		
		try
        {
        	// initiate a new logger with the given level
        	logger = loggerMgr.getLogger(logLevel);
        	
			//InetAddress localAddress = InetAddress.getLocalHost();
        	InetAddress localAddress = InetAddress.getByName(servIp);
			
			serverSocket = new ServerSocket(servPort, 10, localAddress);
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
					c  = new Client(clientSocket, userMgr);
					
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
		public void notifyDisconnection() 
		{
			// remove the client from the clients list		
			clients.remove(c.getId());
			
			// log the client disconnection
			logger.info("The client " + c.getId() + " has disconnected from the server");
		}

		@Override
		public void notifyMessage(String m) 
		{
			broadcast(m);
		}

		@Override
		public void updateRegisteredUsersList() 
		{
			for (String key : clients.keySet()) 
			{
				// update registered users list for all connected clients
				if(clients.get(key).getUser().isConnected())
				{
					clients.get(key).sendRegisteredUsers();
				}
			}
		}
		
		
		/**
		 * @inheritDoc
		 */
		@Override
		public void sendMsgToUser(User selectedUser, User userFrom, Message msg)
		{
			// send message only to user which is connected (and who has an active connection)
			if(selectedUser.getId() != null) 
			{
				if(selectedUser.isConnected()) 
				{
					clients.get(selectedUser.getId()).sendMessage(userFrom, msg);
				}
			}
		}
	}
}
