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
package org.sonatype.nexus.crowd.plugin.internal;

import java.rmi.RemoteException;

import com.atlassian.crowd.exception.ApplicationAccessDeniedException;
import com.atlassian.crowd.exception.ApplicationPermissionException;
import com.atlassian.crowd.exception.ExpiredCredentialException;
import com.atlassian.crowd.exception.InactiveAccountException;
import com.atlassian.crowd.exception.InvalidAuthenticationException;
import com.atlassian.crowd.exception.InvalidTokenException;
import com.atlassian.crowd.exception.OperationFailedException;

/**
 * Implementation of Crowd client's AuthenticationManager which caches tokens
 * from a username/password authentication request.
 *
 * @author Justin Edelson
 * @author Issa Gorissen
 *
 */
public class CachingAuthenticationManager {

    private AuthBasicCache basicCache;
    private RestClient restClient;

    public CachingAuthenticationManager(RestClient restClient, AuthBasicCache basicCache) {
    	this.restClient = restClient;
        this.basicCache = basicCache;
    }

    public String authenticate(String username, String password) throws RemoteException, InactiveAccountException, ExpiredCredentialException, OperationFailedException, InvalidAuthenticationException, ApplicationPermissionException, InvalidTokenException, ApplicationAccessDeniedException {
        assert username != null;
        assert password != null;

        String token = basicCache.getToken(username, password);
        if (token == null) {
            token = restClient.createSessionToken(username, password);

            basicCache.addOrReplaceToken(username, password, token);
        }
        return token;
    }

}
