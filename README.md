Nexus Crowd Plugin
==================

This plugin works with Nexus 2.0.x and Crowd 2.4.x
This is a fork of the original work done by sonatype, but as far as I can tell they stopped supporting it after Nexus 1.8, the code got moved quite a bit from svn to github, lost on the way and only the source for Nexus 1.3 is now available from gitHub.

With a bit of research, we can pull the latest available code base for Nexus 1.8 which was taged 1.6.2-SNAPSHOT from the sonatype forge repository thanks to the source artifact, but this later version is missing a test suite.

The crowd integration is using the almost obsolete SOAP interface (REST is now preferred) and this will be removed in a future version of the crowd client library.

The aim of this project is to offer an integration between Nexus and Crowd that can be installed on Nexus 2.x and offers the same level of functionnality than its Nexus 1.8 counter part.

There are some works that need to be done on the crowd part:
- moving to the REST api (instead of SOAP)
- removing the role related code (as crowd will only support groups starting in the next version)

