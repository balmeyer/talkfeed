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

import java.util.HashMap;
import java.util.Map;

import javax.cache.Cache;
import javax.cache.CacheException;
import javax.cache.CacheManager;

import com.google.appengine.api.memcache.stdimpl.GCacheFactory;

/**
 * Implements Google Caching
 * @author JBVovau
 *
 */
public class CacheService {
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
	@SuppressWarnings("unchecked")
	public static void put(Object key, Object value){
		Cache cache = getCache();
		
		if (cache != null) cache.put(key, value);
	}
	
	public static void remove(Object key){
		Cache cache = getCache();
		if (cache != null) cache.remove(key);
	}
	
	private static Cache getCache(){
		//find in cache first
        Cache cache = null;
        Map<Object,Object> props = new HashMap<Object,Object>();
        props.put(GCacheFactory.EXPIRATION_DELTA, 3600 * 4); //4 hours
        try {
			cache = CacheManager.getInstance().getCacheFactory().createCache(props);
		} catch (CacheException e) {
			e.printStackTrace();
		}
		
		return cache;
	}
}
