package org.sonatype.nexus.plugins.crowd.client;

import static org.junit.Assert.*;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.junit.Test;


public class DefaultCrowdClientHolderTest {

    @Test
    public void testInitialize() throws InitializationException {
        DefaultCrowdClientHolder cut = new DefaultCrowdClientHolder();
        
        assertTrue(cut != null);
    }

}
