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
Integration tests are located within tests folder and can be run using the command `mvn verify`. 

To override the default properties used in the integration tests for your own environment use a command like this..

```
mvn verify -Dfacade.test.properties=file:{pathToFacadePropertiesFile} -proxy.test.properties=file:{pathToProxyPropertiesFile} etc
```

## Using
A 1.0 OINK release is not possible until all of its dependancies are also officially released. In the meantime you can build and run OINK in its current 0.x version.

### Current Dependancies
- [FHIR Java Implementation](http://www.hl7.org/implement/standards/fhir/downloads.html) - Latest snapshot manually installed to local Maven repo. Alternatively configure your local repo to scan the Sonatype Snapshots Repo

```
		<dependency>
			<groupId>me.fhir</groupId>
			<artifactId>fhir-0.81</artifactId>
			<version>${fhir.version}</version>
		</dependency>
```

NB. Properties ${} are set in parent POM


### How to use OINK
See the [wiki](https://openeyes.atlassian.net/wiki/display/OINK/Using+OINK)


## Troubleshooting & Useful Tools
- [Report a bug](https://openeyes.atlassian.net/secure/Dashboard.jspa)
- [Read Documentation](https://openeyes.atlassian.net/wiki/dashboard.action)

## Contributing Changes
- Fork this repo, make changes and submit pull requests.