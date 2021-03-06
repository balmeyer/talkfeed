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

package talkfeed.command;

import java.util.Map;

import talkfeed.BlogManager;

/**
 * Task for removing unused blogs
 * @author JBVovau
 *
 */
@CommandType("purgeblogs")
public class CommandPurgeBlogs implements Command {

	@Override
	public void execute(Map<String, String> args) {
		
		//TODO fix this bug : oldest entries removed => push again
		
		int nbpurge = 100;
		
		if (args.containsKey("nb")){
			nbpurge = Integer.valueOf(args.get("nb"));
		}
		
		BlogManager bm = BlogManager.getInstance();
		
		int nb = bm.removeBlogWithoutSubscription();
		System.out.println(nb + " blog(s) removed");
		
		nb = bm.removeOldestEntries(nbpurge);
		System.out.println(nb + " old entrie(s) removed");

	}

}
