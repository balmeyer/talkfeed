/*
 Copyright 2010/2012 - Jean-Baptiste Vovau

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

import java.util.HashMap;
import java.util.Map;

import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.google.appengine.api.taskqueue.TaskOptions.Method;

/**
 * Task executed in Google App Engine "queues".
 * @author JBVovau
 *
 */
public class QueuedTask {

	private final static String START_URL = "/tasks/";

	//parameters of the task (givien in link)
	private Map<String, String> params;
	protected TaskType type;

	/**
	 * add task to queue
	 * 
	 * @param ins
	 */
	public static void enqueue(QueuedTask ins) {

		// no instruction
		if (ins == null)
			return;

		// find default queue
		Queue q = QueueFactory.getDefaultQueue();

		//build task options
		TaskOptions options = withUrl(ins.getUrl()).method(Method.GET);

		// add parameters to task
		for (String name : ins.getParams().keySet()) {
			String value = ins.getParams().get(name);
			options = options.param(name, value);
		}

		//actually add to GAE Queue
		q.add(options);

	}

	public QueuedTask() {
		this.params = new HashMap<String, String>();
	}

	/**
	 * Parameters of the task, in a key/value map.
	 * @return
	 */
	public Map<String, String> getParams() {
		return this.params;
	}

	/**
	 * Add a parameter to the task
	 * @param key
	 * @param value
	 */
	public void addParam(String key, Object value) {
		if (value == null) return;
		
		this.params.put(key, value.toString());
	}

	/**
	 * Type of the task
	 * @return
	 */
	public TaskType getType() {
		return this.type;
	}

	public void setType(TaskType type) {
		this.type = type;
	}

	/**
	 * 
	 * @return
	 */
	private String getUrl() {
		if (this.type == null)
			return null;
		return START_URL + this.type;
	}

	@Override
	public String toString(){
		return this.type + " [" + this.getUrl() + "] " + this.params;
	}
	
	/**
	 * accepted type of the task
	 * @author vovau
	 * 
	 */
	public enum TaskType {
		account, add, back, del, list, next, say, purge, remove ,
		updateblog, updateuser
	}

}
