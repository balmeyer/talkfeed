/*
 Copyright 2010 - Jean-Baptiste Vovau

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package talkfeed.command;

import java.util.Map;

import talkfeed.data.DataManager;
import talkfeed.data.DataManagerFactory;
import talkfeed.gtalk.TalkService;

/**
 * manning account
 * @author JBVovau
 *
 */
@CommandType("account")
public class CommandAccount implements Command {

	@Override
	public void execute(Map<String, String> args) {
		String id = args.get("id");
		String run = args.get("run");
		
		String time = args.get("time");
		String unit = args.get("unit");
		
		if (id == null) return;
		
		DataManager dm = DataManagerFactory.getInstance();
		
		//manage updates
		if (run != null){
			boolean isPaused = run.equals("0");
			
			dm.updateUserActivity(id, !isPaused);
			
			String reply = null;
			if (isPaused){
				reply = "updates are paused !";
			} else {
				reply = "welcome back !";
			}
			TalkService.sendMessage(id, reply);
			return;
		}
		
		//interval between updates
		if (time != null && unit != null){
			int minutes = Integer.parseInt(time);
			
			if (unit.toLowerCase().startsWith("hour")){
				minutes = minutes * 60;
			}
			
			dm.updateUserInterval(id, minutes);
			TalkService.sendMessage(id, "blog update every " + minutes + " minute(s)");
		}
	}

}
