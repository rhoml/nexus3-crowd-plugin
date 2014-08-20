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
package org.sonatype.nexus.plugins.crowd.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.concurrent.locks.ReentrantLock;

import javax.enterprise.inject.Typed;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.nexus.configuration.application.ApplicationDirectories;
import org.sonatype.nexus.plugins.crowd.config.model.v1_0_0.Configuration;
import org.sonatype.nexus.plugins.crowd.config.model.v1_0_0.io.xpp3.NexusCrowdPluginConfigurationXpp3Reader;
import org.sonatype.sisu.goodies.eventbus.internal.DefaultEventBus;
import org.sonatype.sisu.goodies.eventbus.internal.guava.EventBus;

@Singleton
@Named
@Typed(CrowdPluginConfiguration.class)
public class DefaultCrowdPluginConfiguration extends DefaultEventBus implements
        CrowdPluginConfiguration {

	private final Logger logger = LoggerFactory.getLogger(DefaultCrowdPluginConfiguration.class);

	private File crowdConfigFile;
	private Configuration configuration;
    private ReentrantLock lock = new ReentrantLock();

	@Inject
	public DefaultCrowdPluginConfiguration(EventBus eventBus, ApplicationDirectories applicationDirectories) {
		super(eventBus);
		
		crowdConfigFile = new File(applicationDirectories.getWorkDirectory(), "conf/crowd-plugin.xml");
	}

	
    public Configuration getConfiguration() {
        if (configuration != null) {
            return configuration;
        }

        lock.lock();

        FileInputStream is = null;

        try {
            is = new FileInputStream(crowdConfigFile);

            NexusCrowdPluginConfigurationXpp3Reader reader = new NexusCrowdPluginConfigurationXpp3Reader();

            configuration = reader.read(is);
        } catch (FileNotFoundException e) {
            logger.error("Crowd configuration file does not exist: {}", crowdConfigFile.getAbsolutePath());
        } catch (IOException e) {
        	logger.error("IOException while retrieving configuration file", e);
        } catch (XmlPullParserException e) {
        	logger.error("Invalid XML Configuration", e);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    // just closing if open
                }
            }

            lock.unlock();
        }

        return configuration;
    }

}
