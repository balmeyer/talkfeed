package talkfeed.api;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import talkfeed.BlogManager;
import talkfeed.cache.BlogCache;
import talkfeed.cache.SubscriptionCache;
import talkfeed.data.Blog;
import talkfeed.data.DataManager;
import talkfeed.data.DataManagerFactory;
import talkfeed.data.Subscription;
import talkfeed.data.User;
import talkfeed.utils.TextTools;

import com.google.appengine.api.datastore.Key;

/**
 * Manage API
 * @author balmeyer
 *
 */
public class ApiManager {

	private String user;
	
	private Key userKey;
	
	private PersistenceManager pm;
	
	
	public ApiManager(String user) {
		this.user = user;
	}
	
	
	
	/**
	 * Close object
	 */
	public void close(){
		if (pm != null){
			pm.close();
			pm = null;
		}
	}
	
	public Object request(String path){
		if (path == null) return null;
		
		if (path.equals("userget")){
			return this.getOrCreateUser();
		}
		
		if (path.equals("bloglist")){
			return this.listBlogs();
		}
		
		return null;
	}
	
	/**
	 * Add or create user
	 * @param email
	 */
	public User getOrCreateUser(){

		DataManager dm = DataManagerFactory.getInstance();

		//check is user exists, if not, create it
		//clean email
		String jid = TextTools.cleanJID(this.user);

		User user = dm.getUserFromId(getPersitence(), jid);
		if (user == null){
			user = new User();
			user.setId(jid);
			user.setDateCrea(new Date());
			user.setNextUpdate(new Date());
			getPersitence().currentTransaction().begin();
			getPersitence().makePersistent(user);
			getPersitence().currentTransaction().commit();
		}
		
		return user;
	}
	
	/**
	 * Return blogs for user
	 * @return
	 */
	public List<Blog> listBlogs(){
		
		Collection<Long> idBlogs = SubscriptionCache.getUserBlogs(this.user);

		List<Blog> blogs = new ArrayList<Blog>();
		for(Long id : idBlogs){
			blogs.add(BlogCache.getBlog(id));
		}
		
		return blogs;
	}
	
	/**
	 * Add a blog
	 * @param link
	 * @return
	 */
	public Blog blogAdd(String link){
		//TODO add blog without checking the page
		BlogManager bm = BlogManager.getInstance();
		
		return bm.getOrCreateSource(link);
	}
	
	/**
	 * Return user key
	 * @return
	 */
	private Key getUserKey(){
		if (this.userKey == null){
			Query q = getPersitence().newQuery(User.class);
			q.setFilter("id == email");
			q.declareParameters("String email");
			q.setUnique(true);
			
			User u = (User) q.execute(this.user);
			this.userKey = u.getKey();
			
		}
		
		return this.userKey;
	}
	
	/**
	 * Return persistence
	 * @return
	 */
	private PersistenceManager getPersitence(){
		if (pm == null){
			pm = DataManagerFactory.getInstance().newPersistenceManager();
		}
		return pm;
	}
	
}
