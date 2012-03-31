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

package talkfeed.data;

import java.util.Date;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import com.google.appengine.api.datastore.Key;

@PersistenceCapable
public class Subscription {

	@PrimaryKey
	@Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
	private Key key ;
	
	@Persistent
	private Key userKey;
	
	@Persistent
	private Key blogKey;
	
	@Persistent
	private int priority;
	
	@Persistent
	private Date lastDate;

	@Persistent
	private Date lastProcessDate;
	
	public void setKey(Key key) {
		this.key = key;
	}

	public Key getKey() {
		return key;
	}

	public void setUserKey(Key userKey) {
		this.userKey = userKey;
	}

	/**
	 * User primary key
	 * @return
	 */
	public Key getUserKey() {
		return userKey;
	}

	public void setBlogKey(Key blogKey) {
		this.blogKey = blogKey;
	}

	/**
	 * Blog primary key
	 * @return
	 */
	public Key getBlogKey() {
		return blogKey;
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}

	/**
	 * Priority of subscription for user.
	 * 
	 * @return
	 */
	public int getPriority() {
		return priority;
	}

	public void setLastDate(Date lastDate) {
		this.lastDate = lastDate;
	}

	/**
	 * Last read date
	 * @return
	 */
	public Date getLastDate() {
		return lastDate;
	}

	public void setLastProcessDate(Date lastProcessDate) {
		this.lastProcessDate = lastProcessDate;
	}

	public Date getLastProcessDate() {
		return lastProcessDate;
	}
	
	
}
