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
public class Instruction extends QueuedTask {
	
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
	public static Instruction build(Message message){
		Instruction ins = new Instruction(message);
		
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


		//test if instruction is account
		if (codesForAccount.contains(mainArg)){
			//manage account
			ins.addParam("id", TextTools.cleanJID(message.getFromJid().getId()));
			ins.type = TaskType.account;
			//add
			if (mainArg.equals("start") || mainArg.equals("on")){
				ins.addParam("run", "1");
			}
			//pause
			if (mainArg.equals("stop") || mainArg.equals("pause") || mainArg.equals("off")){
				ins.addParam("run", "0");
			}
			
			//intervals
			if (mainArg.equals("every") && words.length >1){
				//time
				ins.addParam("time", words[1]);
				//unit
				if (words.length >2){
					ins.addParam("unit", words[2]);
				} else {
					ins.addParam("unit", "minutes");
				}
			}
			
			//purge all
			if (mainArg.endsWith("purge")){
				ins.type = TaskType.purge;
				return ins;
			}
			
			return ins;
		}
	
		//instructions about feeds
		if (codesForManagingBlogs.contains(mainArg)){
			ins.type = Enum.valueOf(TaskType.class , mainArg) ;
			ins.addParam("id", TextTools.cleanJID(message.getFromJid().getId()));
			
			//add more args
			int n = 1;
			for(String w : words){
				ins.addParam("arg" + (n++) , w);
			}
			
			return ins;
		}
	
		//simple add site instruction
		if (words.length == 1 && mainArg.contains(".")){
			//add instruction
			ins.type = TaskType.add;
			ins.addParam("id", TextTools.cleanJID(message.getFromJid().getId()));
			ins.addParam("link", words[0].toLowerCase());
			return ins;
		}
		
		//say
		if (words.length > 1 && mainArg.equals("say")){
			ins.type = TaskType.say;
			ins.addParam("id", TextTools.cleanJID(message.getFromJid().getId()));
			ins.addParam("msg", message.getBody());
			return ins;
		}
		
		return null;
	}
	
	/**
	 * Build instruction from message
	 * @param message
	 * @return
	 */
	private Instruction(Message message){
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