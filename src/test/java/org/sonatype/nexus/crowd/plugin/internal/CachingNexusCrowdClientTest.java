package org.sonatype.nexus.crowd.plugin.internal;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.apache.shiro.authc.UsernamePasswordToken;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;
import org.sonatype.nexus.crowd.plugin.CrowdPluginConfiguration;
import org.sonatype.nexus.security.role.Role;

import com.atlassian.crowd.embedded.api.PasswordCredential;
import com.atlassian.crowd.exception.ApplicationAccessDeniedException;
import com.atlassian.crowd.exception.ApplicationPermissionException;
import com.atlassian.crowd.exception.ExpiredCredentialException;
import com.atlassian.crowd.exception.InactiveAccountException;
import com.atlassian.crowd.exception.InvalidAuthenticationException;
import com.atlassian.crowd.exception.OperationFailedException;
import com.atlassian.crowd.integration.rest.entity.GroupEntity;
import com.atlassian.crowd.model.authentication.UserAuthenticationContext;
import com.atlassian.crowd.model.group.Group;
import com.atlassian.crowd.model.group.GroupType;
import com.atlassian.crowd.search.query.entity.restriction.NullRestrictionImpl;
import com.atlassian.crowd.service.client.CrowdClient;

public class CachingNexusCrowdClientTest {

	private CachingNexusCrowdClient c;
	private CacheProvider mockedCache;
	private CrowdPluginConfiguration mockedConfig;
	private CrowdClient mockedClient;

	@Before
	public void setupTest() {
		mockedCache = Mockito.mock(CacheProvider.class);
		mockedConfig = Mockito.mock(CrowdPluginConfiguration.class);
		mockedClient = Mockito.mock(CrowdClient.class);
		c = new CachingNexusCrowdClient(mockedConfig, mockedCache) {
			@Override
			protected CrowdClient getClient() {
				return mockedClient;
			}
		};
	}

	@Test
	public void testAuthenticateNoCacheNoCrowd()
			throws InactiveAccountException, ExpiredCredentialException, ApplicationPermissionException,
			InvalidAuthenticationException, OperationFailedException, ApplicationAccessDeniedException {
		UsernamePasswordToken token = new UsernamePasswordToken("u1", new char[] { 'p', '1' });
		Mockito.when(mockedCache.getToken("u1")).thenReturn(Optional.empty());
		boolean a = c.authenticate(token);
		Mockito.verify(mockedClient).authenticateSSOUser(Mockito.any());
		Assert.assertFalse(a);
	}

	@Test
	public void testAuthenticateNoCacheCrowdOK()
			throws InactiveAccountException, ExpiredCredentialException, ApplicationPermissionException,
			InvalidAuthenticationException, OperationFailedException, ApplicationAccessDeniedException {
		UsernamePasswordToken token = new UsernamePasswordToken("u1", new char[] { 'p', '1' });
		Mockito.when(mockedCache.getToken("u1")).thenReturn(Optional.empty());
		UserAuthenticationContext userAuthCtx = new UserAuthenticationContext();
		userAuthCtx.setName(token.getUsername());
		userAuthCtx.setCredential(new PasswordCredential(new String(token.getPassword())));
		Mockito.when(mockedClient.authenticateSSOUser(userAuthCtx)).thenReturn("tok123");
		boolean a = c.authenticate(token);
		Mockito.verify(mockedCache).putToken("u1", "tok123");
		Assert.assertTrue(a);
	}

	@Test
	public void testAuthenticateCacheOK()
			throws InactiveAccountException, ExpiredCredentialException, ApplicationPermissionException,
			InvalidAuthenticationException, OperationFailedException, ApplicationAccessDeniedException {
		UsernamePasswordToken token = new UsernamePasswordToken("u1", new char[] { 'p', '1' });
		Mockito.when(mockedCache.getToken("u1")).thenReturn(Optional.of("tok123"));
		boolean a = c.authenticate(token);
		Mockito.verify(mockedClient, Mockito.never()).authenticateSSOUser(Mockito.any());
		Assert.assertTrue(a);
	}

	@Ignore
	@Test
	public void testFindRolesByUser() {
		fail("Not yet implemented");
	}

	@Ignore
	@Test
	public void testFindUserByUsername() {
		fail("Not yet implemented");
	}

	@Ignore
	@Test
	public void testFindRoleByRoleId() {
		fail("Not yet implemented");
	}
	
	@Ignore
	@Test
	public void testFindAllUsernames() {
		fail("Not yet implemented");
	}

	@Ignore
	@Test
	public void testFindUsers() {
		fail("Not yet implemented");
	}

	@Ignore
	@Test
	public void testFindUserByCriteria() {
		fail("Not yet implemented");
	}

	@Ignore
	@Test
	public void testFindRoles()
			throws OperationFailedException, InvalidAuthenticationException, ApplicationPermissionException {
		Mockito.when(mockedClient.searchGroups(NullRestrictionImpl.INSTANCE, 0, 32000)).thenReturn(fakeGroups());
		Set<Role> roles = c.findRoles();
		Set<Role> fakeRoles = fakeRoles();
		Assert.assertEquals(2, roles.size());
		fakeRoles.iterator().forEachRemaining(r -> {
			Assert.assertTrue(roles.stream().map(Role::getRoleId).anyMatch(role->role.equals(r.getRoleId())));
		});
	}

	private Set<Role> fakeRoles() {
		Set<Role> roles = new HashSet<>();
		Role r1 = new Role("r1", "r1", "d1", CrowdUserManager.SOURCE, true, Collections.emptySet(), Collections.emptySet());
		Role r2 = new Role("r2", "r1", "d1", CrowdUserManager.SOURCE, true, Collections.emptySet(), Collections.emptySet());
		roles.add(r1);
		roles.add(r2);
		return roles;
	}

	private List<Group> fakeGroups() {
		List<Group> groups = new ArrayList<>();
		Group g1 = new GroupEntity("r1", "d1", GroupType.GROUP, true);
		Group g2 = new GroupEntity("r2", "d2", GroupType.GROUP, true);
		groups.add(g1);
		groups.add(g2);
		return groups;
	}

}
