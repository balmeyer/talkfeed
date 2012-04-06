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

import org.w3c.dom.Document;

/**
* Parse a feed from DOM Document.
* @author Jean-Baptiste Vovau
*/
public interface FeedParser {

   /** put DOM Document in Channel object */
   public Channel parse(Document doc);
   
   
}