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

import java.net.MalformedURLException;
import java.net.URL;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import talkfeed.utils.DocumentLoader;

/**
 * Loading and managing feeds.
 * @author Jean-Baptiste Vovau
 *
 */
public class FeedManager {

	//markers to find type of feed
	private static final String [] atomMarkers = {"entry","feed","title","id","link","updated"};
	private static final String [] rssMarkers = {"rss","channel","title","item","link","pubdate"};
	
	/**
	 * No instance
	 */
	private FeedManager(){}
	
	/**
	 * Find if document loaded is XML or ATOM Feed.
	 * @param content
	 * @return
	 */
	public static boolean isFeed(String content){
		
		return (containsAllMarkers(content, rssMarkers) || containsAllMarkers(content, atomMarkers));
	}
	
	private static boolean containsAllMarkers(String content, String [] markers){
		if(content == null) return false;
		for (String word : markers){
			if (!content.toLowerCase().contains(word)) return false;
		}
		return true;
	}
	
	/**
	 * Load Feed from server
	 * @param rss
	 * @return
	 */
    public static Channel loadRss(String rss) {
        Channel channel = null;

        try {

            // obtiens l'url
            URL url = new URL(rss);

            // document XML
            Document doc = DocumentLoader.load(url);

            if (doc != null) {

                // parser
                FeedParser parser = findParser(doc);

                // channel
                channel = parser.parse(doc);

                if (channel != null) {
                    channel.setRss(rss);
                }
            }
        } catch (MalformedURLException ex) {

            // Logger.getLogger(RssFeedParser.class.getName()).log(Level.SEVERE, null, ex);
        }

        return channel;
    }

    private static FeedParser findParser(Document doc) {

        // atom
        NodeList nl = doc.getElementsByTagName("entry");

        if (nl.getLength() > 0) {
            return new AtomFeedParser();
        }

        // TODO faire tous les parsers
        return new RssFeedParser();
    }
}

