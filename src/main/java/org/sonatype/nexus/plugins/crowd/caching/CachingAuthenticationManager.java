/**
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
/**
 * 
 */
package org.sonatype.nexus.plugins.crowd.caching;

import java.rmi.RemoteException;

import com.atlassian.crowd.exception.ExpiredCredentialException;
import com.atlassian.crowd.service.cache.SimpleAuthenticationManager;
import com.atlassian.crowd.service.soap.client.SecurityServerClient;
import com.atlassian.crowd.util.Assert;

/**
 * Implementation of Crowd client's AuthenticationManager which caches tokens
 * from a username/password authentication request.
 * 
 * @author Justin Edelson
 * 
 */
public class CachingAuthenticationManager extends SimpleAuthenticationManager {

    private AuthBasicCache basicCache;

    public CachingAuthenticationManager(SecurityServerClient securityServerClient,
            AuthBasicCache basicCache) {
        super(securityServerClient);
        this.basicCache = basicCache;
    }
  
    @Override
    public String authenticate(String username, String password) throws RemoteException,
            com.atlassian.crowd.exception.InvalidAuthorizationTokenException,
            com.atlassian.crowd.exception.InvalidAuthenticationException,
            com.atlassian.crowd.exception.InactiveAccountException,
            com.atlassian.crowd.exception.ApplicationAccessDeniedException, ExpiredCredentialException {
        Assert.notNull(username);
        Assert.notNull(password);

        String token = basicCache.getToken(username, password);
        if (token == null) {
                token = super.authenticate(username, password);

            basicCache.addOrReplaceToken(username, password, token);
        }
        return token;
    }
 
}
