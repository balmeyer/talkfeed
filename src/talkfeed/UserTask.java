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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import talkfeed.utils.TextTools;

import com.google.appengine.api.xmpp.Message;




/**
 * Instruction for the dispatcher, to enqueue
 * @author JBVovau
 *
 */
public class UserTask extends QueuedTask {
	
	//accepted instruct
	private static final List<String> codesForAccount 
		= Arrays.asList(new String [] { "start","stop","pause" ,"on","off" ,"every","purge"});
	private static final List<String> codesForManagingBlogs 
		= Arrays.asList(new String [] {"add","remove","del","next","say","list","back"});
	private static ArrayList<String> arrayString;
	
	private Message message;
	
	
	/**
	 * Build instruction from message
	 * @param message
	 * @return
	 */
	public static UserTask build(Message message){
		UserTask userTask = new UserTask(message);
		
		//analyze body message
		if (message.getBody() == null) return null;
		
		//split body 
		String [] words = message.getBody().split(" ");
		
		//nothing ?
		if (words.length == 0) return null;
		
		String mainArg = words[0].toLowerCase();
		
		//build array list containing all commands
		if (arrayString == null){
			arrayString = new ArrayList<String>();
			arrayString.addAll(codesForAccount);
			arrayString.addAll(codesForManagingBlogs);
		}

		//say
		if (words.length > 1 && mainArg.equals("say")){
			userTask.type = TaskType.say;
			userTask.addParam("id", TextTools.cleanJID(message.getFromJid().getId()));
			userTask.addParam("msg", message.getBody());
			return userTask;
		}
		

		//test if instruction is account
		if (codesForAccount.contains(mainArg)){
			//manage account
			userTask.addParam("id", TextTools.cleanJID(message.getFromJid().getId()));
			userTask.type = TaskType.account;
			//add
			if (mainArg.equals("start") || mainArg.equals("on")){
				userTask.addParam("run", "1");
			}
			//pause
			if (mainArg.equals("stop") || mainArg.equals("pause") || mainArg.equals("off")){
				userTask.addParam("run", "0");
			}
			
			//intervals
			if (mainArg.equals("every") && words.length >1){
				//time
				userTask.addParam("time", words[1]);
				//unit
				if (words.length >2){
					userTask.addParam("unit", words[2]);
				} else {
					userTask.addParam("unit", "minutes");
				}
			}
			
			//remove
			if (mainArg.startsWith("remove") && words.length >1){
				userTask.addParam("number", words[1]);
			}
			
			//purge all
			if (mainArg.endsWith("purge")){
				userTask.type = TaskType.purge;
				return userTask;
			}
			
			return userTask;
		}
	
		//instructions about feeds
		if (codesForManagingBlogs.contains(mainArg)){
			userTask.type = Enum.valueOf(TaskType.class , mainArg) ;
			userTask.addParam("id", TextTools.cleanJID(message.getFromJid().getId()));
			
			//add more args
			int n = 1;
			for(String w : words){
				userTask.addParam("arg" + (n++) , w);
			}
			
			return userTask;
		}
	
		//simple add site instruction
		if (words.length == 1 && mainArg.contains(".")){
			//add instruction
			userTask.type = TaskType.add;
			userTask.addParam("id", TextTools.cleanJID(message.getFromJid().getId()));
			userTask.addParam("link", words[0].toLowerCase());
			return userTask;
		}
		

		return null;
	}
	
	/**
	 * Build instruction from message
	 * @param message
	 * @return
	 */
	private UserTask(Message message){
		super();
		this.setMessage(message);
		
	}

	public void setMessage(Message message) {
		this.message = message;
	}

	public Message getMessage() {
		return message;
	}
	



}