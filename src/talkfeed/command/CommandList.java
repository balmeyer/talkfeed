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

import java.util.List;
import java.util.Map;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import talkfeed.data.Blog;
import talkfeed.data.DataManager;
import talkfeed.data.DataManagerFactory;
import talkfeed.data.Subscription;
import talkfeed.data.User;
import talkfeed.talk.TalkService;

import com.google.appengine.api.xmpp.JID;



/**
 * List current subscriptions
 */
@CommandType("list")
public class CommandList implements Command{

	@Override
	public void execute(Map<String, String> args) {

		String id = args.get("id");
		JID jid = new JID(id);
		
		DataManager dm = DataManagerFactory.getInstance();
		
		PersistenceManager pm = dm.newPersistenceManager();
		
		//fetch user
		User u = dm.getUserFromId(id);
		
		Query q = pm.newQuery(Subscription.class);
		q.setFilter("userKey == k");
		q.declareParameters("com.google.appengine.api.datastore.Key k");
		
		@SuppressWarnings("unchecked")
		List<Subscription> subs = (List<Subscription>) q.execute(u.getKey());
		
		int n = 0;
		StringBuilder sb = new StringBuilder();
		for(Subscription s : subs){
			Blog b = pm.getObjectById(Blog.class, s.getBlogKey());
			
			sb.append(++n);
			if(b.getTitle() != null){
				sb.append(" -[");
				sb.append(b.getTitle());
				sb.append("] ");
			} else {
				sb.append(" - ");
			}
			sb.append(b.getLink());
			sb.append("\r\n");
			//list.add(sb.toString());
		}
		
		pm.close();

		
		//send list
		TalkService.sendMessage(jid, sb.toString());
		/*
		for(String s : list){
			System.out.println(sb.toString());
			TalkService.sendMessage(jid, s);
		}*/
		
	}

}
