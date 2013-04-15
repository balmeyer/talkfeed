package talkfeed.command;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

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
			try {
			UserManager us = new UserManager();
			us.updateUser(jid);
			} catch (Exception ex){
				Logger.getLogger("CommandUpdateUser").log(Level.SEVERE,
						"Error update " + jid,ex);
			}
		}

		
	}

}
