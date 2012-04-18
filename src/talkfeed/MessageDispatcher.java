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

import java.util.Date;
import java.util.List;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import talkfeed.data.DataManager;
import talkfeed.data.DataManagerFactory;
import talkfeed.data.User;
import talkfeed.utils.TextTools;

import com.google.appengine.api.xmpp.Message;

/**
 * Main dispatcher for XMPP messages received by the application from users.
 * Those XMPP messages are commands typed in GTalk (or Jabber) windows.
 * 
 * @author Balmeyer
 * 
 */
public class MessageDispatcher {

	private static MessageDispatcher instance;

	public static MessageDispatcher getInstance() {
		if (instance == null)
			instance = new MessageDispatcher();
		return instance;
	}

	/**
	 * No public constructor
	 */
	private MessageDispatcher() {
	}

	/**
	 * dispatch XMPP message
	 * 
	 * @param msg
	 */
	public void dispatch(Message msg) {
		// TODO test
		DataManager dataManager = DataManagerFactory.getInstance();
		PersistenceManager pm = dataManager.newPersistenceManager();

		try {
			// check is user exists, if not, create it
			// clean email
			String jid = TextTools.cleanJID(msg.getFromJid().getId());

			User user = null;
			Query q = pm.newQuery(User.class);
			q.setFilter("id == jid");
			q.declareParameters("java.lang.String jid");

			@SuppressWarnings("unchecked")
			List<User> list = (List<User>) q.execute(jid);

			if (list.size() > 0) {
				user = list.get(0);
			}

			if (user == null) {
				user = new User();
				user.setId(jid);
				user.setDateCrea(new Date());
				user.setNextUpdate(new Date());
				pm.makePersistent(user);
			}
			q.closeAll();

		} finally {
			pm.close();
		}

		// build user task from chat message
		UserTask userTask = UserTask.build(msg);
		// add task to queue treatment
		QueuedTask.enqueue(userTask);

	}

}
