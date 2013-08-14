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

package talkfeed.data;

import java.util.List;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.Query;

/**
 * DataManager implementation for Google AppEngine
 * @author JBVovau
 *
 */
public class StoreDataManager implements DataManager{

	private static final PersistenceManagerFactory pmFactory = JDOHelper.getPersistenceManagerFactory("datamanager");
	
	@Override
	public User getUserFromId(PersistenceManager pm , String id){
		User user = null;

		Query q = pm.newQuery(User.class);
		q.setFilter("id == jid");
		q.declareParameters("java.lang.String jid");
		
		@SuppressWarnings("unchecked")
		List<User> list = (List<User>) q.execute(id);
		
		if (list.size() > 0){
			user = list.get(0);
		}
		q.closeAll();
		
		
		return user;
	}
	
	@Override
	public Subscription getSubscription(PersistenceManager pm, User user, Blog blog){
		
		Subscription sub = null;

		Query q = pm.newQuery(Subscription.class);
		
		q.setFilter("userKey == uk && blogKey == bk");
		q.declareParameters("com.google.appengine.api.datastore.Key uk , "
				+ "com.google.appengine.api.datastore.Key bk");

		@SuppressWarnings("unchecked")
		List<Subscription> list = (List<Subscription>) q.execute(user.getKey(), blog.getKey());
		
		if (list.size() > 0){
			sub = list.get(0);
		}
		q.closeAll();
		q = null;
		
		return sub;
	}
	
	/**
	 * Get a stored Blog from his link (could be direct url or RSS url)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Blog getBlogFromLink(PersistenceManager pm , String link){
		Blog blog = null;

		//try direct link
		Query q = pm.newQuery(Blog.class);
		q.setFilter("link == '" + link +"'");
		
		List<Blog> blogs = (List<Blog>) q.execute();
		
		if (blogs.size() > 0){
			blog = blogs.get(0);
		}
		
		//if blog not found, fetch blog by rss
		if (blog == null){
			q = pm.newQuery(Blog.class);
			q.setFilter("rss == '" + link +"'");
			
			 blogs = (List<Blog>) q.execute();
			if (blogs.size() > 0){
				blog = blogs.get(0);
			}
		}
		q.closeAll();
		q = null; 
		
		return blog;
	}
	

	
	@Override
	public PersistenceManager newPersistenceManager(){
		return this.createPersistenceManager();
	}
	
	/**
	 * Create persistence manager
	 * @return
	 */
	private PersistenceManager createPersistenceManager(){
		PersistenceManager pm = pmFactory.getPersistenceManager();
		return pm;
	}
}
