import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
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
	
	private void addConversationToMap(User userToAdd, String conversationKey, String m)
	{
		// check if the conversation exists otherwise create a new one
		if(userToAdd.getConversation(conversationKey) == null) 
		{
			userToAdd.createConversation(conversationKey);
		}
		
		// add a message to a conversation
		userToAdd.setConversation(conversationKey, m);
	}
	
	public void sendMessage(User userFrom, Message message)
	{
		// add conversation to the user HashMap
		addConversationToMap(user, userFrom.getLogin(), "[" + message.getFormatedDate() + "] " + userFrom.getLogin() + ": " + message.getMessage());
		addConversationToMap(userFrom, user.getLogin(), "[" + message.getFormatedDate() + "] " + userFrom.getLogin() + ": " + message.getMessage());
		
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
	public void sendUpdatedUser(User u)
	{
		try
		{
			outputObjectToClient.writeByte(100);
			outputObjectToClient.writeObject(u);
			outputObjectToClient.flush();
			outputObjectToClient.reset();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * Get all registered users from the HashMap and send it to 
	 * the client via the output stream
	 */
	public void sendRegisteredUsers()
	{
		try
		{	
			outputObjectToClient.writeByte(101); // send registered users list
			outputObjectToClient.writeObject(userMgr.getUsers());
			outputObjectToClient.flush();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * Close all opened connections for the current client
	 * 
	 * @param void
	 * @return void
	 */
	private void closeConnections() {
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
				boolean done = false;
				
				while(true && !done) 
				{
					messageType = inputObjectFromClient.readByte();

					switch(messageType)
					{
					case 1: // register
						// get the User from the client
						user = (User) inputObjectFromClient.readObject();
						
						// check if the User is already registered
						if(userMgr.getByLogin(user.getLogin()) == null) 
						{
							// create a user and save it to the users file
							userMgr.register(user);
							
							// send a notification message to the client
							sendMessage("You have been successfully registered to the chat");
							
							// sent users list to ALL clients
							updateRegisteredUsersList();
						} 
						else 
						{
							sendMessage("The user " + user.getLogin() + " is already registred. Please choose an other username");
						}
					    break;
					    
					case 2: // Unregister
					
						// unregister user
						userMgr.unregister(user);
						userMgr.load();

						// send a notification message to the client
						sendMessage("You have been successfully unregistered from the chat");
						
						// sent users list to ALL clients
						updateRegisteredUsersList();
						
						// notify GUI that the disconnection is successful
						outputObjectToClient.writeByte(12);
						outputObjectToClient.flush();
						
					    break;
					    
					case 11: // Login
						
						// get the User from the client
						user = (User) inputObjectFromClient.readObject();
						
						if(userMgr.getByLogin(user.getLogin()) != null && userMgr.login(user.getLogin(), user.getPwd()))
						{
							// chargement de l'utilisateur 
							user = userMgr.getByLogin(user.getLogin());
							
							// update user status and send it to client
							user.setConnected(true);
							user.setId(id);
							
							userMgr.updateUser(user); // update user in users hashmap
							userMgr.save();
							userMgr.load();

							// update the user to the client
							sendUpdatedUser(user);	
							
							// sent users list to ALL clients
							updateRegisteredUsersList();
							
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
						
						userMgr.updateUser(user); // update user in users hashmap
						userMgr.updateUser(selectedUser);
						
						sendUpdatedUser(user);	
						userMgr.save();
						userMgr.load();
						
						// sent users list to ALL clients
						updateRegisteredUsersList();
						
						// send a notification message to client
						sendMessage("You have been successfully disconnected from the chat");
						
						// notify GUI that the disconnection is successful
						outputObjectToClient.writeByte(12);
						outputObjectToClient.flush();
						
					    break;
					case 13: // Close chat
						if(selectedUser != null) 
						{
							userMgr.updateUser(selectedUser);
							userMgr.save();
							userMgr.load();
						}
						
						if(user != null) 
						{
							user.setConnected(false);
							user.setId(null);
							userMgr.updateUser(user);
							userMgr.save();
							userMgr.load();
							
							// sent users list to ALL clients
							updateRegisteredUsersList();
						}
						
						done = true;
						
					    break;
					case 21: // Reception message
						Message message = (Message) inputObjectFromClient.readObject();
						User userFrom = (User) inputObjectFromClient.readObject();
	
						// check if the selected user exists
						if(selectedUser == null) 
						{
							sendMessage("Please, select at least a user to chat with !");
						} 
						else 
						{
							// add conversation to current User with selected User as conversation key
							addConversationToMap(user, selectedUser.getLogin(), "["+message.getFormatedDate()+"] " + user.getLogin() + " :" + message.getMessage());
							
							// add conversation to selectedUser with current user as key
							addConversationToMap(selectedUser, user.getLogin(), "["+message.getFormatedDate()+"] " + user.getLogin() + " :" + message.getMessage());
							
							// send messages respectively to selected user and to ourself
							sendMsgToUser(selectedUser, userFrom, message);
							sendMessage(user, message);
							
							// update and save users HashMap
							userMgr.updateUser(user);
							userMgr.updateUser(selectedUser);
							userMgr.save();
						}
						
					    break;
					    
					case 111: // User selection to chat with
						selectedUser = (User) inputObjectFromClient.readObject();
						
						// get conversation in the HashMap from desired user (selected user as key)
						if(user.getConversation(selectedUser.getLogin()) != null) 
						{
							ArrayList<String> messages = user.getConversation(selectedUser.getLogin());

							outputObjectToClient.writeByte(121); // send registered users list
							outputObjectToClient.writeObject(messages);
							outputObjectToClient.flush();
						}
						
					    break;
					}
				}
			} 
			catch (IOException e)
			{
				e.printStackTrace();
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
	public void updateRegisteredUsersList() 
	{
		for(ServerObserver obs : serverObservers) 
		{
			obs.updateRegisteredUsersList();
		}
		
	}

	@Override
	public void notifyDisconnection() 
	{
		for(ServerObserver obs : serverObservers) 
		{
			obs.notifyDisconnection();
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
