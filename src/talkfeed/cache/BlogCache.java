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

package talkfeed.cache;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Cache managing for blog updates.
 * 
 * @author balmeyer
 * 
 */
public class BlogCache {

	private static final String KEY_CACHE_BLOGS = "KEY_CACHE_BLOGS";

	private static List<BlogData> blogs = new ArrayList<BlogData>();

	private BlogCache() {
	}

	/**
	 * Indicate that a blog is active, regarding subsrived user presence.
	 * 
	 * @param id
	 */
	public static void setBlogIsActive(long id) {

		refresh();

		BlogData data = new BlogData(id);

		synchronized (KEY_CACHE_BLOGS) {
			if (!blogs.contains(data)) {
				blogs.add(data);
			} else {
				for (BlogData mydata : blogs) {
					if (mydata.equals(data)) {
						// set blog is still active
						mydata.last = Calendar.getInstance().getTime();
						break;
					}
				}
			}

			// update cache
			CacheService.put(KEY_CACHE_BLOGS, blogs);

		}
	}

	public static List<Long> getActiveBlogs(int max) {
		refresh();

		List<Long> list = new ArrayList<Long>();
		int nb = 0;
		Date now = Calendar.getInstance().getTime();

		synchronized (KEY_CACHE_BLOGS) {
			for (BlogData data : blogs) {
				if (nb++ > max)
					break;
				if (now.after(data.next)) {
					list.add(data.id);
				}
			}
		}

		return list;
	}

	public static void setNextUpdate(final long id, int nextMinutes) {
		//refresh from cache
		refresh();

		if (nextMinutes < 60) {
			nextMinutes = 60;
		}
		
		//find blog data in cache et put next time
		BlogData data = new BlogData(id);

		synchronized (KEY_CACHE_BLOGS) {
			if (blogs.contains(data)) {
				blogs.remove(data);
			}

			Calendar next = Calendar.getInstance();
			next.add(Calendar.MINUTE, nextMinutes);
			data.next = next.getTime();
			blogs.add(data);

			// update cache
			CacheService.put(KEY_CACHE_BLOGS, blogs);

		}
	}

	@SuppressWarnings("unchecked")
	private static void refresh() {
		synchronized (KEY_CACHE_BLOGS) {
			if (blogs.size() == 0) {
				Object tmp = CacheService.get(KEY_CACHE_BLOGS);
				if (tmp != null) {
					blogs = (List<BlogData>) tmp;
				}
			}

			// purge inactive blog
			Calendar old = Calendar.getInstance();
			old.add(Calendar.HOUR, 24);
			boolean isupdate = false;
			int i = 0;
			while (i < blogs.size()) {
				BlogData data = blogs.get(i);
				if (old.after(data.last)) {
					blogs.remove(i);
					isupdate = true;
				} else {
					i++;
				}
			}

			if (isupdate) {
				// list was changed: update memcache
				CacheService.put(KEY_CACHE_BLOGS, blogs);
			}

		}
	}

	/**
	 * Data blog
	 * 
	 * @author balmeyer
	 * 
	 */
	private static class BlogData implements Serializable {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		public long id;
		// next update
		public Date next;
		// last update
		public Date last;

		public BlogData(long id) {
			this.id = id;
			this.next = Calendar.getInstance().getTime();
			this.last = this.next;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == null)
				return false;
			BlogData other = (BlogData) obj;
			return (this.id == other.id);
		}
	}

}
