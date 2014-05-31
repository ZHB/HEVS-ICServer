package logger;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LoggerManager {

	//Get the logger
	private static final Logger myLogger = Logger.getLogger("ICServer");

	//Define a file handler
	private FileHandler fh;
	
	//get the current date
	private Calendar currentDate = Calendar.getInstance();
	private SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
	private String dateNow = formatter.format(currentDate.getTime());
	
	private int logLevel;
		
	
	public Logger getLogger(int logLevel) throws SecurityException, IOException 
	{
		this.logLevel = logLevel;
		
		setLogLevel();
		
		// define a new file handler and its log
		fh = new FileHandler(".\\logs\\" + dateNow + ".log", true);       
		//fh = new FileHandler("output.log", 1048576, 1, true);
		myLogger.addHandler(fh);
		//setLogLevel();
		
		//use a custom formatter 
		//SocketFormatter myFormatter = new SocketFormatter();
		fh.setFormatter(new SocketFormatter());

		return myLogger;
	}
	
	public void close() {
		fh.close();
	}
	
	private void setLogLevel() {
		//check valid values
		if ((logLevel > 3) || (logLevel < 1))
		{
			myLogger.setLevel(Level.INFO);
		}
		
		switch(logLevel){
		case 1 :
			myLogger.setLevel(Level.INFO);
			break;
		case 2 :
			myLogger.setLevel(Level.WARNING);
			break;
		case 3 :
			myLogger.setLevel(Level.SEVERE);
			break;
		
		}
		
	}
}
