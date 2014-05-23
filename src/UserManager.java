import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;


/**
 * This class save/load users informations
 * @author SB
 *
 */
public class UserManager implements Serializable
{
	// Pour la sérialisation
	static final long serialVersionUID = 20052014L;
	
	private ArrayList<User> users;
	
	public UserManager()
	{
		users = new ArrayList<User>();
	}
	
	public void showUsers()
	{
		for (User u : users)
		{
			System.out.println(u.login);
		}
	}
	
	public void addUser(User u)
	{
		users.add(u);
	}
	
	/**
	 * Load users data from a text file
	 * @author SB
	 */
	public void load()
	{
		try
		{ 
			//String temp = System.getProperty("java.io.tmpdir"); // Windows temp folder
			String path = "C:/Temp/users.txt";
			
			// Check if file exists
			File f = new File(path);
			if(!f.exists())
			{
				// if not, create one with save method
				save();
			}
			
			FileInputStream file = new FileInputStream(path);
			ObjectInputStream ois = new ObjectInputStream(file);
			users.addAll((ArrayList<User>) ois.readObject());
			
			System.out.println("Load done !");
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		catch (ClassNotFoundException e)
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * Save users data on a text file
	 * @author SB
	 */
	public void save()
	{
		try
		{
			//String temp = System.getProperty("java.io.tmpdir");
			String path = "C:/Temp/users.txt";
			
			FileOutputStream fichier = new FileOutputStream(path);
			ObjectOutputStream oos = new ObjectOutputStream(fichier);
			
			oos.writeObject(users);
			oos.flush();
			oos.close();
			
			System.out.println("Save done !");
		}
		catch (java.io.IOException e)
		{
			e.printStackTrace();
		}
	}
}
