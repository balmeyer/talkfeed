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

import talkfeed.data.DataManager;
import talkfeed.data.DataManagerFactory;
import talkfeed.data.User;
import talkfeed.utils.TextTools;

import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.google.appengine.api.taskqueue.TaskOptions.Method;
import com.google.appengine.api.xmpp.Message;

/**
 * Dispatch XMPP message
 * @author Balmeyer
 *
 */
public class Dispatcher {

	private static Dispatcher instance ;
	
	public static Dispatcher getInstance(){
		if (instance == null) instance = new Dispatcher();
		return instance;
	}
	
	/**
	 * No public constructor
	 */
	private Dispatcher(){}
	
	/**
	 * dispatch XMPP message
	 * @param msg
	 */
	public void dispatch(Message msg){
		
		DataManager dm = DataManagerFactory.getInstance();
		
		//check is user exists
		//clean email
		String jid = TextTools.cleanJID(msg.getFromJid().getId());
		//System.out.println(jid);
		User user = dm.getUserFromId(jid);
		if (user == null){
			user = new User();
			user.setId(jid);
			user.setDateCrea(new Date());
			user.setNextUpdate(new Date());
			dm.save(user);
		}
		
		//find instruction
		Instruction ins = Instruction.build(msg);
		this.enqueue(ins);
		
		//TODO add commands
		//addSource(user, msg);
	}


	/**
	 * Add instruction to queue
	 * @param ins
	 */
	private void enqueue(Instruction ins){
		
		//no instruction
		if (ins == null) return;
		
		//find queue
		Queue q = QueueFactory.getDefaultQueue();
		
		TaskOptions options = withUrl(ins.getUrl()).method(Method.GET);
		
		//add params
		for( String name : ins.getParams().keySet()){
			String value = ins.getParams().get(name);
			options = options.param(name, value);
		}
		
		//add to Queue
		q.add(options);
	}
	
	
}
