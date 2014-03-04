#OINK
The OpenEyes Integration Toolkit for connecting an OpenEyes instance to other healthcare information systems.

##Testing
OpenEyes has a CI Jenkins server that monitors the health of this repository.

**Develop Branch**: [![Build Status](http://ci.openeyes.org.uk/jenkins/buildStatus/icon?job=OpenEyes OINK - Develop)](http://ci.openeyes.org.uk/jenkins/job/OpenEyes%20OINK%20-%20Develop/)

### Unit Tests
```
mvn test
```

### Integration Tests
Integration tests are located within oink-samples-integration and can be run using the command `mvn verify`. By default the configurable web apps used in the integration tests will use the properties files in src/main/resources of oink-samples-integration. This behaviour can be overwritten by specifying alternative properties locations like so:

```
mvn verify -Dfacade.it.properties=file:/foo.properties -Dsilverlink.it.properties=file:/bar.properties
```

## Using
An official OINK release is not possible until all of its dependancies are also officially released. In the meantime you can build and run OINK in its current SNAPSHOT version.

### Current Dependancies
- FHIR Java Implementation - Latest snapshot manually installed to local Maven repo

```
		<dependency>
			<groupId>org.fhir</groupId>
			<artifactId>fhir</artifactId>
			<version>${fhir.version}</version>
		</dependency>
```
- OpenMaps - Latest snapshot from the oink-refactoring branch installed to local Maven repo

```
		<dependency>
			<groupId>com.openMap1.mapper</groupId>
			<artifactId>openmap-mapper-fhir-webapp</artifactId>
			<version>${openmap.version}</version>
			<type>war</type>
		</dependency>
```


### How to use OINK
TBD

## Deploying

### OINK Web Applications and Environment Properties
The Oink Facade Sample Web Application can will read an external .properties file if the Java System Property `oink.facade.properties` value is set. The same goes for the Oink Silverlink Sample Web Application which can read an external .properties file if the Java System Property `oink.silverlink.properties` is set.

### How to deploy
Start with a sample web application from the oink-samples folder, modify as you see fit, package as a war and deploy to a Web Application Container such as Tomcat or Jetty.

## Troubleshooting & Useful Tools
- [Report a bug](https://openeyes.atlassian.net/secure/Dashboard.jspa)
- [Read Documentation](https://openeyes.atlassian.net/wiki/dashboard.action)

## Contributing Changes