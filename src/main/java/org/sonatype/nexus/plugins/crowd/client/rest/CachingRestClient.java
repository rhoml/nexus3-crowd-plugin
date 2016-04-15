/*
 * This program is licensed to you under the Apache License Version 2.0,
 * and you may not use this file except in compliance with the Apache License Version 2.0.
 * You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Apache License Version 2.0 is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
 */
package org.sonatype.nexus.plugins.crowd.client.rest;

import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.rmi.RemoteException;
import java.util.Set;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.nexus.plugins.crowd.config.model.v1_0_0.Configuration;
import org.sonatype.security.authorization.Role;
import org.sonatype.security.usermanagement.User;

/**
 * @author Issa Gorissen
 */
public class CachingRestClient extends RestClient {
	
	private static final String REST_RESPONSE_CACHE = "com.atlassian.crowd.restresponse.cache";
	private static final Logger LOG = LoggerFactory.getLogger(CachingRestClient.class);
	
	private CacheManager ehCacheManager;

	public CachingRestClient(Configuration config) throws URISyntaxException {
		super(config);
		
		ehCacheManager = CacheManager.getInstance();
		// create a cache with max items = 10000 and TTL (live and idle) = 1 hour
		Cache cache = new Cache(REST_RESPONSE_CACHE, 10000, false, false, config.getCacheTTL(), config.getCacheTTL());
		ehCacheManager.addCache(cache);
	}

	@Override
	@SuppressWarnings("unchecked")
	public Set<String> getNestedGroups(String username) throws RemoteException, UnsupportedEncodingException {
		Cache cache = getCache();
		String key = "nestedgroups" + username;
		Element elem = cache.get(key);
		if (elem != null) {
			LOG.debug("getNestedGroups({}) from cache", username);
			return (Set<String>) elem.getObjectValue();
		}
		
		Set<String> groups = super.getNestedGroups(username);
		cache.put(new Element(key, groups));
		return groups;
	}

	@Override
	public User getUser(String userid) throws RemoteException, UnsupportedEncodingException {
		Cache cache = getCache();
		String key = "user" + userid;
		Element elem = cache.get(key);
		if (elem != null) {
			LOG.debug("getUser({}) from cache", userid);
			return (User) elem.getObjectValue();
		}
		
		User user = super.getUser(userid);
		cache.put(new Element(key, user));
		return user;
	}

	@Override
	@SuppressWarnings("unchecked")
	public Set<Role> getAllGroups() throws RemoteException {
		Cache cache = getCache();
		String key = "allgroups";
		Element elem = cache.get(key);
		if (elem != null) {
			LOG.debug("getAllGroups from cache");
			return (Set<Role>) elem.getObjectValue();
		}
		
		Set<Role> groups = super.getAllGroups();
		cache.put(new Element(key, groups));
		return groups;
	}
	
	private Cache getCache() {
		return ehCacheManager.getCache(REST_RESPONSE_CACHE);
	}
}
