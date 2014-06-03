#OINK
The OpenEyes Integration Toolkit for connecting an OpenEyes instance to other healthcare information systems.

## Building
```
mvn clean install
```
A distributable custom Karaf container containing OINK is available under the *platforms/karaf/distro* module. 

If your build fails because you do not have an environment capable of running the integration tests (which take a while to run) then alternatively use

```
mvn clean install -Dskip.ITs=true
```

##Testing
OpenEyes has a CI Jenkins server that monitors the health of this repository.

**Develop Branch**: [![Build Status](http://ci.openeyes.org.uk/jenkins/buildStatus/icon?job=OpenEyes OINK - Develop)](http://ci.openeyes.org.uk/jenkins/job/OpenEyes%20OINK%20-%20Develop/)

### Unit Tests
```
mvn test
```

### Integration Tests
Integration tests are located within tests folder and can be run specifically using the command `mvn verify` or by doing a standard `mvn clean install`. Integration tests significantly extend build time.

To override the default properties used in the integration tests for your own environment use a command like this..

```
mvn verify -Dit.facadeToHl7v2.config=pathToFacadePropertiesFile -Dit.proxy.config=pathToProxyPropertiesFile etc
```

The .property files in oink-itest-shared will be used in the absence of custom properties.

At present there are four property files required for the integration tests

* it.proxy.config (rabbitMQ settings, url of proxy target)
* it.hl7v2.config (rabbitMQ settings, hl7v2 server settings)
* it.facadeToHl7v2.config (rabbitMQ settings, facade mappings, facade server settings)
* it.facadeToProxy.config (rabbitMQ settings, facade mappings, facade server settings)

## Runtime Configuration
Each adapter loads settings inside OSGi through the [Configuration Admin Service](http://felix.apache.org/documentation/subprojects/apache-felix-config-admin.html) through the following PIDs

* uk.org.openeyes.oink.proxy   (for Proxy Adapter)
* uk.org.openeyes.oink.facade  (for Facade Adapter)
* uk.org.openeyes.oink.hl7v2   (for Hl7v2 Adapter)

### Notable Dependancies
- [FHIR Java Implementation](http://www.hl7.org/implement/standards/fhir/downloads.html)


### How to use OINK
See the [wiki](https://openeyes.atlassian.net/wiki/display/OINK/Using+OINK)


## Troubleshooting & Useful Tools
- [Report a bug](https://openeyes.atlassian.net/secure/Dashboard.jspa)
- [Read Documentation](https://openeyes.atlassian.net/wiki/dashboard.action)

## Contributing Changes
- Fork this repo, make changes and submit pull requests.