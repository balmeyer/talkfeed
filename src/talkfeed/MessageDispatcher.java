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

import talkfeed.data.DataManager;
import talkfeed.data.DataManagerFactory;
import talkfeed.data.User;
import talkfeed.utils.TextTools;

import com.google.appengine.api.xmpp.Message;

/**
 *	Main XMPP message dispatcher
 * @author Balmeyer
 *
 */
public class MessageDispatcher {

	private static MessageDispatcher instance ;
	
	public static MessageDispatcher getInstance(){
		if (instance == null) instance = new MessageDispatcher();
		return instance;
	}
	
	/**
	 * No public constructor
	 */
	private MessageDispatcher(){}
	
	/**
	 * dispatch XMPP message
	 * @param msg
	 */
	public void dispatch(Message msg){
		
		DataManager dm = DataManagerFactory.getInstance();
		
		//check is user exists
		//clean email
		String jid = TextTools.cleanJID(msg.getFromJid().getId());

		
		User user = dm.getUserFromId(jid);
		if (user == null){
			user = new User();
			user.setId(jid);
			user.setDateCrea(new Date());
			user.setNextUpdate(new Date());
			dm.save(user);
		}
		
		//find instruction
		UserTask ins = UserTask.build(msg);
		QueuedTask.enqueue(ins);

	}



	
	
}
