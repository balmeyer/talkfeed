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
package talkfeed.command;

import java.util.HashMap;
import java.util.Map;

/**
 * build command from name task. (url is called by queue : /tasks/<name>)
 * @author JBVovau
 *
 */
public class CommandFactory {

	private static Map<String, Command> typeToCommand ;
	
	/** No instance */
	private CommandFactory(){}
	
	public static Command get(String type){
		//init map
		if (typeToCommand == null) init();
		
		Command instance = typeToCommand.get(type);
		
		if (instance == null){
			throw new IllegalArgumentException("type not found : " + type);
		}
		
		return instance;
	}
	
	
	private static void init(){
		typeToCommand = new HashMap<String,Command>();
		
		for(Class<? extends Command> claz : listClass()){
			CommandType ctype = claz.getAnnotation(CommandType.class);
			Command instance = null;
			try {
				instance = claz.newInstance();
				typeToCommand.put(ctype.value() , instance);
			} catch (InstantiationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		
	}
	
	/**
	 * List of all command class
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private static Class<? extends Command> [] listClass(){
		return new Class [] {
			CommandAddSource.class,
			CommandRefreshBlogs.class,
			CommandRefreshSubscriptions.class,
			CommandAccount.class,
			CommandSay.class,
			CommandUpdateBlog.class,
			CommandUpdateSubscription.class,
			CommandPurge.class,
			CommandRemove.class,
			CommandPurgeBlogs.class,
			CommandList.class,
			CommandNext.class,
			CommandBack.class,
			CommandUpdateUser.class
		};
	}
}
