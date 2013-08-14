/*
 Copyright 2010 - 2013 Jean-Baptiste Vovau

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
package talkfeed.web;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;

import talkfeed.cache.BlogCache;
import talkfeed.cache.UserPresence;

@Controller
public class ProbeController {

	@RequestMapping(value = "/probe.htm", method = RequestMethod.GET)
	public ModelAndView index(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		
       UserService userService = UserServiceFactory.getUserService();
       User user = userService.getCurrentUser();
	
        if (user != null ){
        	if (!userService.isUserAdmin()) {
        		resp.sendRedirect("/index.htm");
        	}
        } else {
        	resp.sendRedirect(userService.createLoginURL(req.getRequestURI()));
        }

		ModelAndView mav = new ModelAndView("probe/index");

		int nbusers = UserPresence.listUsers().size();
		int activeblogs = BlogCache.countActiveBlogs();
		
		mav.getModel().put("nbusers", nbusers);
		mav.getModel().put("nbactiveblogs", activeblogs);
		
		mav.getModel().put("blogs", BlogCache.getAllActiveBlogs());
		mav.getModel().put("blogstoupdate", BlogCache.getActiveBlogsToUpdate(20));
		
		mav.getModel().put("users", UserPresence.listUsers());
		mav.getModel().put("usersToUpdate", UserPresence.listUserByNextUpdate(20));
		
		return mav;
	}

}
