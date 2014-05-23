import java.io.Serializable;
import java.util.ArrayList;


public class User implements Serializable
{
	
	String login;
	String pwd;
	ArrayList<Discussion> discussions;
	
	public User(String login, String pwd)
	{
		this.login = login;
		this.pwd = pwd;
	}
	
	public void addDiscussion(Discussion disc)
	{
		discussions.add(disc);
	}
	
	public String getLogin()
	{
		return login;
	}
	
}
