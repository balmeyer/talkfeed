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
package talkfeed.url;

import java.net.MalformedURLException;
import java.net.URL;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import talkfeed.utils.DocumentLoader;

public class BitLyUrlShorten extends CachedUrlShorten{

	private static final String URL = "http://api.bit.ly/v3/shorten";
	
	private static final String API_KEY = "R_00b110cda3420ada525a8ab5de3dd514";
	private static final String API_LOGIN = "talkfeed";
	

	@Override
	public String directShorten(String link){
		
		if (link == null) return null;
		
		//link to bit.ly api
		String postlink = buildUrl(link);
		
		URL u;
		try {
			u = new URL(postlink);
			Document doc = DocumentLoader.load(u);
			
			//parse element
			if (doc != null){
				
				NodeList list = doc.getElementsByTagName("status_code");
				if (list.getLength() > 0){
					//String statusCode = list.item(0).getTextContent();
					NodeList listUrl = doc.getElementsByTagName("url");
					
					if (listUrl.getLength() >0){
						String shortUrl = listUrl.item(0).getTextContent().trim();
						return shortUrl;
					}
				}
				
			}
			
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e){
			//forget exceptions
		}
		
		
		
		return link;
	}
	
	private String buildUrl(String link){
		StringBuilder sb = new StringBuilder(URL);
		sb.append("?login=");
		sb.append(API_LOGIN);
		sb.append("&format=xml&apiKey=");
		sb.append(API_KEY);
		sb.append("&longUrl=");
		sb.append(escapeUrl(link));
		
		
		return sb.toString();
	}
	
	private String escapeUrl(String url){
		
		if (url == null) return null;
		
		String [] escapeThis = {"&", "?", "#"};
		
		StringBuilder s = new StringBuilder(url);
		
		for (String c : escapeThis){
			int start = 0;
			while (start >= 0){
				start = s.indexOf(c, start + 1);
				if (start >= 0){
					s.insert(start++, "/");
				}
			}
		}
		
		return s.toString();
	}
}
