package talkfeed.web;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import talkfeed.SubscriptionService;
import talkfeed.data.Blog;
import talkfeed.data.BlogEntry;
import talkfeed.data.DataManager;
import talkfeed.data.DataManagerFactory;
import talkfeed.data.Subscription;
import talkfeed.gtalk.TalkService;
import talkfeed.utils.TextTools;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.appengine.api.xmpp.JID;

/**
 * controller for web application
 * 
 * @author JBVovau
 * 
 */
@Controller
public class AccountController {

	@RequestMapping(value = "/index.htm", method = RequestMethod.GET)
	public String index(HttpServletRequest req, HttpServletResponse resp) {

		if (req != null && req.getCookies() != null) {
			// if cookie exist, return via account
			for (Cookie c : req.getCookies()) {
				if (c.getName().equalsIgnoreCase("username")
						&& c.getValue() != null) {
					return "redirect:/account.htm";
				}
			}
		}

		return "page/home";
	}

	@RequestMapping(value = "/help.htm", method = RequestMethod.GET)
	private String help() {
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

		resp.addCookie(new Cookie("username", null));

		resp.sendRedirect(userService.createLogoutURL("/index.htm"));

		return null;

	}

	/**
	 * 
	 * @param req
	 * @param resp
	 * @return
	 * @throws IOException
	 */
	@RequestMapping(value = "/account.*", method = RequestMethod.GET)
	public ModelAndView account(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {

		ModelAndView mav = new ModelAndView("page/account");
		UserService userService = UserServiceFactory.getUserService();
		User googleUser = userService.getCurrentUser();

		if (googleUser == null) {
			resp.sendRedirect(userService.createLoginURL("/account.htm"));
			return null;
		}

		resp.addCookie(new Cookie("username", googleUser.getEmail()));

		DataManager dm = DataManagerFactory.getInstance();
		PersistenceManager pm = dm.newPersistenceManager();

		// fetch user
		Query qUser = pm.newQuery(talkfeed.data.User.class);
		qUser.setFilter("id == param");
		qUser.declareParameters("String param");
		qUser.setUnique(true);
		qUser.setRange(0, 1);
		talkfeed.data.User talkfeedUser = (talkfeed.data.User) qUser
				.execute(TextTools.cleanJID(googleUser.getEmail()));

		mav.getModel().put("showInvitation", talkfeedUser == null);
		qUser.closeAll();

		List<Blog> blogs = new ArrayList<Blog>();

		if (talkfeedUser != null) {
			// lkey from user
			Key key = talkfeedUser.getKey();

			// list subscription
			Query qSub = pm.newQuery(Subscription.class);
			qSub.setFilter("userKey == bk");
			qSub.declareParameters("com.google.appengine.api.datastore.Key bk");

			@SuppressWarnings("unchecked")
			List<Subscription> subs = (List<Subscription>) qSub.execute(key);

			for (Subscription sub : subs) {
				try {
					Blog b = pm.getObjectById(Blog.class, sub.getBlogKey());

					blogs.add(b);

				} catch (JDOObjectNotFoundException ex) {
					// blog deleted ! bad !!
					pm.currentTransaction().begin();
					pm.deletePersistent(sub);
					pm.currentTransaction().commit();
					pm.flush();
					continue;
				}
			}
			mav.getModel().put("blogs", blogs);
			qSub.closeAll();

		}

		pm.close();

		// no user
		if (talkfeedUser == null && googleUser != null && blogs.size() == 0) {
			return new ModelAndView("page/noblog");
		}

		return mav;
	}

	@RequestMapping(value = "/ajax/unsubscribe.*", method = RequestMethod.GET)
	public void ajaxUnsubscribe(HttpServletRequest req,
			HttpServletResponse resp, long id) throws Exception {
		System.out.println(id);

		UserService userService = UserServiceFactory.getUserService();
		User user = userService.getCurrentUser();

		if (user != null) {

			SubscriptionService serv = new SubscriptionService();
			serv.removeSubscription(user.getEmail(), id);
			resp.getWriter().write("OK");

		} else {
			throw new Exception("no user");
		}
	}

	@RequestMapping(value = "/ajax/posts.*", method = RequestMethod.GET)
	public ModelAndView ajaxLastEntries(long blogId) {
		DataManager dm = DataManagerFactory.getInstance();
		PersistenceManager pm = dm.newPersistenceManager();

		// select subscriptions
		Query q = pm.newQuery(BlogEntry.class);
		q.setOrdering("pubDate desc");
		q.setRange(0, 15);

		q.setFilter("blogKey == bk");
		q.declareParameters("com.google.appengine.api.datastore.Key bk");

		List<BlogEntry> list = new ArrayList<BlogEntry>(15);

		Blog blog = pm.getObjectById(Blog.class, blogId);

		@SuppressWarnings("unchecked")
		Collection<BlogEntry> col = (Collection<BlogEntry>) q.execute(blog
				.getKey());

		for (BlogEntry be : col) {
			list.add(be);
		}

		pm.close();

		ModelAndView mav = new ModelAndView("ajax/posts");
		mav.getModel().put("posts", list);

		return mav;
	}

	@RequestMapping(value = "/inviteme.*", method = RequestMethod.GET)
	public String inviteMe(HttpServletRequest req, HttpServletResponse resp)
			throws Exception {

		UserService userService = UserServiceFactory.getUserService();
		User user = userService.getCurrentUser();

		if (user != null) {

			JID jid = new JID(user.getEmail());
			TalkService.invite(jid);

			resp.addCookie(new Cookie("invited", "true"));

			return "page/invited";

		} else {

			throw new Exception("no user");
		}

	}

}
