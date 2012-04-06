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
package talkfeed.blog;

import java.util.Date;

import talkfeed.utils.TextTools;

public class FeedItem {
	
	private Channel channel;
	
    private String guid;
    private String title;
    private String description;
    private Date pubDate;
    private String link;
    private String author;
    private String category;
    private String feedburner_origLink;
    private String language;

    public FeedItem(Channel channel){
    	this.channel = channel;
    }
    
    /**
     * Unique identifier of the item
     * @return
     */
    public String getGuid() {
        
        //if no guid, unique identifier become link
        if (guid == null){
            return TextTools.md5Encode(this.link);
        }
        
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    /**
     * Post title
     * @return
     */
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Description of the RSS item
     * @return
     */
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Publication date 
     * @return
     */
    public Date getPubDate() {
        return pubDate;
    }

    public void setPubDate(Date pubDate) {
        this.pubDate = pubDate;
    }

    /**
     * Link of the post
     * @return
     */
    public String getLink() {
        return link;
    }

    public void setLink(String link) {
    	
    	if (link != null){
    		if (link.startsWith("/") && this.channel != null){
    			link = this.channel.getLink() + link;
    		}
    	}
    	
        this.link = TextTools.purgeLink(link);
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getFeedburnerOrigLink() {
        return feedburner_origLink;
    }

    public void setFeedburner_origLink(String feedburner_origLink) {
        this.feedburner_origLink = feedburner_origLink;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }
    
    
}

