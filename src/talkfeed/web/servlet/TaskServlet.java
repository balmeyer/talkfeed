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
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import talkfeed.command.Command;
import talkfeed.command.CommandFactory;

/**
 * Manage queued tasks
 * @author JBVovau
 *
 */
public class TaskServlet extends HttpServlet  {

	/**
	 * 
	 */
	private static final long serialVersionUID = -689549981856939500L;

	/**
	 * 
	 */
	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException{
		
		//get type of task
		String task = req.getPathInfo();
		
		Map<String,String> map = new HashMap<String,String>();
		
		@SuppressWarnings("unchecked")
		Enumeration<String> en = req.getParameterNames();
		
		while (en.hasMoreElements()){
			String name = en.nextElement();
			String value = req.getParameter(name);
			map.put(name,value);
		}
		
		task = task.replace("/", "");
		
		//retrieve command instance
		Command cmd = CommandFactory.get(task);
		
		cmd.execute(map);
		

	}
	
	
}
