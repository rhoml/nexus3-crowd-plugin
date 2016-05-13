Nexus3 Crowd Plugin
==================

This plugin adds a Crowd realm to Sonatype Nexus and enables you to authenticate with Crowd Users and authorize with crowd roles.

It works with Nexus 3.x and Crowd 2.x

This is a fork of http://patrickroumanoff.github.io/nexus-crowd-plugin/

<a href='http://ci.pingunaut.com/job/nexus3-crowd-plugin/'><img src='http://ci.pingunaut.com/buildStatus/icon?job=nexus3-crowd-plugin'></a>

Usage
-
1. Get the plugin 
  There's no downloadable jar hosted at the moment, so you have to clone and build this repo
  ```
  git clone https://github.com/pingunaut/nexus3-crowd-plugin.git
  cd nexus3-crowd-plugin
  mvn install
  ```
  
2. Start nexus with console
  Move into your nexus installation folder. Edit the file bin/nexus.vmoptions to contain the following line
  ```
  -Dkaraf.startLocalConsole=true
  ```
  After that (re-)start nexus. It will then startup with an interactive console enabled. (If the console doesn't show up, you may hit the Enter key after startup).
  Your console should look like this afterwards:
  ```
  karaf@root()> 
  ```
  
3. Install plugin bundle
  Within the console just type
  ```
  bundle:install -s file://[ABSOLUTE_PATH_TO_YOUR_JAR]
  ```
  
4. Configure the plugin
  After installing the Crowd realm should show up in the realm administration and you can activate it like shown below:
  <img src='https://sec.pingunaut.com/wp-content/uploads/2016/05/nexus_crowd.png'>

  A good starting point is mapping one crowd role to a nexus administrator role.
  
  
Development
-

//TODO
