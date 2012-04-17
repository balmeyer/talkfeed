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

/**
 * Entry( or news, or post) from a web source
 * @author JBVovau
 *
 */
@PersistenceCapable
public class BlogEntry {
	
	@PrimaryKey
	@Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
	private Key key;

	@Persistent
	private Key blogKey;
	
	@Persistent
	private Date creaDate;
	
	@Persistent
	private Date pubDate;
	
	@Persistent
	private String title;
	
	@Persistent
	private String link;
	
	@Persistent
	private String shortLink;
	
	//denormalized to save datastore access
	@Persistent
	private String blogTitle;
	
	public void setKey(Key key) {
		this.key = key;
	}

	public Key getKey() {
		return key;
	}

	public void setBlogKey(Key blogKey) {
		this.blogKey = blogKey;
	}

	public Key getBlogKey() {
		return blogKey;
	}

	public void setCreaDate(Date creaDate) {
		this.creaDate = creaDate;
	}

	public Date getCreaDate() {
		return creaDate;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getTitle() {
		return title;
	}

	public void setLink(String link) {
		this.link = link;
	}

	public String getLink() {
		return link;
	}


	public void setPubDate(Date pubDate) {
		this.pubDate = pubDate;
	}

	public Date getPubDate() {
		return pubDate;
	}
	
	@Override
	public String toString(){
		if (this.title != null) return this.title;
		return this.link;
	}

	public String getShortLink() {
		return shortLink;
	}

	public void setShortLink(String shortLink) {
		this.shortLink = shortLink;
	}

	public String getBlogTitle() {
		return blogTitle;
	}

	public void setBlogTitle(String blogTitle) {
		this.blogTitle = blogTitle;
	}
}
