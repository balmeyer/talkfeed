package talkfeed.command;

import java.util.Map;

import talkfeed.utils.Logs;
import talkfeed.xmpp.TalkService;

/**
 * Echo respons to user (test purpose)
 * 
 * @author jean-baptiste
 *
 */
@CommandType("say")
public class CommandSay implements Command {

	@Override
	public void execute(Map<String, String> args) {

	
		String id = args.get("id");
		String msg = args.get("msg");
		
		Logs.info("say. ID : " + id + ", msg : " + msg);
		
		if (id != null && msg != null){
			TalkService.sendMessage(id, msg);
		}
	
	}

}
