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

import talkfeed.blog.SubscriptionService;
import talkfeed.data.DataManagerFactory;
import talkfeed.data.User;

@CommandType("next")
public class CommandNext implements Command {

	@Override
	public void execute(Map<String, String> args) {
		// TODO Auto-generated method stub

		String jid = args.get("id");
		
		if (jid == null) return;
		
		User user = DataManagerFactory.getInstance().getUserFromId(jid);
		
		SubscriptionService subs = new SubscriptionService();
		subs.sendNotifications(user);
	}

}
