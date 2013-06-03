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
package talkfeed.feed;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import talkfeed.utils.TextTools;

/**
 * Parse document in ATOM format.
 * @author JBVovau
 *
 */
public class AtomFeedParser implements FeedParser {

    private DateFormat dateFormat ;
    
    public AtomFeedParser(){
                dateFormat = new SimpleDateFormat(
            "yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
    }
    
    @Override
	public Channel parse(Document doc) {
        Channel c = new Channel();
        
        //title
        c.setTitle(getNodeValue(doc, "title"));
        c.setSubtitle(getNodeValue(doc,"subtitle"));
        c.setRights(getNodeValue(doc, "rights"));
        c.setLink(getLink(doc, "text/html"));
        c.setRss(getLink(doc, "application/atom+xml"));
        
        //update date 
        String sDate = getNodeValue(doc, "updated");
        if (sDate != null){
            c.setLastBuildDate(TextTools.getDateContent(dateFormat, sDate));
        }
        //s
        System.out.println("");
        //items
        NodeList items = doc.getElementsByTagName("entry");
        for (int i = 0 ; i < items.getLength();i++){
            Node item = items.item(i);
            c.insertItemAtFirstPlace(parseItem(c, item));
        }
        return c;
    }

    /**
     * Parse atom items
     * @param node
     * @return
     */
    private FeedItem parseItem(Channel chan, Node node){
        FeedItem item = new FeedItem(chan);
        
        Node n = node.getFirstChild();
        
        while (n != null){
            if (n.getNodeName().equals("title")){
                item.setTitle(n.getTextContent());
            }
            
            if (n.getNodeName().equals("link")){
                String link = n.getAttributes().getNamedItem("href")
                        .getNodeValue();
                item.setLink(link);
            }
            
            if (n.getNodeName().equals("content") ||n.getNodeName().equals("summary") ){
                item.setDescription(n.getTextContent().trim());
            }
            
            if (n.getNodeName().equals("published")){
                item.setPubDate(TextTools.getDateContent(dateFormat, n));
            }
            
            n = n.getNextSibling();
        }
       
        return item;
    }
    
    private String getNodeValue(Document doc , String name){
        NodeList nl = doc.getElementsByTagName(name);
        if (nl.getLength()>0){
            Node n = nl.item(0);
            return n.getTextContent().trim();
        }
        
        return null;
    }
    

    /**
     * Extract link reference
     * @param doc
     * @param rel
     * @return
     */
    private String getLink(Document doc , String type){
    	//StringBuilder sb = new StringBuilder();
    	
    	NodeList list = doc.getElementsByTagName("link");
    	
    	for(int i = 0 ; i < list.getLength() ; i++){
    		Node n = list.item(i);
    		Node attr = n.getAttributes().getNamedItem("type");
    		if (attr != null){
    			if (type.equals(attr.getNodeValue())){
    				Node aLink = n.getAttributes().getNamedItem("href");
    				if (aLink != null) return aLink.getNodeValue();
    			}
    		}
    	}
    	
    	return null;
    }
    
}
