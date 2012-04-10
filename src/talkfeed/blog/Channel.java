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
import java.util.LinkedList;
import java.util.List;

/**
 * Model of a RSS Channel
 * 
 * @author Jean-Baptiste Vovau
 *
 */
public class Channel {
	
    private String title;
    private String subtitle;
    private String link;
    private String description;
    private Date lastBuildDate;
    private String managingEditor;
    private String generator;
    private String atom_id;
    private String rss;
    private String language;
    private String rights;
    
    //list of items
    private LinkedList<FeedItem> items;
    
    //indicate if items are sorted by pubDate
    //private boolean isSorted = false;
    
    public Channel(){
        this.items = new LinkedList<FeedItem>();
    }
    
    @Override
    public String toString(){
        return this.getRss();
    }
    
    /**
     * Add item or article
     * @param item
     */
    public void insertItemAtFirstPlace(FeedItem item){
         //this.getItems().add(item);
    	this.items.add(0, item);
    	
    	//force sort by date
    	//isSorted = false;
    }
    

    /**
     * Return items ordered by date
     * @return
     */
    public List<FeedItem> items(){
    	return this.items;
    }
    
    /**
     * Channel title
     * @return
     */
    public String getTitle() {
        return title;
    }

    /**
     * 
     * @param title
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * URL du site ou du blog
     * @return
     */
    public String getLink() {
        if (link == null && this.rss != null){
            //calcul du blog
            int index = 0;
            index = rss.indexOf("/",7);

            if (index>0){
               link = (rss.substring(0,index));
            } 
        }
        
        return link;
    }

    /**
     * Attribue l'URL du site ou du blog
     * @param link
     */
    public void setLink(String link) {
        if (link == null) return;
        
        while (link.endsWith("/")){
            link = link.substring(0,link.length() - 1);
        }
        this.link = link;
    }

    /**
     * Description du flux
     * @return
     */
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * 
     * @return
     */
    public Date getLastBuildDate() {
        return lastBuildDate;
    }

    public void setLastBuildDate(Date lastBuildDate) {
        this.lastBuildDate = lastBuildDate;
    }

    public String getManagingEditor() {
        return managingEditor;
    }

    public void setManagingEditor(String managingEditor) {
        this.managingEditor = managingEditor;
    }

    public String getGenerator() {
        return generator;
    }

    public void setGenerator(String generator) {
        this.generator = generator;
    }

    public String getAtom_id() {
        return atom_id;
    }

    public void setAtom_id(String atom_id) {
        this.atom_id = atom_id;
    }


    public String getRss() {
        return rss;
    }

    public void setRss(String rss) {
        this.rss = rss;
    }

    /**
     * feed language
     * @return
     */
    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    /**
     * sub title
     * @return
     */
    public String getSubtitle() {
        return subtitle;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    /**
     * Droits et copyright
     * @return
     */
    public String getRights() {
        return rights;
    }

    public void setRights(String rights) {
        this.rights = rights;
    }
    
}
