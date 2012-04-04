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
