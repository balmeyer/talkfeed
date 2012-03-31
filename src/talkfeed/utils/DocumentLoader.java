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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * 
 * Load Documents from url
 * 
 * @author Jean-Baptiste Vovau
 *
 */
public class DocumentLoader {

    private static DocumentBuilderFactory dbf;
    private static DocumentBuilder db;
    
    static {
        try {
            dbf = DocumentBuilderFactory.newInstance();
            db = dbf.newDocumentBuilder();
        } catch (ParserConfigurationException pce) {
            pce.printStackTrace();
        }
    }

    /** Load XML document */
    public static Document load(URL url) {
        try {
            //File file = new File(path);
            Document doc = null;
            InputStream istr = url.openStream();

            doc = db.parse(istr);
            
            
            return doc;
        } catch (SAXException ex) {
            //Logger.getLogger(DocumentLoader.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            //erreur connexion
            //Logger.getLogger(DocumentLoader.class.getName()).log(Level.SEVERE, null, ex);
            //throw new ConnectionException();
        } catch (NullPointerException npex){
            //npex.printStackTrace(); //
        } catch (java.lang.IllegalArgumentException illl){
            
        }

        return null;
    }

	/**
	 * Load web page
	 * 
	 * @param path
	 * @return
	 */
	public static String loadPage(String path) {

		if (path == null) return null;
		
		
		if (!path.toLowerCase().startsWith("http")){
			path = "http://" + path;
		}
		
		try {
			// site url
			URL url = new URL(path);
			// open inputstream
			InputStream is = null;
			try {
				
				is = url.openStream();
				
			} catch (NullPointerException npex) {
				// TODO erreur de socket

				return null;
			} catch (java.lang.IllegalArgumentException ia) {
				// TODO manage error
				return null;
			}
			InputStreamReader isReader = new InputStreamReader(is);
			BufferedReader reader = new BufferedReader(isReader);
			
			StringBuffer buffer = new StringBuffer();
			do {
				String line = reader.readLine();
				if (line == null)
					break;
				else {
					buffer.append(line);
					buffer.append('\n');
				}
				// test
				if (buffer.length() > 8000) {
					break;
				}
			} while (true);

			return buffer.toString();

		} catch (MalformedURLException ex) {
			// Logger.getLogger(NetLoader.class.getName()).log(Level.SEVERE,
			// null, ex);
		} catch (IOException ioex) {
			// Logger.getLogger(NetLoader.class.getName()).log(Level.SEVERE,
			// null, ioex);
		}

		return null;
	}

    
    
}
