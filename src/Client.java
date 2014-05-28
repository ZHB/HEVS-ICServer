import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.rmi.server.UID;
import java.util.ArrayList;


public class Client implements ServerObservable  {
	
	private Socket clientSocket = null;
	private BufferedReader inputFromClient;
	private PrintWriter outputToClient;
	private ObjectOutputStream outputObjectToClient = null;
    private ObjectInputStream inputObjectFromClient = null;
	private String nickname;
	private UID id;
	private ArrayList<ServerObserver> serverObservers = new ArrayList<ServerObserver>();
	private UserManager userMgr;

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
			// start input and output streams to communicate with connected users
			this.inputFromClient = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			this.outputToClient = new PrintWriter(clientSocket.getOutputStream());
			
			
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
				
				while(!done) 
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
							userMgr.save(new User(login, pwd));
							
							outputObjectToClient.writeByte(50); // user saved command7
							outputObjectToClient.writeUTF("You have been successfully registered to the chat");
							outputObjectToClient.flush();
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
						if(userMgr.getByLogin(login) != null) 
						{
							
							if(userMgr.login(login, pwd)) {
								
								
								outputObjectToClient.writeByte(53); // user saved command
								outputObjectToClient.writeUTF("You successfully logged in");
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
	
				inputObjectFromClient.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
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

	@Override
	public void notifyRegistration() {
		for(ServerObserver obs : serverObservers) 
		{
			//obs.notifyMessage(m);
		}
		
	}
}
