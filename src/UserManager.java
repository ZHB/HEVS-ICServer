import java.io.Serializable;
import java.util.ArrayList;


/**
 * This class save/load clients informations
 * @author sbob
 *
 */
public class ClientManager implements Serializable
{
	// SB / Pour la sérialisation
	static final long serialVersionUID = 20052014L;
	
	private ArrayList<Client> clients;
	
	public ClientManager(ArrayList<Client> clients)
	{
		this.clients = clients;
	}
	
	public void load()
	{
		
	}
	
	public void save()
	{
		
	}
}
