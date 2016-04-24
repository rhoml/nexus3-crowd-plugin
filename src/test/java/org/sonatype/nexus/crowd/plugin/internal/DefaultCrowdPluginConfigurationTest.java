package org.sonatype.nexus.crowd.plugin.internal;

import org.junit.Assert;
import org.junit.Test;

import com.atlassian.crowd.service.client.ClientProperties;

public class DefaultCrowdPluginConfigurationTest {

	@Test
	public void testDefaultCrowdPluginConfiguration() {
		DefaultCrowdPluginConfiguration c = new DefaultCrowdPluginConfiguration();
		Assert.assertNotNull(c.getCrowdConfigFile());
	}

	@Test
	public void testGetConfiguration() {
		DefaultCrowdPluginConfiguration c = new DefaultCrowdPluginConfiguration();
		ClientProperties configuration = c.getConfiguration();
		Assert.assertNull(configuration);
	}

}
