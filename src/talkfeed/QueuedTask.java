package talkfeed;

import static com.google.appengine.api.taskqueue.TaskOptions.Builder.withUrl;

import java.util.HashMap;
import java.util.Map;

import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.google.appengine.api.taskqueue.TaskOptions.Method;

public class QueuedTask {

	private final static String START_URL = "/tasks/";

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

		// find queue
		Queue q = QueueFactory.getDefaultQueue();

		TaskOptions options = withUrl(ins.getUrl()).method(Method.GET);

		// add params
		for (String name : ins.getParams().keySet()) {
			String value = ins.getParams().get(name);
			options = options.param(name, value);
		}

		// add to Queue
		q.add(options);

	}

	public QueuedTask() {
		this.params = new HashMap<String, String>();
	}

	public Map<String, String> getParams() {
		return this.params;
	}

	public void addParam(String key, Object value) {
		if (value == null) return;
		
		this.params.put(key, value.toString());
	}

	/**
	 * 
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
	public String getUrl() {
		if (this.type == null)
			return null;
		return START_URL + this.type;
	}

	@Override
	public String toString(){
		return this.type + "[" + this.getUrl() + "]" + this.params;
	}
	
	/**
	 * 
	 * @author vovau
	 * 
	 */
	public enum TaskType {
		account, add, back, del, list, next, say, purge, remove ,
		updateblog, updateuser
	}

}
