package talkfeed.data;

import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

@PersistenceCapable
public class UserMark {

	@PrimaryKey
	private String id;
	
	@Persistent
	private long lastId;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public long getLastId() {
		return lastId;
	}

	public void setLastId(long lastId) {
		this.lastId = lastId;
	}
	
}
