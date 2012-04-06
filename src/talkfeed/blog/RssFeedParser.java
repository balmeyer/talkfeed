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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.logging.Logger;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import talkfeed.utils.TextTools;

/**
 * Transform RSS Format in Channel object.
 * @author JBVovau
 *
 */
public class RssFeedParser implements FeedParser {

	   
    private DateFormat dateFormatRSS2;
    private DateFormat dateFormatRDF;
    private DateFormat dateFormatRSS1;
    
    private static final int NODE_DATE = 0;
    private static final int NODE_TITLE = 1;
    private static final int NODE_AUTHOR = 2;
    private static final int NODE_LANGUAGE = 3;
    private static final int NODE_LINK = 4;
    private static final int NODE_DESCRIPTION = 5;
    
    public RssFeedParser(){
        dateFormatRSS2 = new SimpleDateFormat(
            "EEE, dd MMM yyyy HH:mm:ss Z", Locale.US);
        
        dateFormatRDF = new SimpleDateFormat(
            "yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.US);
        
        dateFormatRSS1 = new SimpleDateFormat(
            "yyyy-MM-dd'T'HH:mm:ss", Locale.US);
    }
    

    /**
     * 
     * @param doc
     */
    @Override
	public Channel parse(Document doc) {
        
        //create new channel to return
        Channel channel = new Channel();
        
        NodeList list = doc.getElementsByTagName("channel");
        
        if (list.getLength() == 0){
            Logger.getLogger("FEED_PARSER").warning("no channel in doc ! ");
            return null;
        }
        //if (node == null) throw new RssParseException("channel node not found");
        
        Node node = list.item(0).getFirstChild();
        
        while (node != null){

            //--- TITLE
            if (node.getNodeName().equals("title")){
                channel.setTitle(node.getTextContent());
            }
            
            //---- LAST BUILD DATE
            if (node.getNodeName().equals("lastBuildDate")){
                channel.setLastBuildDate(
                        TextTools.getDateContent(this.dateFormatRSS2,node));
            }
            
            //--- LINK
            if (node.getNodeName().equals("link")){
                channel.setLink(node.getTextContent());
            }
            
            //--- DESCRIPTION
            if (node.getNodeName().equals("description")){
                channel.setDescription(node.getTextContent());
            }
            
            //---- GENERATOR
            if (node.getNodeName().equals("generator")){
                channel.setGenerator(node.getTextContent());
            }
            
            //--- MANAGING EDITOR
            if (node.getNodeName().equals("managingEditor")){
                channel.setManagingEditor(node.getTextContent());
            }
            
            //--- MANAGING EDITOR
            if (node.getNodeName().equals("atom:id")){
                channel.setAtom_id(node.getTextContent());
            }
            

            //--- LANGAGE
            if (node.getNodeName().equals("language")){
                channel.setLanguage(node.getTextContent());
            }
            node = node.getNextSibling();
        }
        
        //-------------------------------------------
        //ITEMS
        NodeList listItems = doc.getElementsByTagName("item");
        node = listItems.item(0);
        
        while (node != null){
            //--- ITEM
            if (node.getNodeName().equals("item")){
                channel.insertItemAtFirstPlace(parseItem(channel , node));
            }
            node = node.getNextSibling();
        }
        
        return channel;
    }
    

    /**
     * Parse item
     * @param chan
     * @param item
     * @return
     */
    private FeedItem parseItem(Channel chan, Node item){
        FeedItem feedItem = new FeedItem(chan);
        
        Node node = item.getFirstChild();
        while (node != null){
            String nodeValue = node.getNodeName();
            //--- TITLE
            if (isNode(nodeValue, NODE_TITLE)){
                feedItem.setTitle(node.getTextContent());
            }
            
            //--- GUID
            if (nodeValue.equals("guid")){
                feedItem.setGuid(node.getTextContent());
            }
            
            if (isNode(nodeValue, NODE_DESCRIPTION)){
                feedItem.setDescription(node.getTextContent());
            }
            
            if (isNode(nodeValue, NODE_LINK)){
                feedItem.setLink(node.getTextContent());
            }
            
            if (isNode(nodeValue, NODE_AUTHOR)){
                feedItem.setAuthor(node.getTextContent());
            }
            
            if (nodeValue.equals("feedburner:origLink")){
                feedItem.setFeedburner_origLink(node.getTextContent());
            }
            
            if (isNode(nodeValue, NODE_DATE)){
                feedItem.setPubDate(parseDate(node));
            }

            if (isNode(nodeValue, NODE_LANGUAGE)){
                feedItem.setLanguage(node.getTextContent());
            }
            node = node.getNextSibling();
        }
        
        return feedItem;
    }
    
    /**
     * Test if field is a valid type.
     * @param value
     * @return
     */
    private boolean isNode(String value , int typeNode){
        
        value = value.replace("dc:","");
        
        switch (typeNode){
            case NODE_DATE:
                return value.equals("pubDate") || value.equals("date");
            case NODE_AUTHOR:
                return value.equals("author") || value.equals("creator");
            case NODE_DESCRIPTION:
                return value.equals("description") || value.equals("atom:summary");
            case NODE_LANGUAGE:
                return value.equals("language");
            case NODE_LINK:
                return value.equals("link");
            case NODE_TITLE:
                return value.equals("title");
        }
        
        return false;
    }
    
    
    private Date parseDate(Node node){
        Date date = null;
        
        date = TextTools.getDateContent(
                        this.dateFormatRSS2,node);
        
        if (date == null){
            date = TextTools.getDateContent(dateFormatRDF, node);
        }
        
        if (date == null){
            String d = node.getTextContent();
            int plus = d.indexOf('+');
            if (plus>0){
                d = d.substring(0,plus);
            }
            date = TextTools.getDateContent(dateFormatRSS1, d);
        }
        
        
        return date;
        
    }
    /*
    private FeedItem parseRdf(Node rdf){
        FeedItem feedItem = new FeedItem();
        feedItem.
    } */
    
}