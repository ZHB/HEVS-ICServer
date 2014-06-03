import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.rmi.server.UID;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;


public class Client implements ServerObservable  {
	
	private Socket clientSocket = null;
	private ObjectOutputStream outputObjectToClient = null;
    private ObjectInputStream inputObjectFromClient = null;
	private String id;
	private ArrayList<ServerObserver> serverObservers = new ArrayList<ServerObserver>();
	private UserManager userMgr;
	private User user;

	private User selectedUser;
	

	/**
     * Client Constructor
     * 
     * @param clientSocket
     */
	public Client(Socket clientSocket, UserManager userMgr) 
	{

		this.clientSocket = clientSocket;
		this.userMgr = userMgr;
		
		Random random = new Random();
	    String tag = Long.toString(Math.abs(random.nextLong()), 36);
	    this.id =  tag.substring(0, 8);
	    
		//this.id = new UID();

		try 
		{	
			this.outputObjectToClient = new ObjectOutputStream(clientSocket.getOutputStream());
            this.inputObjectFromClient = new ObjectInputStream(clientSocket.getInputStream());

			// start a communication thread for each client.
			Thread listenerThread = new Thread(new ClientListener());
			listenerThread.start();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
		
		// send the generated ID to the user
		sendClientId();
	}
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
	
	public User getUser() {
		return user;
	}
	
	private void sendClientId() {
		// send the generated Client ID back to the connected client
		try {
			outputObjectToClient.writeByte(120);
			outputObjectToClient.writeUTF(id.toString());
			outputObjectToClient.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	

	/**
	 * Send a message to the output stream
	 * 
	 * @param message is a message String
	 */
	public void sendMessage(String message)
	{
		try
		{
			outputObjectToClient.writeByte(21);
			outputObjectToClient.writeUTF(message);
			outputObjectToClient.flush();
			outputObjectToClient.reset();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	public void sendMessage(User userFrom, Message message)
	{
		try
		{
			outputObjectToClient.writeByte(21);
			outputObjectToClient.writeUTF("[" + message.getFormatedDate() + "] " + userFrom.getLogin() + ": " + message.getMessage());
			outputObjectToClient.flush();
			outputObjectToClient.reset();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * Send a message to the output stream
	 * 
	 * @param message is a message String
	 */
	public void sendUser(User u) {
		
		System.out.println("Before Broadcast: "+ u.toString());
		try {
			outputObjectToClient.writeByte(100);
			outputObjectToClient.writeObject(u);
			outputObjectToClient.flush();
			outputObjectToClient.reset();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Get all registered users from the HashMap and send it to 
	 * the client via the output stream
	 */
	public void sendRegisteredUsers() {
		
		HashMap<String, User> users = userMgr.getUsers();

		try {	
			outputObjectToClient.writeByte(101); // send registered users list
			outputObjectToClient.writeObject(users);
			outputObjectToClient.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
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
			outputObjectToClient.close();
			inputObjectFromClient.close();
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
	public class ClientListener implements Runnable
	{
		@Override
		public void run()
		{
			try
			{
				byte messageType;
				
				while(true) 
				{
					messageType = inputObjectFromClient.readByte();

					switch(messageType)
					{
					case 1: // register
						// get the User from the client
						user = (User) inputObjectFromClient.readObject();
						
						// check if the User is already registred
						if(userMgr.getByLogin(user.getLogin()) == null) 
						{
							// create a user and save it to the users file
							userMgr.save(user);
							
							// send a notification message to the client
							sendMessage("You have been successfully registered to the chat");
							
							// sent users list to ALL clients
							broadcastRegistration();
						} 
						else 
						{
							sendMessage("The user " + user.getLogin() + " is already registred. Please choose an other username");
						}
						
					    break;
					case 2: // Unregister
						// get the User from the client
						user = (User) inputObjectFromClient.readObject();
						
						userMgr.delete(user);
						
						user.setConnected(false);
						user.setPwd(null);
						user.setId(null);
						user.setLogin(null);
						
						// send a notification message to the client
						sendMessage("You have been successfully unregistered from the chat");
						
						// sent users list to ALL clients
						broadcastRegistration();
						
						
						
					    break;
					case 11: // Login
						
						// get the User from the client
						user = (User) inputObjectFromClient.readObject();
						

						if(userMgr.getByLogin(user.getLogin()) != null && userMgr.login(user.getLogin(), user.getPwd())) {
							
							
							// update user status and send it to client
							user.setConnected(true);
							user.setId(id.toString());
							userMgr.setUser(user); // update user in users list
							
							// update the user to the client
							sendUser(user);	
							
							
							System.out.println("Login :" + user.toString());
							
							// sent users list to ALL clients
							broadcastRegistration();
							
							// send a notification message to client
							sendMessage("You successfully logged in as " + user.getLogin());
							
							// send all registered users to client
							sendRegisteredUsers();
							
							// send action code
							outputObjectToClient.writeByte(11); // send registered users list
							outputObjectToClient.flush();
						}
						else
						{
							sendMessage("The user " + user.getLogin() + " doesn't exist or the password is incorrect");
						}
						
					    break;
					case 12: // Logout
						
						user.setConnected(false);
						user.setLogin(null);
						user.setPwd(null);
						user.setId(null);
						sendUser(user);	
						
						// sent users list to ALL clients
						broadcastRegistration();
						
						// send a notification message to client
						sendMessage("You have been successfully disconnected from the chat");
						
						outputObjectToClient.writeByte(12); // send registered users list
						outputObjectToClient.flush();
						
					    break;
					case 21: // Reception message
						Message message = (Message) inputObjectFromClient.readObject();
						User userFrom = (User) inputObjectFromClient.readObject();
	
						
						String conversationKey = selectedUser.getLogin();

						
						if(user.getConversation(conversationKey) == null) 
						{
							// create a new conversation for the user
							user.createConversation(conversationKey);
						}
						
						// add a message to a conversation
						user.setConversation(conversationKey, message);
						
						System.out.println(user.getConversation(conversationKey).size());

						// TODO : ajouter vérification connecté
						if(selectedUser == null) {
							sendMessage("Please, select at least a user to chat with !");
						} 
						else 
						{
							sendMsgToUser(selectedUser, userFrom, message);
							//broadcastToSelectedUsers(selectedUser, message);
						}
					    break;
					    
					case 111: // User selection to chat with
						selectedUser = (User) inputObjectFromClient.readObject();
						
						System.out.println(selectedUser.toString());
						
						// résupérer les conversations de l'utilisateur dans le HashMap
						if(user.getConversation(selectedUser.getLogin()) != null) {
							System.out.println("renvoi de l'utilisateur sélectionné " + selectedUser.getLogin() + " " + selectedUser.getId());
							
							for(Message m : user.getConversation(selectedUser.getLogin())) 
							{
								System.out.println(m.getFormatedDate() + " " + m.getMessage());
							}
						}
						
						
					    break;
					}
				}
			} 
			catch (IOException e)
			{
				
			} 
			catch (ClassNotFoundException e)
			{
				e.printStackTrace();
			}
			
			finally
			{		
				// notify observers that the client disconnected
				notifyDisconnection();
				
				// close socket, input and ouput stream for the client
				closeConnections();
			}
		}
	}

	public ObjectOutputStream getOutputObjectToClient()
	{
		return outputObjectToClient;
	}

	public void setOutputObjectToClient(ObjectOutputStream outputObjectToClient)
	{
		this.outputObjectToClient = outputObjectToClient;
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
	public void notifyMessage(String m) 
	{
		for(ServerObserver obs : serverObservers) 
		{
			obs.notifyMessage(m);
		}
	}

	@Override
	public void broadcastRegistration() {
		for(ServerObserver obs : serverObservers) 
		{
			obs.broadcastRegistration(this);
		}
		
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
	public void sendMsgToUser(User selectedUser, User userFrom, Message msg)
	{
		for(ServerObserver obs : serverObservers) 
		{
			obs.sendMsgToUser(selectedUser, userFrom, msg);
		}	
	}
}
