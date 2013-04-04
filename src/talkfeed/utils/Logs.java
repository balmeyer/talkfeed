package talkfeed.utils;

import java.util.logging.Logger;

public class Logs {


	 private static final Logger log = Logger.getLogger(Logs.class.getName());
	
	
	public static void info(String message){

		log.info(message);

	}
}
