package talkfeed.command;

import java.util.Map;

import talkfeed.UserManager;

/**
 * Update every class
 * @author vovau
 *
 */
@CommandType("updateuser")
public class CommandUpdateUser implements Command  {

	@Override
	public void execute(Map<String, String> args) {
		
		//id
		String sid = args.get("id");

		if(sid != null){
			long id = Long.parseLong(sid);
			UserManager us = new UserManager();
			us.updateUser(id);
		}

		
	}

}
