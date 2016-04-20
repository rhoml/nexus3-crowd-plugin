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
package org.sonatype.nexus.crowd.plugin.internal;

import java.net.URISyntaxException;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.nexus.security.role.Role;
import org.sonatype.nexus.security.user.User;

import com.atlassian.crowd.exception.ApplicationPermissionException;
import com.atlassian.crowd.exception.InvalidAuthenticationException;
import com.atlassian.crowd.exception.OperationFailedException;
import com.atlassian.crowd.exception.UserNotFoundException;
import com.atlassian.crowd.service.client.ClientProperties;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

/**
 * @author Issa Gorissen
 */
public class CachingRestClient extends RestClient {

	private static final String REST_RESPONSE_CACHE = "com.atlassian.crowd.restresponse.cache";
	private static final Logger LOG = LoggerFactory.getLogger(CachingRestClient.class);

	private CacheManager ehCacheManager;

	public CachingRestClient(ClientProperties config) throws URISyntaxException {
		super(config);

		ehCacheManager = CacheManager.getInstance();
		// create a cache with max items = 10000 and TTL (live and idle) = 1 hour
		Cache cache = new Cache(REST_RESPONSE_CACHE, 10000, false, false, 3600, 3600);
		ehCacheManager.addCache(cache);
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<String> getNestedGroups(String username)  {
		Cache cache = getCache();
		String key = "nestedgroups" + username;
		Element elem = cache.get(key);
		if (elem != null) {
			LOG.debug("getNestedGroups({}) from cache", username);
			return (List<String>) elem.getObjectValue();
		}

		List<String> groups = super.getNestedGroups(username);
		cache.put(new Element(key, groups));
		return groups;
	}

	@Override
	public User getUser(String userid) throws UserNotFoundException, OperationFailedException, ApplicationPermissionException, InvalidAuthenticationException {
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
	public Set<Role> getAllGroups() throws OperationFailedException, InvalidAuthenticationException, ApplicationPermissionException  {
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
