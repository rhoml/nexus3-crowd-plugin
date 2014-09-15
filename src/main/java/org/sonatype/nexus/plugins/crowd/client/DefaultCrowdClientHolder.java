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
package org.sonatype.nexus.plugins.crowd.client;

import java.net.URISyntaxException;

import javax.enterprise.inject.Typed;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.shiro.ShiroException;
import org.apache.shiro.util.Initializable;
import org.sonatype.nexus.plugins.crowd.caching.AuthBasicCache;
import org.sonatype.nexus.plugins.crowd.caching.AuthBasicCacheImpl;
import org.sonatype.nexus.plugins.crowd.caching.CachingAuthenticationManager;
import org.sonatype.nexus.plugins.crowd.client.rest.CachingRestClient;
import org.sonatype.nexus.plugins.crowd.client.rest.RestClient;
import org.sonatype.nexus.plugins.crowd.config.CrowdPluginConfiguration;
import org.sonatype.nexus.plugins.crowd.config.model.v1_0_0.Configuration;

/**
 * Implementation of the CrowdClientHolder which uses caching wherever possible.
 *
 * @author Justin Edelson
 * @author Issa Gorissen
 */
@Singleton
@Typed(CrowdClientHolder.class)
@Named("default")
public class DefaultCrowdClientHolder implements CrowdClientHolder, Initializable {

    private boolean configured = false;
    private AuthBasicCache basicCache;
    private Configuration configuration;
    private CachingAuthenticationManager authManager;
    private RestClient restClient;

    @Inject
    private CrowdPluginConfiguration crowdPluginConfiguration;

    public boolean isConfigured() {
        return configured;
    }

    public CachingAuthenticationManager getAuthenticationManager() {
    	return authManager;
    }
    
    public RestClient getRestClient() {
    	return restClient;
    }

	@Override
	public void init() throws ShiroException {
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
