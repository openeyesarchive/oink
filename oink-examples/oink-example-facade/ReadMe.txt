Facade Example
===================

This example shows how to build an OINK Facade. It can be run using Maven.

This example is built assuming...

Running from cmd line outside OSGi container
============================================

To run the example using Maven type

	mvn:camel:run
	
To stop the example hit ctrl+c

Running inside OSGi container
=============================

You will need to compile and install this example first:
 	
 	mvn compile install

If using Apache Karaf you can install this example from the shell

  features:addurl mvn:uk.org.openeyes.oink/oink-features/<camel version>/xml/features
  features:install camel-example-facade