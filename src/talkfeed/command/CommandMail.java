package talkfeed.command;

import java.util.Map;

import talkfeed.MailManager;

@CommandType("mail")
public class CommandMail implements Command {

	@Override
	public void execute(Map<String, String> args) {
		//id
		String sid = args.get("id");
		//update by jid (email)
		//String jid = args.get("jid");
		
		long id = Long.valueOf(sid);
		
		MailManager mm = new MailManager();
		mm.mailUser(id);
		
	}

}
