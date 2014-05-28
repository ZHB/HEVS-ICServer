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
	
	public UserManager() {
		this.users = load();
	}
	
	// load the user HashMap
	public HashMap<String, User> load() {
		
		try{
	        File toRead = new File(userFilePath);
	        FileInputStream fis=new FileInputStream(toRead);
	        ObjectInputStream ois=new ObjectInputStream(fis);

	        HashMap<String, User> users = (HashMap<String, User>)ois.readObject();

	        ois.close();
	        fis.close();
	        
	        //print All data in MAP
	        for(Map.Entry<String, User> u :users.entrySet()){
	            System.out.println(u.getKey()+" : "+u.getValue());
	        }
	    }catch(Exception e){}
		
		return users;
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
	public void save(User user)
	{
		// add the user to the map
		users.put(user.getLogin(), user);
		
	    try{
	    	File fileOne=new File(userFilePath);
	    	FileOutputStream fos=new FileOutputStream(fileOne);
	        ObjectOutputStream oos=new ObjectOutputStream(fos);
	        
	        oos.writeObject(users);
	        oos.flush();
	        oos.close();
	        fos.close();
	    }
	    catch(Exception e)
	    {
	    	e.printStackTrace();
	    }
	}
	
	public boolean login(String name, String pwd) throws IOException, ClassNotFoundException {
		
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
