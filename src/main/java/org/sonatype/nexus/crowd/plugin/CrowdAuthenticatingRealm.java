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

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authc.pam.UnsupportedTokenException;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.eclipse.sisu.Description;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.nexus.crowd.plugin.internal.CachingNexusCrowdClient;

@Singleton
@Description("Crowd Authentication Realm")
public class CrowdAuthenticatingRealm extends AuthorizingRealm {

	private static final Logger logger = LoggerFactory.getLogger(CrowdAuthenticatingRealm.class);
	public static String NAME = CrowdAuthenticatingRealm.class.getName();

	private CachingNexusCrowdClient client;

	@Inject
	public CrowdAuthenticatingRealm(final CachingNexusCrowdClient crowdClientHolder) {
		this.client = crowdClientHolder;
	}

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	protected void onInit() {
		super.onInit();
		logger.info("Crowd Realm initialized...");
	}

	@Override
	protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
		String username = (String) principals.getPrimaryPrincipal();
		return new SimpleAuthorizationInfo(client.findRolesByUser(username));
	}

	@Override
	protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
		if (!(token instanceof UsernamePasswordToken)) {
			throw new UnsupportedTokenException(String.format("Token of type %s  is not supported. A %s  is required.",
					token.getClass().getName(), UsernamePasswordToken.class.getName()));
		}

		UsernamePasswordToken t = (UsernamePasswordToken) token;
		boolean authenticated = client.authenticate(t);
		if (authenticated) {
			return createSimpleAuthInfo(t);
		} else {
			return null;
		}
	}

	private SimpleAuthenticationInfo createSimpleAuthInfo(UsernamePasswordToken token) {
		return new SimpleAuthenticationInfo(token.getPrincipal(), token.getCredentials(), NAME);
	}

}
