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
package org.sonatype.nexus.crowd.plugin.internal;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atlassian.crowd.service.client.ClientProperties;
import com.atlassian.crowd.service.client.ClientPropertiesImpl;

public class DefaultCrowdPluginConfiguration implements CrowdPluginConfiguration {

	private static final String CONFIG_FILE = "crowd.properties";

	private final Logger logger = LoggerFactory.getLogger(DefaultCrowdPluginConfiguration.class);

	private Path crowdConfigFile;
	private ClientProperties configuration;
	private ReentrantLock lock = new ReentrantLock();

	public DefaultCrowdPluginConfiguration() {
		crowdConfigFile = Paths.get("./etc/" + CONFIG_FILE);
	}

	@Override
	public ClientProperties getConfiguration() {
		if (configuration != null) {
			return configuration;
		}

		lock.lock();
		Properties p = new Properties();
		try {
			p.load(Files.newInputStream(crowdConfigFile));
			configuration = ClientPropertiesImpl.newInstanceFromProperties(p);

		} catch (IOException e) {
			logger.error("Error reading crowd properties",e);
		}
		lock.unlock();
		return configuration;
	}
}
