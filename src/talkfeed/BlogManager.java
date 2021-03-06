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
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import talkfeed.QueuedTask.TaskType;
import talkfeed.cache.BlogCache;
import talkfeed.cache.SubscriptionCache;
import talkfeed.cache.UserPresence;
import talkfeed.data.Blog;
import talkfeed.data.BlogEntry;
import talkfeed.data.DataManager;
import talkfeed.data.DataManagerFactory;
import talkfeed.data.Subscription;
import talkfeed.feed.Channel;
import talkfeed.feed.FeedItem;
import talkfeed.feed.FeedManager;
import talkfeed.utils.DocumentLoader;
import talkfeed.utils.TextTools;

/**
 * Managing web sources updates.
 * 
 * @author JBVovau
 * 
 */
public final class BlogManager {

	private static final int MIN_INTERVAL = 60 ; //1 hour
	private static final int MAX_INTERVAL = (60 * 4); //4 days
	
	/**
	 * Get instance of BlogManager
	 * @return
	 */
	public static BlogManager getInstance() {
		return new BlogManager();
	}

	/** No public instance */
	private BlogManager() {
	}

	/**
	 * get or create web source from given link (could be RSS, Atom feed or URL)
	 */
	public Blog getOrCreateSource(String link) {
		
		Date now = Calendar.getInstance().getTime();
		
		//long time 
		Calendar calLongTime = Calendar.getInstance();
		calLongTime.set(Calendar.YEAR, 1900);
		
		//prepare link
		link = TextTools.purgeLink(link);
		
		DataManager dm = DataManagerFactory.getInstance();
		PersistenceManager pm = dm.newPersistenceManager();
		
		//find if blog already exists in database
		Blog blog = dm.getBlogFromLink(pm , link);

		if (blog == null) {
			//blog does not exist : load content to parse it
			String content = DocumentLoader.loadPage(link);
 
			String rss = null; 
			
			//test if given link is already RSS or Atom FEED
			if (FeedManager.isFeed(content)){
				rss = link;
			}else {
				//extract RSS link from page content
				rss = TextTools.extractRssFromPage(content);
			}

			//test if rss information hase been found
			if (rss != null){
				//check again if blog exists with given link
				blog = dm.getBlogFromLink(pm , rss);
				//actually create new blog in database
				if (blog == null){
					blog = new Blog();
					blog.setLatestEntry(calLongTime.getTime());
					blog.setNextUpdate(now);
					blog.setLink(link);
					blog.setRss(rss);
					pm.currentTransaction().begin();
					pm.makePersistent(blog);
					pm.currentTransaction().commit();
				}
			} else {
				Logger.getLogger("updateBlog").log(Level.WARNING,
						"blog not found : "  + link);
			}
		}
		
		pm.close();
		pm = null;

		return blog;
	}


	/**
	 * Update feed's blogs.
	 */
	@Deprecated
	public void updateBlogs(int nbMax){
		if (nbMax <=0) return;
		
		DataManager dm = DataManagerFactory.getInstance();
		PersistenceManager pm = dm.newPersistenceManager();
		
		//find blogs where "nextUpdate" is before now, ordered by "nextUpdate" date
		Query q = pm.newQuery(Blog.class);
		q.setRange(0, nbMax);
		q.setFilter("nextUpdate <= date");
		q.declareParameters("java.util.Date date");
		q.setOrdering("nextUpdate");
		
		Date now = Calendar.getInstance().getTime();
		
		@SuppressWarnings("unchecked")
		List<Blog> blogs = (List<Blog>) q.execute(now);
		
		for(Blog blog : blogs){
			//add treatment to queue
			QueuedTask task = new QueuedTask();
			task.setType(TaskType.updateblog);
			task.addParam("id", blog.getKey().getId());
			QueuedTask.enqueue(task);
			
		}
		
		q.closeAll();
		pm.close();
	}
	
	/**
	 * Update blog only when user is present
	 * @param max
	 */
	public void updateActiveBlogs(int max){
		
		//fetch list from Cache
		List<Long> blogToUpdate = BlogCache.getActiveBlogsToUpdate(max);
		
		//to update
		for(Long id : blogToUpdate){
			//add treatment to queue
			QueuedTask task = new QueuedTask();
			task.setType(TaskType.updateblog);
			task.addParam("id", id);
			QueuedTask.enqueue(task);
			
		}
	}
	
	/**
	 * Update single blog (for queued task usage)
	 * @param id
	 */
	public void updateBlog(long id){
		
		DataManager dm = DataManagerFactory.getInstance();
		PersistenceManager pm = dm.newPersistenceManager();
		
		//find blog
		Blog blog = pm.getObjectById(Blog.class,id);
		
		//check if blog has any updates
		BlogUpdateResult result = null;
		
		//fetch channel on the web via RSS
		Channel chan = FeedManager.loadRss(blog.getRss());
		
		if (chan != null){
			//update blog. Returns true if new items were added
			result = updateBlog(pm, blog, chan);
			
			//test url and title and correct if worng
			if (chan.getLink() != null && !chan.getLink().equalsIgnoreCase(blog.getLink())){
				blog.setLink(chan.getLink());
			}
			
			if (chan.getTitle() != null && !chan.getTitle().equalsIgnoreCase(blog.getTitle())){
				blog.setTitle(chan.getTitle());
			}
		} 
		
		//Set last post entry date to blog
		if (result != null && result.isUpdate()) 
			blog.setLatestEntry(result.getLastestEntryDate());
		
		String messageLog = "";
		
		//build nextUpdate
		//if no new update : increase interval
		int newInterval = blog.getRefreshInterval();
		if (result != null && result.isUpdate()){
			newInterval = newInterval / 2;
			messageLog = "Blog updated [" + blog.getTitle() + "]"; 
		} else {
			newInterval = newInterval * 2;
			messageLog = "No new update [" + blog.getTitle() + "]"; 
		}
		
		if (newInterval <= MIN_INTERVAL) newInterval = MIN_INTERVAL; 
		if (newInterval > MAX_INTERVAL) newInterval = MAX_INTERVAL;
		
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.MINUTE, newInterval);
		
		blog.setRefreshInterval(newInterval);
		blog.setNextUpdate(cal.getTime());
		
		pm.flush();
		pm.close();
		
		BlogCache.setNextUpdate(blog.getKey().getId(), newInterval);
		
		Logger.getLogger("updateBlog").log(Level.INFO,
				messageLog);
	}
	
	/**
	 * Active blogs regarding users presence.
	 */
	public void activeBlogsFromUserPresence(){
		
		int nb = 0;
		
		try {
			Collection<String> users = UserPresence.listUsers();
			
			//for each present user : fetch subscriptions and active blog
			for(String user : users){
				//fetch subscriptions
				Collection<Long> blogs = SubscriptionCache.getUserBlogs(user);
				for(long id : blogs){
					BlogCache.setBlogIsActive(id);
					nb++;
				}
			}
			
		}  finally {
			//release datastore
			SubscriptionCache.releaseDataStore();
		}
		
		Logger.getLogger("UserService").info(
				nb +  " blog(s) active");
		
	}
	
	/**
	 * Remove all blogs without subscription
	 * @return
	 */
	public int removeBlogWithoutSubscription(){
		int nb = 0;
		
		DataManager dm = DataManagerFactory.getInstance();
		
		PersistenceManager pm = dm.newPersistenceManager();
		
		Query q = pm.newQuery(Blog.class);
		
		//fetch ALL blogs
		@SuppressWarnings("unchecked")
		List<Blog> allBlogs = (List<Blog>) q.execute();
		
		for(Blog b : allBlogs){
			//fetch sub
			Query sq = pm.newQuery(Subscription.class);
			sq.setFilter("blogKey == bk");
			sq.declareParameters("com.google.appengine.api.datastore.Key bk");
			sq.setRange(0, 1);
			
			@SuppressWarnings("unchecked")
			List<Subscription> ls = (List<Subscription>) sq.execute(b.getKey());
			
			if (ls.size() == 0){
				//delete blog without subscription
				pm.currentTransaction().begin();
				pm.deletePersistent(b);
				pm.currentTransaction().commit();
				nb++;
			}
		}
		
		pm.close();
		
		return nb;
	}
	
	
	/**
	 * Remove old entries
	 * @param nbdays
	 * @return
	 */
	public int removeOldestEntries(int nbdays){
		int nb = 0;
		
		DataManager dm = DataManagerFactory.getInstance();
		
		PersistenceManager pm = dm.newPersistenceManager();
		
		Query q = pm.newQuery(BlogEntry.class);
		q.setFilter("creaDate < date");
		q.setRange(0,50);
		q.declareParameters("java.util.Date date");
		
		Calendar cdat = Calendar.getInstance();
		cdat.add(Calendar.DATE , -nbdays);
		
		//fetch ALL blogs
		@SuppressWarnings("unchecked")
		List<BlogEntry> oldest = (List<BlogEntry>) q.execute(cdat.getTime());
		
		for(BlogEntry be : oldest){
			pm.currentTransaction().begin();
			pm.deletePersistent(be);
			pm.currentTransaction().commit();
			nb++;
		}

		pm.close();
		
		return nb;
	}
	
	/**
	 * update entries for a blog with given channel
	 * @param blog
	 * @param chan
	 * @return True if any change has occurred (new items found)
	 */
	private BlogUpdateResult updateBlog(PersistenceManager pm ,Blog blog, Channel chan){
		
		BlogUpdateResult result = new BlogUpdateResult();
		
		Date recentDate = blog.getLatestEntry();

		//if most recent entry found, pubDate becomes its date
		if (recentDate == null){
			//if no most recent entry, pubDate is minimum
			Calendar c = Calendar.getInstance();
			c.set(Calendar.YEAR,1900);
			recentDate = c.getTime();
		}
		
		//query to find blog entry by GUID
		Query queryBlogEntry = pm.newQuery(BlogEntry.class);
		queryBlogEntry.setFilter("guid == g");
		queryBlogEntry.declareParameters("java.lang.String g");
		queryBlogEntry.setUnique(true);
		
		//compare item
		for(FeedItem item : chan.items()){
			
			//parse error
			if (item.getLink() == null) continue;
			
			//set creaDate
			Date pubDate = item.getPubDate();
			if (pubDate == null) pubDate = Calendar.getInstance().getTime();
			
			//date
			if(recentDate.after(pubDate)) continue;

			BlogEntry entry = (BlogEntry) queryBlogEntry.execute(item.getGuid());
			
			if (entry == null){
				//new item
				entry = new BlogEntry();
				entry.setBlogKey(blog.getKey());
				entry.setCreaDate(new Date());
				entry.setPubDate(pubDate);
				entry.setGuid(item.getGuid());
				entry.setLink(TextTools.limitText(item.getLink(),500));
				entry.setTitle(TextTools.limitText(item.getTitle(),500));
				entry.setBlogTitle(TextTools.limitText(chan.getTitle(),500));
				pm.makePersistent(entry);
				result.setUpdate(true);
				result.checkEntryDate(pubDate);
			}

			queryBlogEntry.close(entry);
		}
		
		queryBlogEntry.closeAll();
		
		return result;
	}
	
	
	/**
	 * Result of a blog update
	 * @author JB Balmeyer
	 *
	 */
	private class BlogUpdateResult{
		/**
		 * are new entries found ?
		 */
		private boolean hasUpdate ;
		/**
		 * The date of the latest entry
		 */
		private Date lastestEntryDate;
		
		public boolean isUpdate() {
			return hasUpdate;
		}
		public void setUpdate(boolean hasUpdate) {
			this.hasUpdate = hasUpdate;
		}
		
		/**
		 * Returns the latest entry date
		 * @return
		 */
		public Date getLastestEntryDate() {
			return lastestEntryDate;
		}
		
		/**
		 * check a new date
		 * @param lastestEntryDate
		 */
		public void checkEntryDate(Date newEntryDate) {
			
			if (this.lastestEntryDate == null) this.lastestEntryDate = newEntryDate;
			else {
				if (this.lastestEntryDate.before(newEntryDate)) {
					this.lastestEntryDate = newEntryDate;
				}
			}
		}
	
	}
	
}
