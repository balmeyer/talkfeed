/*
 Copyright 2010/2012 - Jean-Baptiste Vovau

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

import static com.google.appengine.api.taskqueue.TaskOptions.Builder.withUrl;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import talkfeed.data.Blog;
import talkfeed.data.BlogEntry;
import talkfeed.data.DataManagerFactory;
import talkfeed.data.Subscription;
import talkfeed.data.User;
import talkfeed.data.UserMark;
import talkfeed.talk.TalkService;
import talkfeed.url.UrlShortenFactory;
import talkfeed.utils.CacheService;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.google.appengine.api.taskqueue.TaskOptions.Method;
import com.google.appengine.api.xmpp.JID;
import com.google.appengine.api.xmpp.Presence;


/**
 * Send subscription via user
 * @author vovau
 *
 */
public class UserService {

	private static final String USERMARK_ID = "1";
	private static final int NB_SUBSCRIPTION_MAX = 15;
	
	public void updateUsers(int nbMax){
		
		PersistenceManager pm = DataManagerFactory.getInstance().newPersistenceManager();
		
		//find mark
		long lastId = 1;
		Query qm = pm.newQuery(UserMark.class);
		qm.setRange(0, 1);
		qm.setUnique(true);
		UserMark um = (UserMark) qm.execute();
		if (um == null){
			um = new UserMark();
			um.setId(USERMARK_ID);
			um.setLastId(1);
			pm.makePersistent(um);
		}
		
		lastId = um.getLastId();
		if (lastId <1) lastId = 1;
		
		//find user
		Query q = pm.newQuery(User.class);
		q.setFilter("key >= lastId");
		q.setOrdering("key");
		q.declareParameters("Long lastId");
		q.setRange(0 , nbMax);
		
		//list user
		boolean hasListed = false;
		List<User> list = (List<User>) q.execute(lastId);
		
		for(User u : list) {
			//ask for update
			Queue queue = QueueFactory.getDefaultQueue();

			TaskOptions options = withUrl("/tasks/updateuser")
					.method(Method.GET).param("id",
							String.valueOf(u.getKey().getId()));

			// add to Queue
			queue.add(options);
			
			hasListed = true;
			lastId = u.getKey().getId() + 1;
		}
		
		//no one listed : start to beginning
		if (!hasListed) lastId = 1;
		
		//end of process
		um.setLastId(lastId);
		pm.flush();
		pm.close();
		
	}
	
	/**
	 * Send notification to user, if connected
	 * @param id
	 */
	public void updateUser(long id){
		//nowaday
		Date now = Calendar.getInstance().getTime();
		
		//find user
		PersistenceManager pm = DataManagerFactory.getInstance().newPersistenceManager();
		
		User user = (User) pm.getObjectById(User.class, id);
		
		// test user presence
		JID jid = new JID(user.getId());
		Presence presence = TalkService.getPresence(jid);
		
		if (presence != null && presence.isAvailable()){
			//update !
			
			//acceptable date
			Calendar acdate = Calendar.getInstance();
			acdate.add(Calendar.HOUR, -1);
			Date acceptableDate = acdate.getTime();
			
			// select subscriptions
			Query q = pm.newQuery(Subscription.class);
			q.setOrdering("lastProcessDate");
			q.setRange(0, NB_SUBSCRIPTION_MAX);
			q.setFilter("userKey == uk && lastProcessDate < date");
			q.declareParameters("com.google.appengine.api.datastore.Key uk, java.util.Date date");

			@SuppressWarnings("unchecked")
			List<Subscription> subs = (List<Subscription>) q.execute(user.getKey(),acceptableDate);

			//update
			for(Subscription sub : subs){
				//fetch blog
				Blog blog = (Blog) pm.getObjectById(Blog.class, sub.getBlogKey());
				
				//last analyze date
				sub.setLastProcessDate(now);
				pm.currentTransaction().begin();
				pm.flush();
				pm.currentTransaction().commit();
				
				//compare dates
				if (blog.getLastUpdate().after(sub.getLastDate())){
					//TODO notify user
					Date lastdate = this.notifySubscriptionAndReturnLastDate(pm, sub, jid);
					if (lastdate == null) lastdate = now;
					sub.setLastDate(lastdate);
					pm.currentTransaction().begin();
					pm.flush();
					pm.currentTransaction().commit();
					
					//blog
					user.setLastUpdate(now);
					user.setLastSubscriptionKey(sub.getKey());
					pm.currentTransaction().begin();
					pm.flush();
					pm.currentTransaction().commit();
					break;
					
				}
				
				
			}
			
			//flush
			//pm.currentTransaction().begin();
			//pm.flush();
			//pm.currentTransaction().commit();
			q.closeAll();
			pm.close();
			
		}
	}
	

	/**
	 * Notify new BlogEntry to user and get new "last blogEntry date"
	 * return null if no entry is needed
	 * 
	 * @param pm
	 * @param sub
	 * @param jabberId
	 * @return
	 */
	private Date notifySubscriptionAndReturnLastDate(PersistenceManager pm,
			Subscription sub, JID jabberId) {

		//Date now = Calendar.getInstance().getTime();
		Date newDate = null;

		// find oldest entry from blog subscription which haven't been sent
		Query q = pm.newQuery(BlogEntry.class);
		q.setFilter("blogKey == blog && pubDate > date");
		q.setOrdering("pubDate");
		q.declareParameters("com.google.appengine.api.datastore.Key blog, java.util.Date date");
		q.setUnique(true);
		q.setRange(0, 1);

		//find blog entry
		BlogEntry entryToPush = (BlogEntry) q.execute(sub.getBlogKey(),
				sub.getLastDate());

		if (entryToPush != null) {
			// error link : no stanza, try next
			if (entryToPush.getLink() == null) {
				if (entryToPush.getPubDate() == null) {
					return Calendar.getInstance().getTime();
				} else {
					return entryToPush.getPubDate();
				}
			}

			//fetch link
			String link = entryToPush.getShortLink();
			
			if (link == null){
				link = UrlShortenFactory.getInstance().shorten(entryToPush.getLink());
			} 
			
			String blogTitle = entryToPush.getBlogTitle();
			//TODO remove this when production
			if (blogTitle == null) blogTitle = getBlogTitle(pm, entryToPush);
					
			StringBuilder sb = new StringBuilder();
			sb.append('[');
			sb.append(blogTitle);
			sb.append("] ");
			sb.append(entryToPush.getTitle());
			sb.append(" : ");
			sb.append(link);

			// send message
			TalkService.sendMessage(jabberId, sb.toString());
			// mark last show date

			newDate = entryToPush.getPubDate();
			
			
			
			//save short link
			if (entryToPush.getShortLink() == null){
				entryToPush.setShortLink(link);
				pm.currentTransaction().begin();
				pm.flush();
				pm.currentTransaction().commit();
			}
			
			q.closeAll();

		} else {
			// no new entry : return no date
			newDate = null; // new Date();
		}

		return newDate;
	}

	/**
	 * To ensure transition
	 * @param pm
	 * @param be
	 * @return
	 */
	private String getBlogTitle(PersistenceManager pm, BlogEntry be) {

		//TODO remove this
		
		if (be == null)
			return null;

		Key k = be.getBlogKey();
		String key = "title" + String.valueOf(k.getId());

		Object title = CacheService.get(key);

		if (title == null) {
			Blog b = pm.getObjectById(Blog.class, k);
			title = b.getTitle();
			if (title != null)
				CacheService.put(key, title);
			else
				return "";
		}

		return title.toString();
	}
}
