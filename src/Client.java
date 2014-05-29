import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.rmi.server.UID;
import java.util.ArrayList;
import java.util.HashMap;


public class Client implements ServerObservable  {
	
	private Socket clientSocket = null;
	private ObjectOutputStream outputObjectToClient = null;
    private ObjectInputStream inputObjectFromClient = null;
	private UID id;
	private ArrayList<ServerObserver> serverObservers = new ArrayList<ServerObserver>();
	private UserManager userMgr;
	private User user = new User();

	/**
     * Client Constructor
     * 
     * @param clientSocket
     */
	public Client(Socket clientSocket, UserManager userMgr) 
	{

		this.clientSocket = clientSocket;
		this.userMgr = userMgr;
		this.id = new UID();

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
	}
	
	public UID getId() {
		return id;
	}

	public void setId(UID id) {
		this.id = id;
	}

	/**
	 * Send a message to the output stream
	 * 
	 * @param message is a message String
	 */
	public void sendMessage(String message) {
		//outputToClient.println(message);
		//outputToClient.flush();
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
	public class ClientListener implements Runnable {
		private String line;
		
		@Override
		public void run() {
			try {
				System.out.println("Waiting client message");
				boolean done = false;
				
				byte messageType;
				
				while(true) 
				{

					messageType = inputObjectFromClient.readByte();
					String login;
					String pwd;
					
					switch(messageType)
					{
					case 1: // register
						login = inputObjectFromClient.readUTF();
						pwd = inputObjectFromClient.readUTF();
						
						// lire le fichier des utilisateurs enregistrés.
						if(userMgr.getByLogin(login) == null) 
						{
							// create a user and save it to the users file
							user.setLogin(login);
							user.setPwd(pwd);
							user.setConnected(false);
							userMgr.save(user);
							
							outputObjectToClient.writeByte(50); // user saved command7
							outputObjectToClient.writeUTF("You have been successfully registered to the chat");
							outputObjectToClient.flush();
							outputObjectToClient.reset();
							
							// Send the created user to the client
							outputObjectToClient.writeByte(60);
							outputObjectToClient.writeObject(user);
							outputObjectToClient.flush();
							
							
							// sent users list to ALL clients
							broadcastRegistration();
						} 
						else 
						{
							outputObjectToClient.writeByte(51); // user already registred command
							outputObjectToClient.writeUTF("The user " + login + " is already registred. Please choose an other username");
							outputObjectToClient.flush();
						}
						
					    break;
					case 2: // Login
						login = inputObjectFromClient.readUTF();
						pwd = inputObjectFromClient.readUTF();
						
						// check if the user exist. In that case, check the given password with the password in users list
						if((userMgr.getByLogin(login)) != null) 
						{
							
							if(userMgr.login(login, pwd)) {
								
								// connexion OK => envoyer l'objet 
								outputObjectToClient.writeByte(53); // user successfully loggedin command
								outputObjectToClient.writeUTF("You successfully logged in");
								outputObjectToClient.flush();
								outputObjectToClient.reset();
								
								// send users list to the client
								HashMap<String, User> users = userMgr.getUsers();
				
								try {	
									outputObjectToClient.writeByte(70); // send registered users list
									System.out.println("envoi de " + users.size());
									outputObjectToClient.writeObject(users);
									outputObjectToClient.flush();
								} catch (IOException e) {
									e.printStackTrace();
								}
								
								// user successfully logged command
								outputObjectToClient.writeByte(55);
								
								user.setLogin(login);
								user.setPwd(pwd);
								user.setConnected(true);
								
								outputObjectToClient.writeObject(user);
								outputObjectToClient.flush();
								
							
							}
							else
							{
								outputObjectToClient.writeByte(52); // user saved command
								outputObjectToClient.writeUTF("The user " + login + " doesn't exist or the password is incorrect");
								outputObjectToClient.flush();
							}
						} 
						else 
						{
							outputObjectToClient.writeByte(52); // user already registred command
							outputObjectToClient.writeUTF("The user " + login + " doesn't exist or the password is incorrect");
							outputObjectToClient.flush();
						}
						
					    break;
					case 3: // Send message
					    System.out.println("Message C [1]: " + inputObjectFromClient.readUTF());
					    break;
					default:
					    done = true;
					}
				}
			} 
			catch (IOException e) {} 
			catch (ClassNotFoundException e) {
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

	public ObjectOutputStream getOutputObjectToClient() {
		return outputObjectToClient;
	}

	public void setOutputObjectToClient(ObjectOutputStream outputObjectToClient) {
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

}
