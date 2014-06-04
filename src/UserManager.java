import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * This class save/load users informations
 * @author SB
 *
 */
//public class UserManager implements Serializable
public class UserManager
{

	private static final String userFilePath = "./data";
	private static final String userFileName = "users.txt";
	private String userFilePathName = "";
	private Map<String, User> users = new HashMap<String, User>();
	private Logger logger; 
	
	public UserManager(Logger logger)
	{
		this.logger = logger;
		this.userFilePathName = userFilePath + "/" + userFileName;
	}
	
	public Map<String, User> getUsers()
	{
		return users;
	}
	
	public void updateUser(User u) 
	{
		if(u != null)
		{
			users.put(u.getLogin(), u);
		}
	}

	// Load users data from the file
	public void load()
	{
		
		try
		{
	        File toRead = new File(userFilePathName);
	        if (!toRead.exists())
	        {
	        	save();
	        	logger.info("File " + userFilePathName + " created");
	        }
	        
	        FileInputStream fis = new FileInputStream(toRead);
	        ObjectInputStream ois = new ObjectInputStream(fis);

	        users = (HashMap<String, User>) ois.readObject();

	        ois.close();
	        fis.close();
	    }
		catch(Exception e)
		{
			logger.severe(e.getMessage());
		}
	}
	
	// Save users data to the file
	public void save()
	{
        try
        {
        	// check if the logs directory, if not create it
    		File theDir = new File(userFilePath);
    		if (!theDir.exists()) {
    		    theDir.mkdir(); 
    		    logger.info("Directory " + userFilePath + " created");
    		}
    		
        	FileOutputStream fos = new FileOutputStream(userFilePathName);
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			
			oos.writeObject(users);
			oos.flush();
			oos.close();
		}
        catch (IOException e)
        {
        	logger.severe(e.getMessage());
		}
	}
	
	public User getByLogin(String login)
	{
		// Check if file exists
		File f = new File(userFilePathName);
		
		if(!f.exists())
		{
			logger.warning("File " + userFilePathName + " doesn't exists");
			return null;
		}
		
		FileInputStream file;
		try {
			file = new FileInputStream(userFilePathName);
			ObjectInputStream ois = new ObjectInputStream(file);
			
			// read file and create the HashMap
			users = (HashMap<String, User>) ois.readObject();
			
			file.close();
			ois.close();
		} 
		catch (IOException e) 
		{
			logger.severe(e.getMessage());
		} 
		catch (ClassNotFoundException e) 
		{
			logger.severe(e.getMessage());
		}
		
		// get the user by his login (the HashMap key)
		return users.get(login);
	}
	
	/**
	 * Save users data on a text file
	 * @author SB
	 */
	public void register(User user)
	{
		// add the user to the map -> register
		users.put(user.getLogin(), user);
		save();
		
		logger.info("New user registered " + user.getLogin());
	}
	
	/**
	 * Unregister
	 * @param user
	 */
	public void unregister(User user)
	{
		// remove the conversation from the map
		users.get(user.getLogin()).removeConversation(user.getLogin());
				
		// remove the user from the map -> unregister
		users.remove(user.getLogin());
		
		save();
		
		logger.info("User " + user.getLogin() + " has unregistered");
	}
	
	public boolean login(String name, String pwd) throws IOException, ClassNotFoundException
	{
		
		// Check if file exists
		File f = new File(userFilePathName);
		
		if(!f.exists())
		{
			logger.warning("The file " + userFilePathName + " doesn't exists");
			return false;
		}
		
		FileInputStream file = new FileInputStream(userFilePathName);
		ObjectInputStream ois = new ObjectInputStream(file);
		
		// read file and create the HashMap
		users = (HashMap<String, User>) ois.readObject();
		
		file.close();
		ois.close();
		 
		// get the user by his login (the HashMap key)
		User u = users.get(name);
		

		// check if given strings are equals
		if(u.getPwd().equals(pwd.trim())) {
			return true;
		}
		
		logger.warning("Loggin error for user " + u.getLogin() + ". The given password is incorrect");
		
		return false;
	}
}
