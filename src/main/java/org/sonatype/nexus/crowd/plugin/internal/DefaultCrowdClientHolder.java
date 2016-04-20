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
package org.sonatype.nexus.crowd.plugin.internal;

import java.io.Serializable;
import java.net.URISyntaxException;

import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.shiro.ShiroException;

import com.atlassian.crowd.service.client.ClientProperties;

/**
 * Implementation of the CrowdClientHolder which uses caching wherever possible.
 *
 * @author Justin Edelson
 * @author Issa Gorissen
 */
@Singleton
@Named
public class DefaultCrowdClientHolder implements Serializable {

    private boolean configured = false;
    private AuthBasicCache basicCache;
    private ClientProperties configuration;
    private CachingAuthenticationManager authManager;
    private RestClient restClient;

    private CrowdPluginConfiguration crowdPluginConfiguration = new DefaultCrowdPluginConfiguration();

	public boolean isConfigured() {
        return configured;
    }

	public CachingAuthenticationManager getAuthenticationManager() {
    	return authManager;
    }

	public RestClient getRestClient() {
    	return restClient;
    }

	public DefaultCrowdClientHolder() {
        configuration = crowdPluginConfiguration.getConfiguration();
        if (configuration != null) {
            basicCache = new AuthBasicCacheImpl(60 * configuration.getSessionValidationInterval());
			try {
				restClient = new CachingRestClient(configuration);
			} catch (URISyntaxException use) {
				throw new ShiroException("Rest client init failed", use);
			}
            authManager = new CachingAuthenticationManager(restClient, basicCache);
            configured = true;
        }
	}
}
