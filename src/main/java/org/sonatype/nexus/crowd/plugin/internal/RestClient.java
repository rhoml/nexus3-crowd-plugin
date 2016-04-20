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

import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.nexus.security.role.Role;
import org.sonatype.nexus.security.user.User;
import org.sonatype.nexus.security.user.UserStatus;

import com.atlassian.crowd.embedded.api.PasswordCredential;
import com.atlassian.crowd.embedded.api.SearchRestriction;
import com.atlassian.crowd.exception.ApplicationAccessDeniedException;
import com.atlassian.crowd.exception.ApplicationPermissionException;
import com.atlassian.crowd.exception.ExpiredCredentialException;
import com.atlassian.crowd.exception.GroupNotFoundException;
import com.atlassian.crowd.exception.InactiveAccountException;
import com.atlassian.crowd.exception.InvalidAuthenticationException;
import com.atlassian.crowd.exception.InvalidTokenException;
import com.atlassian.crowd.exception.OperationFailedException;
import com.atlassian.crowd.exception.UserNotFoundException;
import com.atlassian.crowd.integration.http.util.CrowdHttpValidationFactorExtractor;
import com.atlassian.crowd.integration.http.util.CrowdHttpValidationFactorExtractorImpl;
import com.atlassian.crowd.integration.rest.service.factory.RestCrowdClientFactory;
import com.atlassian.crowd.model.authentication.CookieConfiguration;
import com.atlassian.crowd.model.authentication.UserAuthenticationContext;
import com.atlassian.crowd.model.authentication.ValidationFactor;
import com.atlassian.crowd.model.group.Group;
import com.atlassian.crowd.search.query.entity.restriction.NullRestrictionImpl;
import com.atlassian.crowd.search.query.entity.restriction.Property;
import com.atlassian.crowd.search.query.entity.restriction.PropertyImpl;
import com.atlassian.crowd.search.query.entity.restriction.TermRestriction;
import com.atlassian.crowd.service.client.ClientProperties;
import com.atlassian.crowd.service.client.CrowdClient;

/**
 * @author Issa Gorissen
 */
public class RestClient {

	private static final Logger LOG = LoggerFactory.getLogger(RestClient.class);

	private CrowdClient client;

	public RestClient(ClientProperties config) throws URISyntaxException {
		client = new RestCrowdClientFactory().newInstance(config);
		CrowdHttpValidationFactorExtractor validationFactorExtractor = CrowdHttpValidationFactorExtractorImpl
				.getInstance();
	}

	/**
	 * Create new session token
	 *
	 * @param username
	 * @param password
	 * @return session token
	 * @throws RemoteException
	 * @throws InvalidTokenException
	 * @throws ApplicationPermissionException
	 * @throws InvalidAuthenticationException
	 * @throws OperationFailedException
	 * @throws ApplicationAccessDeniedException
	 * @throws ExpiredCredentialException
	 * @throws InactiveAccountException
	 */
	public String createSessionToken(String username, String password) throws RemoteException, OperationFailedException, InvalidAuthenticationException, ApplicationPermissionException, InvalidTokenException, InactiveAccountException, ExpiredCredentialException, ApplicationAccessDeniedException {
		LOG.debug("session creation attempt for '{}'", username);
		final CookieConfiguration cookieConfig = client
                .getCookieConfiguration();
        System.out.printf("Cookie Config: %1$s, %2$s\n",
                cookieConfig.getDomain(), cookieConfig.getName());
        UserAuthenticationContext userAuthCtx = new UserAuthenticationContext();
        userAuthCtx.setName(username);
        userAuthCtx.setCredential(new PasswordCredential(password));
        ValidationFactor[] factors = new ValidationFactor[]{
                new ValidationFactor("remote_address", "127.0.0.1")
            };
        userAuthCtx.setValidationFactors(factors);
        final String token = client.authenticateSSOUser(userAuthCtx);
        client.validateSSOAuthentication(
                token, Arrays.asList(factors));


//		com.atlassian.crowd.model.user.User authenticateUser = client.authenticateUser(username, password);
        return token;
	}

	/**
	 * Retrieves the groups that the user is a nested member of
	 *
	 * @param username
	 * @return a set of roles (as strings)
	 * @throws RemoteException
	 * @throws UnsupportedEncodingException
	 */
	public List<String> getNestedGroups(String username) {
		LOG.debug("getNestedGroups({})", username);

		try {
			return client.getNamesOfGroupsForUser(username, 0, 10000);
		} catch (UserNotFoundException | OperationFailedException | InvalidAuthenticationException
				| ApplicationPermissionException e) {
			LOG.error("Error getting nested groups", e);
			return Collections.emptyList();
		}
	}

	/**
	 * Retrieves cookie configurations
	 *
	 * @return a <code>ConfigCookieGetResponse</code>
	 * @throws ApplicationPermissionException
	 * @throws InvalidAuthenticationException
	 * @throws OperationFailedException
	 */
	public CookieConfiguration getCookieConfig()
			throws OperationFailedException, InvalidAuthenticationException, ApplicationPermissionException {
		LOG.debug("ConfigCookieGetResponse getCookieConfig()");
		return client.getCookieConfiguration();
	}

	/**
	 * Get the complete list of active user ids from Crowd
	 *
	 * @return
	 * @throws ApplicationPermissionException
	 * @throws InvalidAuthenticationException
	 * @throws OperationFailedException
	 * @throws RemoteException
	 */
	// XXX: does not seem to be used by Nexus 2.1.2
	public Set<String> getAllUsernames()
			throws OperationFailedException, InvalidAuthenticationException, ApplicationPermissionException {
		LOG.debug("getAllUsernames()");
		return client.searchUserNames(NullRestrictionImpl.INSTANCE, 0, 32000).stream().collect(Collectors.toSet());

	}

	/**
	 * @param userid
	 * @return a <code>org.sonatype.security.usermanagement.User</code> from
	 *         Crowd by a userid
	 * @throws InvalidAuthenticationException
	 * @throws ApplicationPermissionException
	 * @throws OperationFailedException
	 * @throws UserNotFoundException
	 * @throws RemoteException
	 * @throws UnsupportedEncodingException
	 */
	public User getUser(String userid) throws UserNotFoundException, OperationFailedException,
			ApplicationPermissionException, InvalidAuthenticationException {
		LOG.debug("getUser({})", userid);
		return convertUser(client.getUser(userid));
	}

	/**
	 * Returns user list based on multiple criteria
	 *
	 * @param userId
	 * @param email
	 * @param filterGroups
	 * @param maxResults
	 * @return
	 * @throws ApplicationPermissionException
	 * @throws InvalidAuthenticationException
	 * @throws OperationFailedException
	 */
	// XXX: seems Nexus 2.1.2 only search by userId
	// so we make the search in crowd on the userid OR email
	// A Nexus user will be able to make a lookup based on the email
	public Set<User> searchUsers(String userId, String email, Set<String> filterGroups, int maxResults)
			throws OperationFailedException, InvalidAuthenticationException, ApplicationPermissionException {
		LOG.debug("searchUsers({},{},{},{})", userId, email, filterGroups, maxResults);

		Property<String> property = new PropertyImpl<>("userId", String.class);
		SearchRestriction restrictions = new TermRestriction<>(property, userId);
		List<com.atlassian.crowd.model.user.User> searchUsers = client.searchUsers(restrictions, 0, maxResults);
		Set<User> result = searchUsers.stream().map(u -> convertUser(u)).collect(Collectors.toSet());
		return result;

	}

	/**
	 * @param groupName
	 * @return a <code>org.sonatype.security.authorization.Role</code> by its
	 *         name
	 * @throws ApplicationPermissionException
	 * @throws InvalidAuthenticationException
	 * @throws OperationFailedException
	 * @throws GroupNotFoundException
	 * @throws UnsupportedEncodingException
	 */
	// XXX: Nexus 2.1.2 does not seem to use this method
	public Role getGroup(String groupName) throws GroupNotFoundException, OperationFailedException,
			InvalidAuthenticationException, ApplicationPermissionException {
		LOG.debug("getGroup({})", groupName);
		Group group = client.getGroup(groupName);
		return convertGroup(group);
	}

	/**
	 *
	 * @return all the crowd groups
	 * @throws ApplicationPermissionException
	 * @throws InvalidAuthenticationException
	 * @throws OperationFailedException
	 */
	public Set<Role> getAllGroups()
			throws OperationFailedException, InvalidAuthenticationException, ApplicationPermissionException {
		LOG.debug("getAllGroups()");

		SearchRestriction res = NullRestrictionImpl.INSTANCE;
		List<Group> searchGroups = client.searchGroups(res, 0, 32000);

		HashSet<Role> result = new HashSet<Role>();
		for (Group group : searchGroups) {
			result.add(new Role(group.getName(), group.getName(), "", "", true, null, null));
		}

		return result;
	}

	private User convertUser(com.atlassian.crowd.model.user.User in) {
		// TODO: check if there is something like DefaultUser in Nexus 3
		User user = new User();
		user.setUserId(in.getName());
		user.setFirstName(in.getFirstName());
		user.setLastName(in.getLastName());
		user.setEmailAddress(in.getEmailAddress());
		user.setStatus(in.isActive() ? UserStatus.active : UserStatus.disabled);
		return user;
	}

	private Role convertGroup(Group in) {
		Role role = new Role();
		role.setRoleId(in.getName());
		role.setName(in.getName());
		role.setDescription(in.getDescription());
		role.setReadOnly(true);
		return role;
	}

}
