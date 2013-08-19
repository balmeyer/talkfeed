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
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import talkfeed.UserManager;
import talkfeed.data.User;
import talkfeed.utils.TextTools;

import com.google.appengine.api.xmpp.Presence;


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
	public static void setUserPresence(String jid, Presence presence){

		Logger.getLogger("TalkFeedServlet").log(Level.INFO,
				"Presence : " + presence.isAvailable()
		+ ". presence show : " + presence.getPresenceShow()
		+ ". presence type : "+ presence.getPresenceType());
		
		refreshListWithCache();
		
		jid = TextTools.cleanJID(jid);
		
		UserData data = new UserData(jid);
		
		synchronized (KEY_CACHE_PRESENCE) {
			if (presence.isAvailable()){
				if (!users.contains(data)){
					//add users
					users.add(data);
					//check if user exists
					UserManager um = new UserManager();
					User u = um.getOrCreateUser(jid);
					Logger.getLogger("TalkFeedServlet").log(Level.INFO, 
							"New presence : " + jid + " - user db : " + u);
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
	 * Returns true is user present in available list
	 * @param jid
	 * @return
	 */
	public static boolean isUserAvailable(String jid){
		refreshListWithCache();
		
		jid = TextTools.cleanJID(jid);
		
		UserData data = new UserData(jid);
		
		return users.contains(data);
	}
	
	public static Collection<String> listUserByNextUpdate(int max) {
		return listUsers(max, true);
	}
	
	public static Collection<String> listUsers() {
		return listUsers(10000, false);
	}
	
	/**
	 * List user presence
	 * @return
	 */
	private static Collection<String> listUsers(int max , boolean orderByNextUpdate){
		
		refreshListWithCache();
		
		int nb = 0;
		
		Date now = Calendar.getInstance().getTime();
		
		Collection<String> copy = new ArrayList<String>();
		synchronized (KEY_CACHE_PRESENCE) {
			//sort collection by user next update
			Collections.sort(users);
			
			for(UserData data : users){
				//all users
				if (!orderByNextUpdate){
					copy.add(data.jid);
					continue;
				} else {
					if (data.nextUpdate == null || now.after(data.nextUpdate)){
						copy.add(data.jid);
					}
					
				}
				
				if (nb++ >= max) break;
			
			}
		}
		
		Logger.getLogger("UserPresence").log(Level.INFO,
				"Active users : " + nb + ", to update : " + copy.size());
		
		return copy;
	}
	
	/**
	 * Set user update
	 * @param jid
	 */
	public static void setNextUpdate(String jid, int minutes){
		
		jid = TextTools.cleanJID(jid);
		
		if (minutes < 10) minutes = 10;
		
		UserData data = getDataFromJID(jid);
		
		if (data != null) {
			Calendar next = Calendar.getInstance();
			next.add(Calendar.MINUTE, minutes);
			data.nextUpdate = next.getTime();
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
	
	/**
	 * Refresh user list witch cache
	 */
	@SuppressWarnings("unchecked")
	private static void refreshListWithCache(){
		//try cache first
		Object tmp = CacheService.get(KEY_CACHE_PRESENCE);
		if (tmp != null){
			synchronized (KEY_CACHE_PRESENCE) {
				users = (List<UserData>) tmp;
			}
			
		}
	}
	
	private static class UserData implements Serializable,Comparable<UserData>{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		public String jid;
		public Date presenceDate ;
		public Date nextUpdate;
		
		public UserData(String jid){
			this.jid = jid;
			this.presenceDate = Calendar.getInstance().getTime();
			this.nextUpdate = this.presenceDate;
		}
		
		@Override
		public boolean equals(Object obj){
			if (obj == null) return false;
			UserData other = (UserData) obj;
			return this.jid.equals(other.jid);
		}
		
		@Override
		public int hashCode(){
			return this.jid.hashCode();
		}
		
		@Override
		public String toString(){
			return this.jid + "[when:" + this.presenceDate + "]";
		}

		@Override
		public int compareTo(UserData other) {
			if (other == null) return 0;
			if (this.nextUpdate == null) return -1;
			if (other.nextUpdate == null) return 1;
			return this.nextUpdate.compareTo(other.nextUpdate);
		}
	}
	
}
