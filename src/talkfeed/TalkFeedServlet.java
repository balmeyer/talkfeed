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

import java.io.IOException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import talkfeed.talk.TalkService;

import com.google.appengine.api.xmpp.Message;

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
		//parse new message
		Message message = TalkService.parseMessage(req);
		
		//dispatch message
		/*OLD
		Dispatcher dispatcher = new Dispatcher();
		dispatcher.dispatch(message);*/
		
		Dispatcher.getInstance().dispatch(message);
		

	}
	
	
	
}
