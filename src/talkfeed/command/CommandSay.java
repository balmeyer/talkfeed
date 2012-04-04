package talkfeed.command;

import java.util.Map;

import talkfeed.gtalk.TalkService;

@CommandType("say")
public class CommandSay implements Command {

	@Override
	public void execute(Map<String, String> args) {
		// TODO Auto-generated method stub
	
		String id = args.get("id");
		String msg = args.get("msg");
		
		if (id != null && msg != null){
			System.out.println(msg);
			TalkService.sendMessage(id, msg);
		}
	
	}

}
