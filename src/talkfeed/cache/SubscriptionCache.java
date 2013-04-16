/*
 Copyright 2010/2013 - Jean-Baptiste Balmeyer

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


package talkfeed.cache;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import talkfeed.data.DataManager;
import talkfeed.data.DataManagerFactory;
import talkfeed.data.Subscription;
import talkfeed.data.User;
import talkfeed.utils.TextTools;

/**
 * Caching subscriptions
 * @author balmeyer
 *
 */
public class SubscriptionCache {

	private static PersistenceManager currentPm;

	private static final String KEY_USER_SUB = "KEY_USER_SUB_";
	
	private SubscriptionCache(){}
	
	@SuppressWarnings("unchecked")
	public static Collection<Long> getUserBlogs(String email){
		email = TextTools.cleanJID(email);
		//fetch in cache
		List<Long> blogs = new ArrayList<Long>();
		String key = KEY_USER_SUB + email;
		Object tmp = CacheService.get(key);
		
		if (tmp != null){
			//subscriptions list in cache
			blogs = (List<Long>) tmp;
		} else {
			//TODO if not in cache : fetch from datastore
			Query qUser = getPM().newQuery(User.class);
			qUser.setFilter("id == email");
			qUser.declareParameters("String email");
			qUser.setRange(0, 1);
			qUser.setUnique(true);

			User user = (User) qUser.execute(email);
			
			if (user != null){
				//fetch subscription
				// select subscriptions
				Query q = getPM().newQuery(Subscription.class);
				q.setFilter("userKey == uk");
				q.declareParameters("com.google.appengine.api.datastore.Key uk");

				List<Subscription> subs = (List<Subscription>) q.execute(user
						.getKey());
				for(Subscription sub : subs){
					long id = sub.getBlogKey().getId();
					if (!blogs.contains(id)){
						blogs.add(id);
					}
				}
				q.closeAll();
				q = null;
			}
			qUser.closeAll();
			qUser = null;
			
			CacheService.put(key, blogs);
		}
		
		
		
		return blogs;
	}
	
	/**
	 * Remove subscriptions from cache.
	 * @param userId
	 */
	public static void removeUserFromCache(String user){
		user = TextTools.cleanJID(user);
		String key = KEY_USER_SUB + user;
		CacheService.remove(key);
	}
	
	/**
	 * Free datastore
	 */
	public static void releaseDataStore(){
		if (currentPm != null){
	
			currentPm.close();
			currentPm = null;
		}
	}
	
	
	private static PersistenceManager getPM(){
		if (currentPm == null){
		DataManager dm = DataManagerFactory.getInstance();
		currentPm = dm.newPersistenceManager();
		}
		return currentPm;
	}
}
