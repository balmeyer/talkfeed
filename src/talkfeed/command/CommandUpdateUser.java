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
		//update by jid (email)
		String jid = args.get("jid");

		if(sid != null){
			long id = Long.parseLong(sid);
			UserManager us = new UserManager();
			us.updateUser(id);
		}
		
		if(jid != null){
			UserManager us = new UserManager();
			us.updateUser(jid);
		}

		
	}

}
