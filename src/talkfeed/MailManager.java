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
package talkfeed;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import talkfeed.data.BlogEntry;
import talkfeed.data.DataManager;
import talkfeed.data.DataManagerFactory;
import talkfeed.data.Subscription;
import talkfeed.data.User;

/**
 * Manage mail alerts
 * @author Jean-Baptiste
 *
 */
public class MailManager {

	private static final String HOST = "http://localhost:8888/";
	
	/**
	 * Send mail to user
	 * @param id
	 */
	public void mailUser(long id){
		
		//data
		DataManager dm = DataManagerFactory.getInstance();
		PersistenceManager pm = dm.newPersistenceManager();
		
		//fetch user
		User user = pm.getObjectById(User.class,id);
		
		if (user.getLastEmail() == null){
			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.HOUR, -(4 * 24));
			user.setLastEmail(cal.getTime());
		}
		
		Date now = Calendar.getInstance().getTime();
		
		boolean ismail = false;
		
		StringBuilder email = new StringBuilder();
		
		email.append("Hello ");
		email.append(user.getId());
		email.append("\r\n\r\nHere are your updates since ");
		email.append(user.getLastEmail());
		email.append("\r\n\r\n");
		
		//retrieve subscriptions
		//fetch sub
		Query sq = pm.newQuery(Subscription.class);
		sq.setFilter("userKey == uk && lastDate > date");
		sq.declareParameters("com.google.appengine.api.datastore.Key uk, java.util.Date date");
		
		@SuppressWarnings("unchecked")
		List<Subscription> ls = (List<Subscription>) sq.execute(user.getKey(),user.getLastEmail());
		
		//fetch new subscriptions
		for(Subscription sub : ls){
			//fetch entries
			Query qEntry = pm.newQuery(BlogEntry.class);
			qEntry.setFilter("blogKey == bk && pubDate > date");
			qEntry.declareParameters("com.google.appengine.api.datastore.Key bk, java.util.Date date");
			
			//fetch entries
			List<BlogEntry> entries = (List<BlogEntry>) qEntry.execute(sub.getBlogKey(),user.getLastEmail());
			
			boolean title = false;
			for(BlogEntry entry : entries){
				ismail = true;
				if (!title) {
					title = true;
					email.append("\r\n");
					email.append(entry.getBlogTitle());
					email.append("\r\n");
				}
				email.append(" * ");
				email.append(entry.getTitle());
				email.append("\r\n  ");
				email.append(entry.getLink());
				email.append("\r\n");
			}
			qEntry.closeAll();
		}
		sq.closeAll();
		
		email.append("\r\nClick here to unsubscribe : ");
		email.append(HOST);
		email.append("stopmail/");
		email.append(user.getKey().getId());
		email.append("\r\nOr type 'email stop' to the talkfeed bot in gtalk.");
		System.out.println(email.toString());
	}

	
}
