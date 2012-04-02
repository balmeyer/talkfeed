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
package talkfeed.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;

import org.w3c.dom.Node;

/**
 * Misc tools
 * @author Jean-Baptiste Vovau
 *
 */
public class TextTools {

	private static final String PATTERN_RSS_XML = "application/rss+xml";
	private static final String PATTERN_ATOM_XML = "application/atom+xml";

	public static String cleanJID(String id){
		if (id == null) return id;
		
		int slash = id.indexOf("/");
		
		if(slash> 0){
			return id.substring(0 , slash);
		}
		
		return id;
	}
	
	
	public static String purgeLink(String link) {

		if (link == null)
			return null;

		//end slash at the end of link
		while (link.endsWith("/")) {
			link = link.substring(0, link.length() - 1);
		}

		// forbidden char
		char[] stopchar = { ' ', '"', ',', };
		int end = -1;
		for (char s : stopchar) {
			end = link.indexOf(s);
			if (end > 0) {
				link = link.substring(0, end);
			}
		}

		//add start
		if (!link.startsWith("/") && !link.startsWith("http")
				&& !link.contains("://")){
			link = "http://" + link;
		}
		
		return link.trim();
	}


	public static String extractRssFromPage(String body) {

		if (body == null) {
			return null;
		}

		body = body.toLowerCase();

		int index = body.indexOf(PATTERN_RSS_XML);

		if (index < 0) {
			index = body.indexOf(PATTERN_ATOM_XML);
		}

		if (index >= 0) {
			
			//fetch entire tag
			
			// fin de balise
			int end = body.indexOf('>', index);

			if (end <= index) {
				return null;
			}

			int start = index;
			while (start>=0){
				char c = body.charAt(--start);
				if (c == '<'){
					break;
				}
			}
			
			// fragment
			String frag = body.substring(start, end);
			String link = frag.substring(frag.indexOf("href=") + 6);
			// fin de guillemet
			if (link.indexOf('"') >= 0) {
				link = link.substring(0, link.indexOf('"')).replace("\"", "");
			} else {
				// TODO remove this
				link = link + "";
			}

			link = TextTools.purgeLink(link);

			return link;
		}

		//nothing found
		return null;
	}

	
    public static Date getDateContent(DateFormat dateFormat, Node node){
        String d = node.getTextContent();
        
        Date date = null;
        try {

            date = dateFormat.parse(d);
        } catch (ParseException ex) {
            //Logger.getLogger(RssFeedParser.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return date;
    }
    
    public static Date getDateContent(DateFormat dateFormat, String d){

        Date date = null;
        try {

            date = dateFormat.parse(d);
        } catch (ParseException ex) {
            //Logger.getLogger(RssFeedParser.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return date;
    }

    /**
     * Encode text to MD5
     * @param key
     * @return
     */
    public static String md5Encode(String key) {

        byte[] uniqueKey = key.getBytes();
        byte[] hash = null;

        try {

            hash = MessageDigest.getInstance("MD5").digest(uniqueKey);
        } catch (NoSuchAlgorithmException e) {
            throw new Error("no MD5 support in this VM");
        }

        StringBuffer hashString = new StringBuffer();

        for (int i = 0; i < hash.length; ++i) {
            String hex = Integer.toHexString(hash[i]);
            if (hex.length() == 1) {
                hashString.append('0');
                hashString.append(hex.charAt(hex.length() - 1));
            } else {
                hashString.append(hex.substring(hex.length() - 2));
            }

        }

        return hashString.toString();

    }

}
