import java.io.Serializable;
import java.util.ArrayList;


public class User implements Serializable
{
	
	private String login;
	private String pwd;
	private ArrayList<Discussion> discussions;
	
	public User(String login, String pwd)
	{
		this.login = login;
		this.pwd = pwd;
	}
	
	public String getPwd() {
		return pwd;
	}

	public void setPwd(String pwd) {
		this.pwd = pwd;
	}

	public void setLogin(String login) {
		this.login = login;
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
