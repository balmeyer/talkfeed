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
import java.util.List;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import talkfeed.blog.Channel;
import talkfeed.blog.FeedItem;
import talkfeed.blog.FeedManager;
import talkfeed.data.Blog;
import talkfeed.data.BlogEntry;
import talkfeed.data.DataManager;
import talkfeed.data.DataManagerFactory;
import talkfeed.data.Subscription;
import talkfeed.data.User;
import talkfeed.utils.DocumentLoader;
import talkfeed.utils.TextTools;

import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.google.appengine.api.taskqueue.TaskOptions.Method;

/**
 * Managing web sources update.
 * 
 * @author JBVovau
 * 
 */
public class BlogService {

	/**
	 * Get instance of BlogManager
	 * @return
	 */
	public static BlogService getInstance() {
		return new BlogService();
	}

	/** No public instance */
	private BlogService() {
	}

	/**
	 * get or create web source from given link (could be RSS, atom or URL)
	 */
	public Blog getOrCreateSource(String link) {
		//arrange or prepare link
		link = TextTools.purgeLink(link);
		
		// check blog from link
		DataManager dm = DataManagerFactory.getInstance();
		Blog blog = dm.getBlogFromLink(link);

		if (blog == null) {
			//blog does not exist : load content to parse it
			String content = DocumentLoader.loadPage(link);
 
			String rss = null; 
			
			//test if given link is already RSS or Atom FEED
			if (FeedManager.isFeed(content)){
				rss = link;
			}else {
				//extract RSS link from blog content
				rss = TextTools.extractRssFromPage(content);
			}

			if (rss != null){
				//check blog again
				blog = dm.getBlogFromLink(rss);
				
				if (blog == null){
					blog = new Blog();
					blog.setLastUpdate(new Date());
					blog.setNextUpdate(new Date());
					blog.setLink(link);
					blog.setRss(rss);
					dm.save(blog);
				}
			}
		}

		return blog;
	}

	/**
	 * Purge link
	 * 
	 * @param link
	 * @return
	 */


	/**
	 * Update feed's blogs.
	 */
	public void updateBlogs(int nbMax){
		if (nbMax <=0) return;
		
		DataManager dm = DataManagerFactory.getInstance();
		
		PersistenceManager pm = dm.newPersistenceManager();
		
		//find blogs, ordered by "nextUpdate" date
		Query q = pm.newQuery(Blog.class);
		q.setRange(0, nbMax);
		q.setFilter("nextUpdate <= date");
		q.declareParameters("java.util.Date date");
		q.setOrdering("nextUpdate");
		
		Date now = Calendar.getInstance().getTime();
		
		@SuppressWarnings("unchecked")
		List<Blog> blogs = (List<Blog>) q.execute(now);
		
		for(Blog blog : blogs){
			//find queue
			Queue queue = QueueFactory.getDefaultQueue();
			
			
			TaskOptions options = withUrl("/tasks/updateblog")
			.method(Method.GET)
			.param("id", String.valueOf(blog.getKey().getId()));
			
			//add to GAE Queue
			queue.add(options);
		}
		
		
		pm.close();
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
		boolean hasUpdate = false;
		
		//fetch channel on the web via RSS
		Channel chan = FeedManager.loadRss(blog.getRss());
		
		if (chan != null){
			//update blog. Returns true if new items were added
			hasUpdate = updateBlog(pm, blog, chan);
			
			//test url and title and correct if worng
			if (chan.getLink() != null && !chan.getLink().equalsIgnoreCase(blog.getLink())){
				blog.setLink(chan.getLink());
			}
			
			if (chan.getTitle() != null && !chan.getTitle().equalsIgnoreCase(blog.getTitle())){
				blog.setTitle(chan.getTitle());
			}
		} 
		
		//set update date
		if (hasUpdate) blog.setLastUpdate(new Date());
		
		//build nextUpdate
		//if no new update : increase interval
		int newInterval = blog.getRefreshInterval();
		if (!hasUpdate){
			newInterval = newInterval * 2;
		} else {
			newInterval = newInterval / 2;
		}
		if (newInterval <= 120) newInterval = 120; //2 hours
		if (newInterval > (24 * 60 * 3)) newInterval = 24 * 60 * 3; //max : 3 days
		
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.MINUTE, newInterval);
		
		blog.setRefreshInterval(newInterval);
		blog.setNextUpdate(cal.getTime());
		
		pm.flush();
		pm.close();
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
	 * @return True if any change has occured (new items found)
	 */
	private boolean updateBlog(PersistenceManager pm ,Blog blog, Channel chan){
		
		
		boolean newUpdates = false;
		
		Date recentDate = null;
		
		//find max entry
		Query queryMostRecentEntry = pm.newQuery(BlogEntry.class);
		queryMostRecentEntry.setFilter("blogKey == bk");
		queryMostRecentEntry.declareParameters("com.google.appengine.api.datastore.Key bk");
		queryMostRecentEntry.setOrdering("pubDate desc");
		queryMostRecentEntry.setRange(0, 1);
		
		//fetch most recent entry by pubDate to test "pubDate"
		@SuppressWarnings("unchecked")
		List<BlogEntry> listMostRecentEntry = (List<BlogEntry>) queryMostRecentEntry.execute(blog.getKey());
		BlogEntry mostRecentEntry = null;
		if (listMostRecentEntry.size() > 0) mostRecentEntry = listMostRecentEntry.get(0);
		
		if (mostRecentEntry != null) {
			recentDate = mostRecentEntry.getPubDate();
		} else {
			Calendar c = Calendar.getInstance();
			c.set(Calendar.YEAR,1900);
			recentDate = c.getTime();
		}
		
		//compare item
		for(FeedItem item : chan.itemsOrderByDate()){
			
			//parse error
			if (item.getLink() == null) continue;
			
			//set creaDate
			Date pubDate = item.getPubDate();
			if (pubDate == null) pubDate = Calendar.getInstance().getTime();
			
			//date
			if(recentDate.after(pubDate)) continue;
			
			//find by guid
			Query queryBlogEntry = pm.newQuery(BlogEntry.class);
			queryBlogEntry.setFilter("guid == g");
			queryBlogEntry.declareParameters("java.lang.String g");
			queryBlogEntry.setUnique(true);
			
			BlogEntry entry = (BlogEntry) queryBlogEntry.execute(item.getGuid());
			
			if (entry == null){
				//new item
				entry = new BlogEntry();
				entry.setBlogKey(blog.getKey());
				entry.setCreaDate(new Date());
				entry.setPubDate(pubDate);
				entry.setGuid(item.getGuid());
				entry.setLink(item.getLink());
				entry.setTitle(item.getTitle());
				entry.setBlogTitle(chan.getTitle());
				pm.makePersistent(entry);
				newUpdates = true;
			}
			
		}
		
		return newUpdates;
	}
	
}
