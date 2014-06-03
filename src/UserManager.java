import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * This class save/load users informations
 * @author SB
 *
 */
public class UserManager implements Serializable
{

	private static final String userFilePath = "./data/users.txt";
	private HashMap<String, User> users = new HashMap<String, User>();
	
	public UserManager()
	{
		
	}
	
	public HashMap<String, User> getUsers()
	{
		return users;
	}

	/*
	public void setUsers(HashMap<String, User> users)
	{
		this.users = users;
	}
	*/
	
	public void updateUser(User u) 
	{
		System.out.println("Update status: " + u.toString());
		users.put(u.getLogin(), u);
	}

	// Load users data from the file
	public void load()
	{
		try
		{
	        File toRead = new File(userFilePath);
	        if (!toRead.exists())
	        {
	        	save();
	        }
	        FileInputStream fis = new FileInputStream(toRead);
	        ObjectInputStream ois=new ObjectInputStream(fis);

	        users = (HashMap<String, User>) ois.readObject();

	        ois.close();
	        fis.close();
	    }
		catch(Exception e)
		{
			
		}
		
		//return users;
	}
	
	// Save users data to the file
	public void save()
	{
        try
        {
        	FileOutputStream fos = new FileOutputStream(userFilePath);
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			
			oos.writeObject(users);
			oos.flush();
			oos.close();
		}
        catch (IOException e)
        {
			e.printStackTrace();
		}
	}
	
	public User getByLogin(String login) throws IOException, ClassNotFoundException
	{
		// Check if file exists
		File f = new File(userFilePath);
		
		if(!f.exists())
		{
			return null;
		}
		
		FileInputStream file = new FileInputStream(userFilePath);
		ObjectInputStream ois = new ObjectInputStream(file);
		
		// read file and create the HashMap
		users = (HashMap<String, User>) ois.readObject();
		
		file.close();
		ois.close();
		 
		// get the user by his login (the HashMap key)
		//System.out.println("getByLogin returned the user " + users.get(login).getLogin());
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
	}
	
	/**
	 * Unregister
	 * @param user
	 */
	public void unregister(User user)
	{
		// remove the user from the map -> unregister
		users.remove(user.getLogin());
		save();
	}
	
	public boolean login(String name, String pwd) throws IOException, ClassNotFoundException
	{
		
		// Check if file exists
		File f = new File(userFilePath);
		
		if(!f.exists())
		{
			return false;
		}
		
		FileInputStream file = new FileInputStream(userFilePath);
		ObjectInputStream ois = new ObjectInputStream(file);
		
		// read file and create the HashMap
		users = (HashMap<String, User>) ois.readObject();
		
		file.close();
		ois.close();
		 
		// get the user by his login (the HashMap key)
		//System.out.println("getByLogin returned the user " + users.get(login).getLogin());
		User u = users.get(name);
		

		// check if given strings are equals
		if(u.getPwd().equals(pwd.trim())) {
			return true;
		}
		
		return false;
	}
}
