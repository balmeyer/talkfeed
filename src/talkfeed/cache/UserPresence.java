/*
 Copyright 2010/2013 - Jean-Baptiste Balmeyer

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
import java.util.Collection;
import java.util.Date;
import java.util.List;


/**
 * Save user presence
 * @author vovau
 *
 */
public class UserPresence {

	//list of present user
	private static List<UserData> users = new ArrayList<UserData>();
	
	private static final String KEY_CACHE_PRESENCE = "KEY_CACHE_PRESENCE";
	
	private UserPresence(){}
	
	/**
	 * Set user presence
	 * @param jid
	 * @param presence
	 */
	public static void setPresence(String jid, boolean presence){

		refreshListWithCache();
		
		UserData data = new UserData(jid);
		
		synchronized (KEY_CACHE_PRESENCE) {
			if (presence){
				if (!users.contains(data)){
					//add users
					users.add(data);
				}
			} else {
				//not present
				if (users.contains(data)){
					users.remove(data);
				}
			}
			
			//save in cache
			CacheService.put(KEY_CACHE_PRESENCE, users);
		}
	}
	
	/**
	 * List user presence
	 * @return
	 */
	public static Collection<String> listPresence(int max){
		
		refreshListWithCache();
		
		Date now = Calendar.getInstance().getTime();
		
		Collection<String> copy = new ArrayList<String>();
		synchronized (KEY_CACHE_PRESENCE) {
			int nb = 0;
			for(UserData data : users){
				if (nb++ >= max) break;
				
				if (now.after(data.when))
				copy.add(data.jid);
			}
		}
		
		return copy;
	}
	
	/**
	 * Set user update
	 * @param jid
	 */
	public static void setNextUpdate(String jid, int minutes){
		
		if (minutes < 10) minutes = 10;
		
		UserData data = getDataFromJID(jid);
		
		if (data != null) {
			Calendar next = Calendar.getInstance();
			next.add(Calendar.MINUTE, minutes);
			data.when = next.getTime();
		}
	}
	
	/**
	 * Remove user not present
	 * @param jid
	 */
	public static void removeUser(String jid){
		synchronized (KEY_CACHE_PRESENCE) {
			UserData toremove = new UserData(jid);
			users.remove(toremove);
		}
	}
	

	private static UserData getDataFromJID(String jid){
		if(jid == null) return null;
		
		synchronized (KEY_CACHE_PRESENCE) {
			for(UserData data : users){
				if (data.jid.equals(jid)) return data;
			}
		}
		return null;
	}
	
	@SuppressWarnings("unchecked")
	private static void refreshListWithCache(){
		//try cache first
		if (users.size() == 0){
			Object tmp = CacheService.get(KEY_CACHE_PRESENCE);
			if (tmp != null){
				synchronized (KEY_CACHE_PRESENCE) {
					users = (List<UserData>) tmp;
				}
				
			}
		}
	}
	
	private static class UserData implements Serializable{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		public String jid;
		public Date when;
		
		public UserData(String jid){
			this.jid = jid;
			this.when = Calendar.getInstance().getTime();
		}
		
		@Override
		public boolean equals(Object obj){
			if (obj == null) return false;
			UserData other = (UserData) obj;
			return this.jid.equals(other.jid);
		}
		
		@Override
		public String toString(){
			return this.jid + "[when:" + this.when + "]";
		}
	}
	
}
