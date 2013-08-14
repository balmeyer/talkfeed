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

import java.io.Serializable;
import java.util.Date;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import com.google.appengine.api.datastore.Key;

/**
 * Information source
 * @author JBVovau
 *
 */
@PersistenceCapable
public class Blog implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@PrimaryKey
	@Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
	private Key key;
	
	@Persistent
	private String link;
	
	@Persistent
	private String rss;
	
	@Persistent
	private String title;
	
	@Persistent
	private Date lastUpdate;
	
	@Persistent
	private Date nextUpdate ;
	
	@Persistent
	private int refreshInterval;
	
	@Persistent
	private Date lastEntry;

	public void setKey(Key key) {
		this.key = key;
	}

	public Key getKey() {
		return key;
	}

	public void setLink(String link) {
		this.link = link;
	}

	/**
	 * Link (url) of the web source.
	 * @return
	 */
	public String getLink() {
		return link;
	}

	public void setRss(String rss) {
		this.rss = rss;
	}

	/**
	 * RSS url of web source.
	 * @return
	 */
	public String getRss() {
		//relative path for rss feed
		if (rss != null && rss.startsWith("/")){
			return this.getLink() + rss;
		}
		
		return rss;
	}

	public void setLatestEntry(Date lastUpdate) {
		this.lastUpdate = lastUpdate;
	}

	/**
	 * Returns pubDate of the more recent entry.
	 * @return
	 */
	public Date getLatestEntry() {
		return lastUpdate;
	}

	public void setRefreshInterval(int refreshInterval) {
		this.refreshInterval = refreshInterval;
	}

	public int getRefreshInterval() {
		return refreshInterval;
	}

	public void setNextUpdate(Date nextUpdate) {
		this.nextUpdate = nextUpdate;
	}

	public Date getNextUpdate() {
		return nextUpdate;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getTitle() {
		return title;
	}
	
	@Override
	public String toString(){
		if (title != null) return title;
		return this.link;
	}
	
	public String getLabel(){
		return this.toString();
	}
}
