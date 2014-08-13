#OINK
The OpenEyes Integration Toolkit for connecting an OpenEyes instance to other healthcare information systems.

## Building
```
mvn clean install
```
A distributable custom Karaf container containing OINK is available under the *platforms/karaf/distro* module. 


##Testing
OpenEyes has a CI Jenkins server that monitors the health of this repository.

**Develop Branch**: [![Build Status](http://ci.openeyes.org.uk/jenkins/buildStatus/icon?job=OpenEyes OINK - Develop)](http://ci.openeyes.org.uk/jenkins/job/OpenEyes%20OINK%20-%20Develop/)

### Unit Tests
```
mvn test
```
Note some unit tests will be ignored if a RabbitMQ broker is not detected.

### Integration Tests
Integration tests are located within tests folder and can be run specifically using the command `mvn verify testing` or by doing a standard `mvn clean install -Ptesting`. Integration tests significantly extend build time.


## Runtime Configuration
Each adapter loads settings inside OSGi through the [Configuration Admin Service](http://felix.apache.org/documentation/subprojects/apache-felix-config-admin.html) through the following PIDs

* uk.org.openeyes.oink.proxy   (for Proxy Adapter)
* uk.org.openeyes.oink.facade  (for Facade Adapter)
* uk.org.openeyes.oink.hl7v2   (for Hl7v2 Adapter)

See the README for each adapter for more configuration instructions.

### Notable Dependancies
- [FHIR Java Implementation](http://www.hl7.org/implement/standards/fhir/downloads.html)
- [Vagrant](vagrantup.com) (for running Integration Tests)


### How to use OINK
See the [wiki](https://openeyes.atlassian.net/wiki/display/OINK/Using+OINK)


## Troubleshooting & Useful Tools
- [Report a bug](https://openeyes.atlassian.net/secure/Dashboard.jspa)
- [Read Documentation](https://openeyes.atlassian.net/wiki/dashboard.action)

## Contributing Changes
- Fork this repo, make changes and submit pull requests.
