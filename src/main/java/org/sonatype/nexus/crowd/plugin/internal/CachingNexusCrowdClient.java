package org.sonatype.nexus.crowd.plugin.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.nexus.crowd.plugin.CrowdPluginConfiguration;
import org.sonatype.nexus.crowd.plugin.NexusCrowdClient;
import org.sonatype.nexus.security.role.Role;
import org.sonatype.nexus.security.user.User;
import org.sonatype.nexus.security.user.UserSearchCriteria;
import org.sonatype.nexus.security.user.UserStatus;

import com.atlassian.crowd.embedded.api.PasswordCredential;
import com.atlassian.crowd.embedded.api.SearchRestriction;
import com.atlassian.crowd.exception.ApplicationAccessDeniedException;
import com.atlassian.crowd.exception.ApplicationPermissionException;
import com.atlassian.crowd.exception.ExpiredCredentialException;
import com.atlassian.crowd.exception.GroupNotFoundException;
import com.atlassian.crowd.exception.InactiveAccountException;
import com.atlassian.crowd.exception.InvalidAuthenticationException;
import com.atlassian.crowd.exception.OperationFailedException;
import com.atlassian.crowd.exception.UserNotFoundException;
import com.atlassian.crowd.integration.rest.service.factory.RestCrowdClientFactory;
import com.atlassian.crowd.model.authentication.UserAuthenticationContext;
import com.atlassian.crowd.model.group.Group;
import com.atlassian.crowd.search.builder.Combine;
import com.atlassian.crowd.search.builder.Restriction;
import com.atlassian.crowd.search.query.entity.restriction.NullRestrictionImpl;
import com.atlassian.crowd.search.query.entity.restriction.constants.UserTermKeys;
import com.atlassian.crowd.service.client.CrowdClient;
import com.google.inject.Inject;

@Singleton
@Named
public class CachingNexusCrowdClient implements NexusCrowdClient {

	private static final Logger LOGGER = LoggerFactory.getLogger(CachingNexusCrowdClient.class);

	private static final int MAX_RESULTS = 32000;

	private CrowdClient client;
	private CacheProvider cache;
	private CrowdPluginConfiguration config;

	@Inject
	public CachingNexusCrowdClient(CrowdPluginConfiguration config, CacheProvider cache) {
		this.config = config;
		this.cache = cache;
	}

	@Override
	public boolean authenticate(UsernamePasswordToken token) {
		// check if token is cached
		Optional<String> cachedToken = cache.getToken(token.getUsername());
		if (cachedToken.isPresent()) {
			return true;
		}

		// if not, try to authenticate
		try {
			String crowdToken = getClient().authenticateSSOUser(createCrowdUserContext(token));
			if (StringUtils.isNotEmpty(crowdToken)) {
				// if authenticated, put token into cache and return
				cache.putToken(token.getUsername(), crowdToken);
				return true;
			} else {
				return false;
			}
		} catch (InactiveAccountException | ExpiredCredentialException | ApplicationPermissionException
				| InvalidAuthenticationException | OperationFailedException | ApplicationAccessDeniedException e) {
			LOGGER.error("Error while authentication", e);
			return false;
		}
	}

	protected CrowdClient getClient() {
		if (client == null) {
			client = new RestCrowdClientFactory().newInstance(config.getConfiguration());
		}
		return client;
	}

	private UserAuthenticationContext createCrowdUserContext(UsernamePasswordToken token) {
		UserAuthenticationContext userAuthCtx = new UserAuthenticationContext();
		userAuthCtx.setName(token.getUsername());
		userAuthCtx.setCredential(new PasswordCredential(new String(token.getPassword())));
		return userAuthCtx;
	}

	@Override
	public Set<String> findRolesByUser(String username) {
		Optional<Set<String>> cachedGroups = cache.getGroups(username);
		if (cachedGroups.isPresent()) {
			return cachedGroups.get();
		}

		try {
			Set<String> groups = new HashSet<>(getClient().getNamesOfGroupsForUser(username, 0, MAX_RESULTS));
			cache.putGroups(username, groups);
			return groups;
		} catch (UserNotFoundException | OperationFailedException | InvalidAuthenticationException
				| ApplicationPermissionException e) {
			return emptyOnError(e, String.class);
		}
	}

	@Override
	public User findUserByUsername(String username) {
		// TODO cache
		try {
			com.atlassian.crowd.model.user.User crowdUser = getClient().getUser(username);
			return mapUser(crowdUser);
		} catch (UserNotFoundException | OperationFailedException | ApplicationPermissionException
				| InvalidAuthenticationException e) {
			LOGGER.error(String.format("Error while getting user with id %s", username), e);
			return null;
		}
	}

	@Override
	public Role findRoleByRoleId(String roleId) {
		// TODO cache
		try {
			return mapGroup(getClient().getGroup(roleId));
		} catch (GroupNotFoundException | OperationFailedException | InvalidAuthenticationException
				| ApplicationPermissionException e) {
			LOGGER.error(String.format("Error while getting group with id %s", roleId), e);
			return null;
		}
	}

	private Role mapGroup(Group crowdGroup) {
		Role r = new Role();
		r.setRoleId(crowdGroup.getName());
		r.setName(crowdGroup.getName());
		r.setDescription(crowdGroup.getDescription());
		r.setSource(CrowdUserManager.SOURCE);
		r.setReadOnly(true);
		return r;
	}

	private User mapUser(com.atlassian.crowd.model.user.User c) {
		User u = new User();
		u.setEmailAddress(c.getEmailAddress());
		u.setFirstName(c.getFirstName());
		u.setLastName(c.getLastName());
		u.setReadOnly(true);
		u.setStatus(c.isActive() ? UserStatus.active : UserStatus.disabled);
		u.setUserId(c.getName());
		return null;
	}

	@Override
	public Set<String> findAllUsernames() {
		// TODO cache
		try {
			return new HashSet<>(getClient().searchUserNames(NullRestrictionImpl.INSTANCE, 0, MAX_RESULTS));
		} catch (OperationFailedException | InvalidAuthenticationException | ApplicationPermissionException e) {
			return emptyOnError(e, String.class);
		}
	}

	@Override
	public Set<User> findUsers() {
		// TODO cache
		try {
			return getClient().searchUsers(NullRestrictionImpl.INSTANCE, 0, MAX_RESULTS).stream().map(u -> mapUser(u))
					.collect(Collectors.toSet());
		} catch (OperationFailedException | InvalidAuthenticationException | ApplicationPermissionException e) {
			return emptyOnError(e, User.class);
		}
	}

	@Override
	public Set<User> findUserByCriteria(UserSearchCriteria criteria) {
		// TODO cache
		Collection<SearchRestriction> restrictions = new ArrayList<>();
		if (StringUtils.isNotEmpty(criteria.getEmail())) {
			restrictions.add(Restriction.on(UserTermKeys.EMAIL).containing(criteria.getEmail()));
		}
		if (StringUtils.isNotEmpty(criteria.getUserId())) {
			restrictions.add(Restriction.on(UserTermKeys.USERNAME).containing(criteria.getUserId()));
		}

		try {
			List<com.atlassian.crowd.model.user.User> result = getClient().searchUsers(Combine.allOf(restrictions), 0,
					MAX_RESULTS);
			if (CollectionUtils.isNotEmpty(criteria.getOneOfRoleIds())) {
				result.removeIf(userInGroupFilter(criteria.getOneOfRoleIds()));
			}
			return result.stream().map(u -> mapUser(u)).collect(Collectors.toSet());
		} catch (OperationFailedException | InvalidAuthenticationException | ApplicationPermissionException e) {
			return emptyOnError(e, User.class);
		}
	}

	private Predicate<com.atlassian.crowd.model.user.User> userInGroupFilter(final Set<String> filterRoleIds) {
		Predicate<com.atlassian.crowd.model.user.User> filter = new Predicate<com.atlassian.crowd.model.user.User>() {
			@Override
			public boolean test(com.atlassian.crowd.model.user.User t) {
				return !Collections.disjoint(findRolesByUser(t.getName()), filterRoleIds);
			}
		};
		return filter;
	}

	@Override
	public Set<Role> findRoles() {
		// TODO cache
		try {
			List<Group> groups = getClient().searchGroups(NullRestrictionImpl.INSTANCE, 0, MAX_RESULTS);
			return groups.stream().map(g -> mapGroup(g)).collect(Collectors.toSet());
		} catch (OperationFailedException | InvalidAuthenticationException | ApplicationPermissionException e) {
			return emptyOnError(e, Role.class);
		}
	}

	private <T> Set<T> emptyOnError(Exception e, Class<T> type) {
		LOGGER.error("Error while contacting crowd", e);
		return Collections.emptySet();
	}
}
