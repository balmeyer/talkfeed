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

import java.util.HashMap;
import java.util.Map;

import net.sf.jsr107cache.Cache;
import net.sf.jsr107cache.CacheException;
import net.sf.jsr107cache.CacheFactory;
import net.sf.jsr107cache.CacheManager;

import com.google.appengine.api.memcache.stdimpl.GCacheFactory;

/**
 * Implements Google Caching
 * @author JBVovau
 *
 */
public class CacheService {
	
	/** No instance */
	private CacheService(){}
	
	/**
	 * Returns object in cache
	 * @param key
	 * @return
	 */
	public static Object get(Object key){
		Cache cache = getCache();
		
		if (cache != null) return cache.get(key);
		
		return null;
	}
	
	/**
	 * Save object in cache
	 * @param key
	 * @param value
	 */
	public static void put(Object key, Object value){
		Cache cache = getCache();
		
		if (cache != null) cache.put(key, value);
	}
	
	public static void remove(Object key){
		Cache cache = getCache();
		if (cache != null) cache.remove(key);
	}
	
	/**
	 * returns current cache
	 * @return
	 */
	private static Cache getCache(){
		//find in cache first
        Cache cache = null;
        
        //configs
        Map<Object,Object> props = new HashMap<Object,Object>();
        props.put(GCacheFactory.EXPIRATION_DELTA, 3600 * 12); //12 hours
        
        CacheFactory cacheFactory = null ;
		try {
			cacheFactory = CacheManager.getInstance().getCacheFactory();
			cache = cacheFactory.createCache(props);
		} catch (CacheException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		

		
		return cache;
	}
}
