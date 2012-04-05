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
package talkfeed.gtalk;

import com.google.appengine.api.xmpp.JID;

/**
 * New notification
 * @author vovau
 *
 */
public class GTalkBlogNotification {

	private JID jabberID;
	
	private String blogTitle;
	
	private String postTitle;
	
	private String postUrl;

	/**
	 * JabberID to send message
	 * @return
	 */
	public JID getJabberID() {
		return jabberID;
	}

	public void setJabberID(JID jabberID) {
		this.jabberID = jabberID;
	}

	public String getBlogTitle() {
		return blogTitle;
	}

	public void setBlogTitle(String blogTitle) {
		this.blogTitle = blogTitle;
	}

	public String getPostTitle() {
		return postTitle;
	}

	public void setPostTitle(String postTitle) {
		this.postTitle = postTitle;
	}

	public String getPostUrl() {
		return postUrl;
	}

	public void setPostUrl(String postUrl) {
		this.postUrl = postUrl;
	}
	
}
