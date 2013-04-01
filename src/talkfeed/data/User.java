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
public class User {

	@PrimaryKey
	@Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
	private Key key;
	
	@Persistent
	private String id;

	@Persistent
	private boolean paused;
	
	@Persistent
	private int interval;
	
	@Persistent
	private Key lastSubscriptionKey;
	
	@Persistent
	private Date dateCrea;
   
	@Persistent
	private Date lastUpdate;
	
	@Persistent
	private Date nextUpdate;
	
	@Persistent
	private boolean presence;
	
	public void setKey(Key key) {
		this.key = key;
	}

	public Key getKey() {
		return key;
	}


	public void setId(String id) {
		this.id = id;
	}

	/**
	 * Jabber ID / Email
	 */
	public String getId() {
		return id;
	}

	public void setPaused(boolean paused) {
		this.paused = paused;
	}

	/**
	 * Indicate if user has paused updates.
	 * @return
	 */
	public boolean isPaused() {
		return paused;
	}

	public void setInterval(int interval) {
		this.interval = interval;
	}

  /**
  Interval between each update in user's chat.
  */
	public int getInterval() {
		return interval;
	}
	
	@Override
	public String toString(){
		return this.id;
	}

	public void setLastSubscriptionKey(Key lastSubscriptionKey) {
		this.lastSubscriptionKey = lastSubscriptionKey;
	}

  /**
  Key of the last feed
  */
	public Key getLastSubscriptionKey() {
		return lastSubscriptionKey;
	}

  /**
  Account creation date
  */
	public Date getDateCrea() {
		return dateCrea;
	}

	public void setDateCrea(Date dateCrea) {
		this.dateCrea = dateCrea;
	}
  
  /**
    Last time user receive update
  */
	public Date getLastUpdate() {
		return lastUpdate;
	}

	public void setLastUpdate(Date lastUpdate) {
		this.lastUpdate = lastUpdate;
	}

	/**
	 * Next time user must be notified
	 * @return
	 */
	public Date getNextUpdate() {
		return nextUpdate;
	}

	public void setNextUpdate(Date nextUpdate) {
		this.nextUpdate = nextUpdate;
	}

	public boolean isPresence() {
		return presence;
	}

	public void setPresence(boolean presence) {
		this.presence = presence;
	}

	

	
}
