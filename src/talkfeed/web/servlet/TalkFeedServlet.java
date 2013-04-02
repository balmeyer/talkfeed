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

package talkfeed.web.servlet;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import talkfeed.MessageDispatcher;
import talkfeed.UserManager;
import talkfeed.gtalk.TalkService;
import talkfeed.utils.TextTools;

import com.google.appengine.api.xmpp.Message;
import com.google.appengine.api.xmpp.Presence;

/**
 * Main TalkFeed Servlet where XMPP Message from Users are parsed.
 * @author JBVovau
 *
 */
@SuppressWarnings("serial")
public class TalkFeedServlet extends HttpServlet {

	/**
	 * XMPP message received.
	 */
	@Override
	public void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		
		//get type of task
		String action = req.getPathInfo();
		
		if(action == null) return;
		
		action = action.replace("/","");
		
		//MESSAGE
		if (action.equals("messagechat")) {
			//parse new Jabber message
			Message message = TalkService.parseMessage(req);
			
			//dispatch message
			MessageDispatcher.getInstance().dispatch(message);
			return;
		}
		
		//ERROR
		if (action.equals("messageerror")){
			
			Logger.getLogger("TalkFeedServlet").log(Level.WARNING, "error chat");
			return;
		}
		
		//PRESENCE STATUS

		
		//PRESENCE AVAILABE
		if(action.equals("presenceavailable")){
			UserManager um = new UserManager();
			
			Presence presence = TalkService.getPresence(req);
			
			String user = TextTools.cleanJID(presence.getFromJid().getId());
			um.setPresence(user, true);
		}
		
		//PRESENCE
		if(action.equals("presenceunavailable")){
			UserManager um = new UserManager();
			
			Presence presence = TalkService.getPresence(req);
			
			String user = TextTools.cleanJID(presence.getFromJid().getId());
			
			um.setPresence(user, false);
		}
	}
	
	
	
}
