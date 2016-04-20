/*
 * Copyright (c) 2010 Sonatype, Inc. All rights reserved.
 *
 * This program is licensed to you under the Apache License Version 2.0,
 * and you may not use this file except in compliance with the Apache License Version 2.0.
 * You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Apache License Version 2.0 is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
 */
package org.sonatype.nexus.crowd.plugin;

import java.rmi.RemoteException;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authc.pam.UnsupportedTokenException;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.eclipse.sisu.Description;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.nexus.crowd.plugin.internal.DefaultCrowdClientHolder;
import org.sonatype.nexus.security.config.SecurityConfigurationManager;

import com.atlassian.crowd.exception.ApplicationAccessDeniedException;
import com.atlassian.crowd.exception.ApplicationPermissionException;
import com.atlassian.crowd.exception.ExpiredCredentialException;
import com.atlassian.crowd.exception.InactiveAccountException;
import com.atlassian.crowd.exception.InvalidAuthenticationException;
import com.atlassian.crowd.exception.InvalidTokenException;
import com.atlassian.crowd.exception.OperationFailedException;

@Singleton
@Named(CrowdAuthenticatingRealm.ROLE)
@Description("OSS Crowd Authentication Realm")
public class CrowdAuthenticatingRealm extends AuthorizingRealm {

	public static final String ROLE = "NexusCrowdAuthenticationRealm";
	private static final String DEFAULT_MESSAGE = "Could not retrieve info from Crowd.";
	private static boolean active;

	private DefaultCrowdClientHolder crowdClientHolder;

	private static final Logger logger = LoggerFactory.getLogger(CrowdAuthenticatingRealm.class);

	public static boolean isActive() {
		return active;
	}

	@Inject
	public CrowdAuthenticatingRealm(final SecurityConfigurationManager configuration, final DefaultCrowdClientHolder holder){
		crowdClientHolder = holder;
		System.out.println("#### crowd");
	}

	@Override
	public String getName() {
		return ROLE;
	}

	@Override
	protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authenticationToken)
			throws AuthenticationException {
		if (!(authenticationToken instanceof UsernamePasswordToken)) {
			throw new UnsupportedTokenException("Token of type " + authenticationToken.getClass().getName()
					+ " is not supported. A " + UsernamePasswordToken.class.getName() + " is required.");
		}
		UsernamePasswordToken token = (UsernamePasswordToken) authenticationToken;

		String password = new String(token.getPassword());

		try {
			crowdClientHolder.getAuthenticationManager().authenticate(token.getUsername(), password);
			return new SimpleAuthenticationInfo(token.getPrincipal(), token.getCredentials(), getName());
		} catch (ApplicationAccessDeniedException | InactiveAccountException | ExpiredCredentialException
				| RemoteException | OperationFailedException | InvalidAuthenticationException
				| ApplicationPermissionException | InvalidTokenException e) {
			throw new AuthenticationException(DEFAULT_MESSAGE, e);
		}
	}

	@Override
	protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
		String username = (String) principals.getPrimaryPrincipal();
		try {
			List<String> groups = crowdClientHolder.getRestClient().getNestedGroups(username);
			return new SimpleAuthorizationInfo(groups.stream().collect(Collectors.toSet()));
		} catch (Exception e) {
			throw new AuthorizationException(DEFAULT_MESSAGE, e);
		}
	}

	@Override
	protected void onInit() {
		super.onInit();
		active = true;
		logger.info("Crowd Realm initialized...");
	}

}
