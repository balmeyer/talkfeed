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

import static com.google.appengine.api.taskqueue.TaskOptions.Builder.withUrl;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import talkfeed.data.Blog;
import talkfeed.data.BlogEntry;
import talkfeed.data.DataManager;
import talkfeed.data.DataManagerFactory;
import talkfeed.data.Subscription;
import talkfeed.data.User;
import talkfeed.talk.TalkService;
import talkfeed.url.UrlShorten;
import talkfeed.url.UrlShortenFactory;
import talkfeed.utils.CacheService;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.google.appengine.api.taskqueue.TaskOptions.Method;
import com.google.appengine.api.xmpp.JID;
import com.google.appengine.api.xmpp.Presence;

public class SubscriptionService {

	// caching
	private final Map<Long, Date> blogToDate = new HashMap<Long, Date>();

	private UrlShorten urlShorten;

	/**
	 * Build notifications to send to users
	 * 
	 * @param nbMax
	 */
	public void sendNotifications(int nbMax) {

		// clear cache datas
		blogToDate.clear();

		DataManager dm = DataManagerFactory.getInstance();
		PersistenceManager pm = dm.newPersistenceManager();

		// select subscriptions
		Query q = pm.newQuery(Subscription.class);
		q.setOrdering("lastProcessDate");
		q.setRange(0, nbMax);

		// user that have received update
		Set<Long> users = new HashSet<Long>();

		@SuppressWarnings("unchecked")
		List<Subscription> subs = (List<Subscription>) q.execute();

		for (Subscription subscription : subs) {
			// user has already received an update
			if (users.contains(subscription.getUserKey().getId()))
				continue;

			// find blog and compare update time
			Date blogLastUpdate = blogToDate.get(subscription.getBlogKey()
					.getId());

			// not found in cache
			if (blogLastUpdate == null) {
				try {
					Blog blog = pm.getObjectById(Blog.class,
							subscription.getBlogKey());
					blogLastUpdate = blog.getLastUpdate();
					blogToDate.put(blog.getKey().getId(), blogLastUpdate);
				} catch (JDOObjectNotFoundException ex) {
					// blog deleted ! bad !!
					pm.currentTransaction().begin();
					pm.deletePersistent(subscription);
					pm.currentTransaction().commit();
					pm.flush();
					continue;
				}
			}

			// compare date between last update blog and subscriptions
			if (blogLastUpdate.after(subscription.getLastDate())) {
				// newest entries in blog !
				// updateBlog + " is newest than " + subscription.getLastDate())

				Queue queue = QueueFactory.getDefaultQueue();

				TaskOptions options = withUrl("/tasks/updatesubscription")
						.method(Method.GET).param("id",
								String.valueOf(subscription.getKey().getId()));

				// add to Queue
				queue.add(options);
				// add user
				users.add(subscription.getUserKey().getId());

			}

		}

		q.closeAll();
		pm.close();
	}

	/**
	 * Send next notification to user
	 * 
	 * @param user
	 */
	public void sendNotifications(User user) {
		if (user == null)
			return;

		DataManager dm = DataManagerFactory.getInstance();
		PersistenceManager pm = dm.newPersistenceManager();

		// select subscriptions
		Query q = pm.newQuery(Subscription.class);
		q.setOrdering("lastProcessDate");
		q.setRange(0, 25);
		q.setFilter("userKey == uk");
		q.declareParameters("com.google.appengine.api.datastore.Key uk");

		@SuppressWarnings("unchecked")
		List<Subscription> subs = (List<Subscription>) q.execute(user.getKey());

		boolean hasUpdate = false;

		for (Subscription subscription : subs) {

			// find blog and compare update time
			Date updateBlog = blogToDate.get(subscription.getBlogKey().getId());

			// not found in cache
			if (updateBlog == null) {
				try {
					Blog blog = pm.getObjectById(Blog.class,
							subscription.getBlogKey());
					updateBlog = blog.getLastUpdate();
					blogToDate.put(blog.getKey().getId(), updateBlog);
				} catch (JDOObjectNotFoundException ex) {
					// blog deleted ! bad !!
					pm.currentTransaction().begin();
					pm.deletePersistent(subscription);
					pm.currentTransaction().commit();
					pm.flush();
					continue;
				}
			}

			// compare date
			if (updateBlog.after(subscription.getLastDate())) {
				// newest entries in blog !
				// updateBlog + " is newest than " + subscription.getLastDate())

				Queue queue = QueueFactory.getDefaultQueue();

				TaskOptions options = withUrl("/tasks/updatesubscription")
						.method(Method.GET).param("id",
								String.valueOf(subscription.getKey().getId()));

				// add to Queue
				queue.add(options);
				hasUpdate = true;
				// only one blog
				break;
			}
		}

		// nothing has been shown
		if (!hasUpdate) {
			TalkService.sendMessage(user.getId(), "all feeds are read !");
		}

	}

	/**
	 * Notify to gtalk user a single Subscription
	 * 
	 * @param id
	 */
	public void notifySubscription(long id) {
		DataManager dm = DataManagerFactory.getInstance();
		PersistenceManager pm = dm.newPersistenceManager();

		// fetch subscription
		Subscription sub = pm.getObjectById(Subscription.class, id);
		// fetch user
		User user = pm.getObjectById(User.class, sub.getUserKey());
		Key lastSubKey = null;
		// pm.currentTransaction().begin();

		// test user presence
		JID jid = new JID(user.getId());
		Presence presence = TalkService.getPresence(jid);

		Date newDate = null; // new date for subscription
		// Date nextUserDate = null; //new date to update user

		if (presence != null && presence.isAvailable()) {
			// test last update
			if (user.getLastUpdate() != null) {
				
				Date nowIsNow = Calendar.getInstance().getTime();
				
				Calendar nextCal = Calendar.getInstance();
				nextCal.setTime(user.getLastUpdate());
				nextCal.add(Calendar.MINUTE, user.getInterval());
				Date nextDate = nextCal.getTime();

				
				// interval not ok
				if (nextDate.after(nowIsNow)) {
					// gimme a break !
					return;
				}
			}

			// user here ! send entry available
			newDate = this.notifySubscriptionAndReturnLastDate(pm, sub, jid); 
			
			//new date is next date on next post
			if (newDate != null)
				lastSubKey = (sub.getKey());
			else
				lastSubKey = null;

		} else {
			// user not present : manage sub regarding priority
			if (sub.getPriority() < 0) {
				// purge all notification
				newDate = null; // new Date();
			}
		}

		pm.currentTransaction().begin();

		// flag indicates if all entries has been sent for this subscription
		if (newDate != null)
			sub.setLastDate(newDate);
		//last process date on this subscription
		sub.setLastProcessDate(new Date());

		pm.currentTransaction().commit();

		
		//new update
		if (lastSubKey != null) {
			pm.currentTransaction().begin();
			
			user.setLastSubscriptionKey(lastSubKey); // set last subscription
														// key
			user.setLastUpdate(new Date()); // last update, for interval manager
			
			pm.currentTransaction().commit();
		}

		
		pm.close();
	}

	public boolean removeSubscription(long id) {

		DataManager dm = DataManagerFactory.getInstance();
		PersistenceManager pm = dm.newPersistenceManager();

		Subscription s = pm.getObjectById(Subscription.class, new Long(id));
		pm.currentTransaction().begin();
		pm.deletePersistent(s);
		pm.currentTransaction().commit();
		pm.flush();

		pm.close();

		return true;
	}

	public boolean removeSubscription(String email, long blogId) {
		DataManager dm = DataManagerFactory.getInstance();
		PersistenceManager pm = dm.newPersistenceManager();

		Query qUser = pm.newQuery(User.class);
		qUser.setFilter("id == email");
		qUser.declareParameters("String email");
		qUser.setRange(0, 1);
		qUser.setUnique(true);

		User user = (User) qUser.execute(email);

		if (user == null) {
			qUser.closeAll();
			return false;
		}

		Blog blog = pm.getObjectById(Blog.class, new Long(blogId));

		Query q = pm.newQuery(Subscription.class);
		q.setFilter("userKey == uid && blogKey == bid");
		q.declareParameters("com.google.appengine.api.datastore.Key uid, com.google.appengine.api.datastore.Key bid");
		q.setRange(0, 1);

		@SuppressWarnings("unchecked")
		List<Subscription> list = (List<Subscription>) q.execute(user.getKey(),
				blog.getKey());

		if (list.size() > 0) {
			pm.currentTransaction().begin();
			pm.deletePersistent(list.get(0));
			pm.currentTransaction().commit();
			pm.flush();

		}
		q.closeAll();
		pm.close();

		return true;
	}

	/**
	 * Notify new BlogEntry to user and get new "last blogEntry date"
	 * 
	 * @param pm
	 * @param sub
	 * @param jabberId
	 * @return
	 */
	private Date notifySubscriptionAndReturnLastDate(PersistenceManager pm,
			Subscription sub, JID jabberId) {

		Date newDate = null;

		// find first
		Query q = pm.newQuery(BlogEntry.class);
		q.setFilter("blogKey == blog && pubDate > date");
		q.setOrdering("pubDate");
		q.declareParameters("com.google.appengine.api.datastore.Key blog, java.util.Date date");
		q.setUnique(true);
		q.setRange(0, 1);

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

			// find blog
			String title = getBlogTitle(pm, entryToPush);

			//fetch link
			String link = entryToPush.getShortLink();
			
			if (link == null){
				link = getUrlShorten().shorten(entryToPush.getLink());
			} 
					
					
			StringBuilder sb = new StringBuilder();
			sb.append('[');
			sb.append(title);
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
	 * Fetch blog title
	 * 
	 * @param pm
	 * @param k
	 * @return
	 */
	private String getBlogTitle(PersistenceManager pm, BlogEntry be) {

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

	/**
	 * Get url shorten
	 * 
	 * @return
	 */
	private UrlShorten getUrlShorten() {
		if (urlShorten == null)
			this.urlShorten = UrlShortenFactory.getInstance();
		return this.urlShorten;
	}

}
