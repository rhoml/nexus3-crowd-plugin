package org.sonatype.nexus.crowd.plugin.internal;

import org.junit.Assert;
import org.junit.Test;

public class DefaultCrowdPluginConfigurationTest {

	@Test
	public void testDefaultCrowdPluginConfiguration() {
		CrowdProperties c = new CrowdProperties();
		Assert.assertEquals("nexus", c.getApplicationName());
	}

}
