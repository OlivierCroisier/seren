Seren : the Java Serialization Enhancer
=======================================

Seren (SERialization ENhancer) aims to enhance your classes so that they are much quicker to serialize. 
It does so by instrumenting the classes at load-time to generate optimized writeObject / readObject methods, based on
some best practices seen in the JavaSpecialist Master Course.

Which classes should be enhanced is determined by a configuration file (see below)


Building Seren
==============

To compile the library and package it as a jar :

    mvn clean compile package

To run the integration tests (after the library has been packaged) :

    mvn verify



Configuration
=============

To know which classes to instrument, Seren needs you to configure a "class filter".
Some of the most commonly needed are provided for your convenience (see the javadoc for a list of available filters and
their configuration options), but you can very easily write your own if required.

A class filter is defined by a logical name by which it will referred to on the command line, the name of the class
that implements its algorithm, and an optional set of configuration properties (specific to this filter).

All this configuration takes place in the "seren.properties" file, which must be placed at the root of the classpath.
You can configure several filters in this file; only the one specified on the command-line will be used (more later).

The syntax is easy :

    filterId=com.company.filterClass
    filterId.property1=value1
    filterId.property2=value2
    ...

For example :

    filterByPackageList=net.thecodersbreakfast.seren.filter.PackageListFilter
    filterByPackageList.packages=com.company.pkg1,com.company.package2

    filterByPackagePattern=net.thecodersbreakfast.seren.filter.PackagePatternFilter
    filterByPackagePattern.pattern=^com\\.company\\.(.*)\\.model

    filterByClassPattern=net.thecodersbreakfast.seren.filter.ClassPatternFilter
    filterByClassPattern.pattern=(.*)\\.Pojo


Runnning Seren
==============

Seren uses the Javassist library to perform its bytecode engineering magic, so be sure to put javassist.jar right next
to seren.jar .

Then, to run your application with Seren, just add the following option to the command line :

    -javaagent:<path/to/seren.jar>=<filterId>

For example :

    java -cp <classpath> -javaagent:/home/olivier/seren.jar=filterByPackageList


Developing a custom class filter
================================

Class filters must implement the net.thecodersbreakfast.seren.filter.ClassFilter interface, that defines two methods :

    public void configure(Map<String,String> config);
    public boolean acceptClass(ClassLoader classLoader, CtClass classDefinition);

The "configure" method is called after the Filter is instanciated. The Map passed as a parameter contains the
filter's properties defined in the configuration file; its keys are the names of the properties related to this
particular filter (minus the filter's Id prefix).

The "acceptClass" method is then called for each loaded class. It's up to you to use the provided class definition
(a Javassist CtClass instance) to determine if it should be instrumented.


Contact info
============

For any question, please contact me at olivier(@)thecodersbreakfast.net
