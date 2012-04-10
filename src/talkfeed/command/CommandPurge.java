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

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import talkfeed.data.DataManager;
import talkfeed.data.DataManagerFactory;
import talkfeed.data.Subscription;
import talkfeed.data.User;
import talkfeed.gtalk.TalkService;

/**
 * Purge all pending entries in queue for one user
 * @author JBVovau
 *
 */
@CommandType("purge")
public class CommandPurge implements Command{

	@Override
	public void execute(Map<String, String> args) {

		String jid = args.get("id");
		
		Date now = Calendar.getInstance().getTime();
		
		if (jid != null){
			DataManager dm = DataManagerFactory.getInstance();
			PersistenceManager pm = dm.newPersistenceManager();
			
			//fetch user
			Query quser = pm.newQuery(User.class);
			quser.setFilter("id == jid");
			quser.declareParameters("java.lang.String jid");
			quser.setUnique(true);
			
			User user = (User) quser.execute(jid);
			
			if (user != null){
				//fetch all subscriptions
				Query q = pm.newQuery(Subscription.class);
				q.setFilter("userKey == key");
				q.declareParameters("com.google.appengine.api.datastore.Key key");
				
				@SuppressWarnings("unchecked")
				List<Subscription> list = (List<Subscription>) q.execute(user.getKey());
				
				//mark all user's subscription as read (by recent date)
				for(Subscription s : list){
					pm.currentTransaction().begin();
					s.setLatestEntryNotifiedDate(now);
					s.setLastProcessDate(now);
					pm.currentTransaction().commit();
					pm.flush();
				}
				
			}

			pm.close();
			TalkService.sendMessage(jid, "all feeds are purged !");
		}
	}

}
