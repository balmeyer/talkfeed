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

import talkfeed.UserManager;

/**
 * Refresh user subscriptions
 * @author JBVovau
 *
 */
@CommandType("refreshsubscriptions")
public class CommandRefreshSubscriptions implements Command {

	@Override
	public void execute(Map<String, String> args) {
		
		/*
		SubscriptionService serv = new SubscriptionService();
		serv.sendNotifications(NB_MAX ); */
		
		UserManager serv = new UserManager();
		//serv.updateUsers(NB_MAX);
		serv.updatePresentUsers();
	}

}
