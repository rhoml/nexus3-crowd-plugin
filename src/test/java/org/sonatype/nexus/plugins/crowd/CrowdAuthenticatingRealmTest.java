/**
 * Copyright (c) 2009 Sonatype, Inc. All rights reserved.
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
package org.sonatype.nexus.plugins.crowd;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.sonatype.nexus.plugins.crowd.CrowdAuthenticatingRealm;

public class CrowdAuthenticatingRealmTest {

    private CrowdAuthenticatingRealm realm;

    @Before
    public void setup() {
        realm = new CrowdAuthenticatingRealm();

    }

    @Test
    public void checkActiveFlag() throws Exception {
        assertFalse(CrowdAuthenticatingRealm.isActive());
        realm.initialize();
        assertTrue(CrowdAuthenticatingRealm.isActive());
    }

    @After
    public void teardown() {
        realm.dispose();
    }

}
