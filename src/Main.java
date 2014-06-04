
public class Main {

	public static void main(String[] args)
	{	
		int logLevel = 1;
		
		// read command line args
		if(args.length > 0)
		{
			try {
				logLevel = Integer.parseInt(args[0]);
				
				if(logLevel > 3 || logLevel < 1)
				{
					System.out.println("Argument " + args[0] + " must be between 1 and 3. Strarting server with log level 3");
					logLevel = 3;
				}
		    } catch (NumberFormatException e) {
		        System.err.println("Invalid log level argument. Argument " + args[0] + " must be an integer.");
		        System.exit(1);
		    }
		}
		
		ICServer server = new ICServer(logLevel);
	}
}
