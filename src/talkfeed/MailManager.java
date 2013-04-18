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

	/**
	 * Send mail to user
	 * @param id
	 */
	public void mailUser(long id){
		System.out.println(id);
	}
	
	private void mailFromDate(long id , Date last){
		
		//data
		DataManager dm = DataManagerFactory.getInstance();
		PersistenceManager pm = dm.newPersistenceManager();
		
		//fetch user
		User user = pm.getObjectById(User.class,id);
		
		if (user.getLastEmail() == null){
			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.HOUR, -(7 * 24));
			user.setLastEmail(cal.getTime());
		}
		
		Date now = Calendar.getInstance().getTime();
		
		boolean ismail = false;
		
		//retrieve subscriptions
		//fetch sub
		Query sq = pm.newQuery(Subscription.class);
		sq.setFilter("userKey == uk && lastDate > date");
		sq.declareParameters("com.google.appengine.api.datastore.Key bk, java.util.Date");
		
		@SuppressWarnings("unchecked")
		List<Subscription> ls = (List<Subscription>) sq.execute(user.getKey(), user.getLastEmail());
		
		//fetch new subscriptions
		for(Subscription sub : ls){
			//fetch entries
			Query qEntry = pm.newQuery(BlogEntry.class);
			qEntry.setFilter("blogKey == bk && pubDate > date");
			sq.declareParameters("com.google.appengine.api.datastore.Key bk, java.util.Date");
			
			//fetch entries
			List<BlogEntry> entries = (List<BlogEntry>) qEntry.execute(sub.getBlogKey() , user.getLastEmail());
			
			String title = null;
			for(BlogEntry entry : entries){
				if (title == null) title = entry.getBlogTitle();
			}
		}
		
	}
	
}
