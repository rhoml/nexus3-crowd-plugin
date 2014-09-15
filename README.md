Nexus Crowd Plugin [![Build Status](https://travis-ci.org/PatrickRoumanoff/nexus-crowd-plugin.png)](https://travis-ci.org/PatrickRoumanoff/nexus-crowd-plugin)
==================

I have started the work to move the code base to Nexus 3.0-SNAPSHOT, this is a big change, the plugin system is now based on OSGi using Apache Karaf and Felix, all the dependencies changed, and the delivery mechanism as well. there is no more plugin-repository folder , but a deploy fodler - more testing is probably required, but the basics seems to be working. When Nexus 3.0.0 is release I will push a build as well.

This plugin works with Nexus 3.x and Crowd 2.x

This is a fork of the original work done by Sonatype, but
they stopped supporting the oss version and moved it to Nexus Pro, 
if you need a supported version go buy their awesome software.

The crowd integration is using the Crowd REST API - which allows us to ignore all Atlassian dependencies and greatly simplifies the dev process.

The aim of this project is to offer an integration between Nexus and Crowd that
can be installed on Nexus 3.x OSS and offers some basic functionality for advanced features please look up Nexus Pro.

To build the Nexus Plugin bundle, use the maven lifecycle pre-integration-test

Please read on at http://patrickroumanoff.github.io/nexus-crowd-plugin/
