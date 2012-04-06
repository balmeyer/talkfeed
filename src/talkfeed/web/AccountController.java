package talkfeed.web;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import talkfeed.SubscriptionService;
import talkfeed.data.Blog;
import talkfeed.data.BlogEntry;
import talkfeed.data.DataManager;
import talkfeed.data.DataManagerFactory;
import talkfeed.data.Subscription;
import talkfeed.gtalk.TalkService;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.appengine.api.xmpp.JID;

/**
 * controller for web application
 * @author JBVovau
 *
 */
@Controller
public class AccountController {

	@RequestMapping(value = "/index.htm", method = RequestMethod.GET)
	public String index(){
		return "page/home";
	}
	
	/**
	 * Connect to Google Account
	 * 
	 * @param req
	 * @param resp
	 * @return
	 * @throws IOException
	 */
	@RequestMapping(value = "/connect.*", method = RequestMethod.GET)
	public String doConnexion(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {

		UserService userService = UserServiceFactory.getUserService();
		User user = userService.getCurrentUser();

		if (user == null) {
			resp.sendRedirect(userService.createLoginURL("/account.htm"));
			return null;
		}

		// redirection page d'accueil
		return "redirect:/account.htm";
	}

	/**
	 * 
	 * @param req
	 * @param resp
	 * @return
	 * @throws IOException
	 */
	@RequestMapping(value = "/disconnect.*", method = RequestMethod.GET)
	public String deconnexion(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		UserService userService = UserServiceFactory.getUserService();

		resp.sendRedirect(userService.createLogoutURL("/index.htm"));

		return null;

	}

	@RequestMapping(value = "/account.*", method = RequestMethod.GET)
	public ModelAndView index(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {

		ModelAndView mav = new ModelAndView("page/account");
		UserService userService = UserServiceFactory.getUserService();
		User user = userService.getCurrentUser();

		if (user == null) {
			resp.sendRedirect(userService.createLoginURL("/account.htm"));
			return null;
		}

		// load subscriptions
		DataManager dm = DataManagerFactory.getInstance();
		PersistenceManager pm = dm.newPersistenceManager();

		Query q = pm.newQuery(talkfeed.data.User.class);
		q.setFilter("id == param");
		q.declareParameters("String param");
		q.setRange(0, 1);

		@SuppressWarnings("unchecked")
		List<talkfeed.data.User> users = (List<talkfeed.data.User>) q
				.execute(user.getEmail());

		if (users.size() > 0) {
			// lkey from user
			Key key = users.get(0).getKey();

			// list subscription
			Query qSub = pm.newQuery(Subscription.class);
			qSub.setFilter("userKey == bk");
			qSub.declareParameters("com.google.appengine.api.datastore.Key bk");
			
			List<Blog> blogs = new ArrayList<Blog>();
			
			@SuppressWarnings("unchecked")
			List<Subscription> subs = (List<Subscription>) qSub.execute(key);
			
			for (Subscription sub : subs){
				try {
				Blog b = pm.getObjectById(Blog.class, sub.getBlogKey());
				
				blogs.add(b);
				
				} catch (JDOObjectNotFoundException ex){
					//blog deleted ! bad !!
					pm.currentTransaction().begin();
					pm.deletePersistent(sub);
					pm.currentTransaction().commit();
					pm.flush();
					continue;
				}
			}
			mav.getModel().put("blogs", blogs);

		}

		// Collection<Subscription> list=

		pm.close();
		
		return mav;
	}

	
	@RequestMapping(value = "/ajax/unsubscribe.*", method = RequestMethod.GET)
	public void ajaxUnsubscribe(HttpServletRequest req, HttpServletResponse resp, long id) throws Exception{
		System.out.println(id);
		
		UserService userService = UserServiceFactory.getUserService();
		User user = userService.getCurrentUser();
		
		if (user != null){
		
		SubscriptionService serv = new SubscriptionService();
		serv.removeSubscription(user.getEmail() , id);
		resp.getWriter().write("OK");
		
		} else {
			
			throw new Exception("no user");
		}
	}
	
	@RequestMapping(value = "/ajax/posts.*", method = RequestMethod.GET)
	public ModelAndView ajaxLastEntries(long blogId){
		DataManager dm = DataManagerFactory.getInstance();
		PersistenceManager pm = dm.newPersistenceManager();
		
		//select subscriptions 
		Query q = pm.newQuery(BlogEntry.class);
		q.setOrdering("pubDate desc");
		q.setRange(0, 15);
		
		q.setFilter("blogKey == bk");
		q.declareParameters("com.google.appengine.api.datastore.Key bk");
		
		List<BlogEntry> list = new ArrayList<BlogEntry>(15);
		
		
		Blog blog = pm.getObjectById(Blog.class,blogId);
		
		@SuppressWarnings("unchecked")
		Collection<BlogEntry> col = (Collection<BlogEntry>) q.execute(blog.getKey());
		
		for(BlogEntry be : col){
			list.add(be);
		}
		
		pm.close();
		
		ModelAndView mav = new ModelAndView("ajax/posts");
		mav.getModel().put("posts", list);
		
		return mav;
	}
	
	@RequestMapping(value = "/ajax/invite.*", method = RequestMethod.GET)
	public String ajaxInvite() throws Exception{
		
		UserService userService = UserServiceFactory.getUserService();
		User user = userService.getCurrentUser();
		
		if (user != null){
		
		JID jid = new JID(user.getEmail());
		TalkService.invite(jid);
		
		return "ajax/invited";
		
		} else {
			
			throw new Exception("no user");
		}

	}
	
}
