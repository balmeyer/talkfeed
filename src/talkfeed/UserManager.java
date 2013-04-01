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

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import talkfeed.QueuedTask.TaskType;
import talkfeed.data.Blog;
import talkfeed.data.BlogEntry;
import talkfeed.data.DataManager;
import talkfeed.data.DataManagerFactory;
import talkfeed.data.Subscription;
import talkfeed.data.User;
import talkfeed.gtalk.GTalkBlogNotification;
import talkfeed.gtalk.TalkService;
import talkfeed.url.UrlShortenFactory;
import talkfeed.utils.CacheService;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.xmpp.JID;
import com.google.appengine.api.xmpp.Presence;

/**
 * Send subscription via user
 * 
 * @author vovau
 * 
 */
public class UserManager {

	private static final int NB_SUBSCRIPTIONS_MAX = 20;

	private PersistenceManager currentManager;

	
	public void setPresence(String id , boolean presence){
		if (id == null) return;
		
		//chargement user
		DataManager dm = DataManagerFactory.getInstance();
		PersistenceManager pm = dm.newPersistenceManager();

		Query qUser = pm.newQuery(User.class);
		qUser.setFilter("id == email");
		qUser.declareParameters("String email");
		qUser.setRange(0, 1);
		qUser.setUnique(true);

		User user = (User) qUser.execute(id);

		if (user == null) {
			qUser.closeAll();
			return ;
		}
		
		user.setPresence(presence);
		pm.currentTransaction().begin();
		pm.flush();
		pm.currentTransaction().commit();
		
	}
	
	@SuppressWarnings("unchecked")
	public void updateUsers(int nbMax) {

		Date now = Calendar.getInstance().getTime();

		PersistenceManager pm = DataManagerFactory.getInstance()
				.newPersistenceManager();

		// TMP transition
		// TODO remove when ok
		/*
		 * data correction for version 0.6.1 Query qall =
		 * pm.newQuery(User.class); List<User> all = (List<User>)
		 * qall.execute(); for(User us : all){ if (us.getNextUpdate() == null) {
		 * us.setNextUpdate(now); pm.currentTransaction().begin(); pm.flush();
		 * pm.currentTransaction().commit(); }
		 * 
		 * }
		 */

		// find user
		Query q = pm.newQuery(User.class);
		q.setFilter("nextUpdate <= next");
		q.setFilter("paused == false");
		q.setOrdering("nextUpdate");
		q.declareParameters("java.util.Date next");
		q.setRange(0, nbMax);

		// list user
		List<User> list = (List<User>) q.execute(now);

		for (User user : list) {
			//build task for queuing
			QueuedTask task = new QueuedTask();
			task.setType(TaskType.updateuser);
			task.addParam("id", user.getKey().getId());
			QueuedTask.enqueue(task);


		}

		// end of process
		q.closeAll();
		pm.close();

	}

	/**
	 * Send notification to user, if connected
	 * 
	 * @param id
	 */
	public void updateUser(long id) {
		// Nowadays
		Date now = Calendar.getInstance().getTime();

		// create new persistenceManager
		this.currentManager = DataManagerFactory.getInstance()
				.newPersistenceManager();

		//fetch user from his jabber id
		User user = (User) this.currentManager.getObjectById(User.class, id);

		// next update
		int minuteNextUpdate = user.getInterval();
		if (minuteNextUpdate < 10)
			minuteNextUpdate = 10;

		// test user presence
		JID jid = new JID(user.getId());
		Presence presence = TalkService.getPresence(jid);

		if (presence != null && presence.isAvailable()) {
			//user is present : do update !

			// select subscriptions
			Query q = this.currentManager.newQuery(Subscription.class);
			q.setOrdering("lastProcessDate");
			q.setRange(0, NB_SUBSCRIPTIONS_MAX);
			q.setFilter("userKey == uk");
			q.declareParameters("com.google.appengine.api.datastore.Key uk");

			@SuppressWarnings("unchecked")
			List<Subscription> subs = (List<Subscription>) q.execute(user
					.getKey());

			// update
			for (Subscription sub : subs) {
				// update is done ?
				boolean updateDone = false;

				// fetch blog
				Blog blog = (Blog) this.currentManager.getObjectById(
						Blog.class, sub.getBlogKey());

				// compare dates
				if (blog.getLatestEntryDate().after(sub.getLatestEntryNotifiedDate())) {
					Logger.getLogger("UserService").info(
							"user " + user.getId() + " present. Try notify : "
									+ blog.getTitle());

					// find next entry
					BlogEntry nextEntry = this.findNextEntry(sub);

					if (nextEntry == null) {
						// subscription is up to date
					} else {
						// notify user
						this.sendBlogEntry(jid, nextEntry);
						// set modification is done
						updateDone = true;
						// set current subscription mark to entry date
						sub.setLatestEntryNotifiedDate(nextEntry.getPubDate());
					}
				}

				// Update subscription process
				// last analyze date
				sub.setLastProcessDate(now);
				this.currentManager.currentTransaction().begin();
				this.currentManager.flush();
				this.currentManager.currentTransaction().commit();

				// break
				if (updateDone) {
					// update user
					user.setLastUpdate(now);
					user.setLastSubscriptionKey(sub.getKey());
					this.currentManager.currentTransaction().begin();
					this.currentManager.flush();
					this.currentManager.currentTransaction().commit();
					break;
				}

			}

			q.closeAll();

		} else {
			minuteNextUpdate = 20;
			Logger.getLogger("UserService").info(
					"user " + user.getId() + " not present");
		}

		// next update
		// record next update
		Calendar nextTime = Calendar.getInstance();
		nextTime.add(Calendar.MINUTE, minuteNextUpdate);
		user.setNextUpdate(nextTime.getTime());

		// flush
		this.currentManager.currentTransaction().begin();
		this.currentManager.flush();
		this.currentManager.currentTransaction().commit();

		this.currentManager.close();
		this.currentManager = null;
	}


	/**
	 * Remove a subscription
	 * 
	 * @param id
	 * @return
	 */
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
	


	/**
	 * Remove subscription
	 * 
	 * @param email
	 * @param blogId
	 * @return
	 */
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
	 * Find the next entry for current subscription
	 * 
	 * @return
	 */
	private BlogEntry findNextEntry(Subscription sub) {
		checkArguments(this.currentManager != null,
				"current PersistenceManager is null");

		// find oldest entry from blog subscription which haven't been sent
		Query q = this.currentManager.newQuery(BlogEntry.class);
		q.setFilter("blogKey == blog && pubDate > date");
		q.setOrdering("pubDate");
		q.declareParameters("com.google.appengine.api.datastore.Key blog, java.util.Date date");
		q.setUnique(true);
		q.setRange(0, 1);

		// find blog entry
		BlogEntry entryToPush = (BlogEntry) q.execute(sub.getBlogKey(),
				sub.getLatestEntryNotifiedDate());

		q.closeAll();

		return entryToPush;
	}

	/**
	 * Notify new BlogEntry to user and get new "last blogEntry date" return
	 * null if no entry is needed
	 * 
	 * @param pm
	 * @param sub
	 * @param jabberId
	 * @return
	 */
	private void sendBlogEntry(JID jabberId, BlogEntry entry) {
		checkArguments(entry != null, "Entry must not be null");

		// fetch link
		String link = entry.getShortLink();

		if (link == null) {
			link = UrlShortenFactory.getInstance().shorten(entry.getLink());
		}

		String blogTitle = entry.getBlogTitle();
		// TODO remove this when production
		if (blogTitle == null)
			blogTitle = this.getBlogTitle(entry);

		// build notification
		GTalkBlogNotification notif = new GTalkBlogNotification();
		notif.setBlogTitle(blogTitle);
		notif.setJabberID(jabberId);
		notif.setPostTitle(entry.getTitle());
		notif.setPostUrl(link);
		// send notif
		TalkService.sendMessage(notif);

		Logger.getLogger("UserService").info(
				"New entry for " + jabberId.getId() + " : " + link + "["
						+ entry.getPubDate() + "]");

	}

	/**
	 * To ensure transition
	 * 
	 * @param pm
	 * @param be
	 * @return
	 */
	private String getBlogTitle(BlogEntry be) {
		checkArguments(this.currentManager != null,
				"current PersistenceManager must not be null");
		// TODO remove this

		if (be == null)
			return null;

		Key k = be.getBlogKey();
		String key = "title" + String.valueOf(k.getId());

		Object title = CacheService.get(key);

		if (title == null) {
			Blog b = this.currentManager.getObjectById(Blog.class, k);
			title = b.getTitle();
			if (title != null)
				CacheService.put(key, title);
			else
				return "";
		}

		return title.toString();
	}

	
	
	/**
	 * 
	 * @param expression
	 * @param message
	 */
	private void checkArguments(boolean expression, String message) {
		if (!expression) {
			throw new IllegalArgumentException(message);
		}
	}

}
