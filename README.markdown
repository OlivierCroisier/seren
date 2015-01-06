Seren : the Java Serialization Enhancer
=======================================

Seren (SERialization ENhancer) aims to enhance your classes so that they are much quicker to serialize. 
It does so by instrumenting the classes at load-time to generate optimized writeObject / readObject methods, based on
some best practices seen in the JavaSpecialist Master Course.

Which classes should be enhanced is determined by a "filter", which is configured in the "seren.properties"
configuration file (see below).

In each selected class, Seren will detect and optimize all non-static, non-transient fields (as the standard
serialization system does).


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

A class filter is defined by a logical name, and an optional set of configuration properties specific to this filter.

All this configuration takes place in the "seren.properties" file, which must be placed at the root of the classpath.
You can configure several filters in this file; only the one specified by the "seren.filter" property will be used.

The syntax is easy :

```properties
seren.filter=<filterId>

filter.<filterId>=com.company.filterClass
filter.<filterId>.property1=value1
filter.<filterId>.property2=value2
...
```

For example :

```properties
// Configure which filter will be used
seren.filter=filterByPackageList

// The selected filter's configuration
filter.filterByPackageList=net.thecodersbreakfast.seren.filter.PackageListFilter
filter.filterByPackageList.packages=com.company.pkg1, com.company.package2

// Another filter configuration - won't be used
filter.filterByPackagePattern=net.thecodersbreakfast.seren.filter.PackagePatternFilter
filter.filterByPackagePattern.pattern=^com\\.company\\.(.*)\\.model
```

One othe configuration option is the "seren.verbose" parameter, which can be set to true or false (default). In verbose
mode, both the filter and the transformer print extra information on the standart output stream (console).

```properties
seren.verbose=true
```        

Running Seren
==============

To run your application with Seren, just add the following option to the command line. Also, make sure the Javassist
library (javassist.jar) is available in the classpath.

    -javaagent:<path/to/seren.jar>

For example :

    java -cp <classpath> -javaagent:/home/olivier/seren.jar


Developing a custom class filter
================================

Class filters must implement the net.thecodersbreakfast.seren.filter.ClassFilter interface, that defines two methods :

```java
    public void configure(Map<String,String> config) throws Exception;
    public boolean acceptClass(ClassLoader classLoader, CtClass classDefinition) throws Exception;
```

The "configure" method is called after the Filter is instanciated. The Map passed as a parameter contains the
filter's properties defined in the configuration file; its keys are the names of the properties related to this
particular filter, minus the filter's prefix (filter.<filterId>).

The "acceptClass" method is then called for each loaded class. It's up to you to use the provided class definition
(a Javassist CtClass instance) to determine if it should be instrumented.
It is recommended to used BaseClassFilter (net.thecodersbreakfast.seren.filter.BaseClassFilter) as a superclass for
all filters, as it provides utility methods and performs some basic checks, such as verifying if the class is
actually a class (not an enum, interface, etc.) and if it implements Serializable.

As an example, below is the code of the PackageListFilter filter :

```java
    public class PackageListFilter extends BaseClassFilter {

        private Set<String> packages = new HashSet<String>();

        @Override
        public void configure(Map<String, String> config) {
            String packageNames = config.get("packages");
            packages.addAll(Arrays.asList(packageNames.split(",\\s*")));
        }

        @Override
        public boolean acceptClass(ClassLoader classLoader, CtClass classDefinition) throws Exception {
            return super.acceptClass(classLoader, classDefinition) && packages.contains(classDefinition.getPackageName());
        }
    }
```

Licence & Contact info
======================

This library is licenced under the 3-clause BSD Licence (see the attached LICENCE file).

IANAL, but this means (roughly) that you can freely use SEREN in your personal or commercial product, in source or
binary form, provided you distribute the unmodified licence file with it, make clear I am the original author,
and do not use my name or the library name to promote your own products.
Oh, and if it explodes in production, I don't have anything to do with it :)

For any question, please contact me at olivier(@)thecodersbreakfast.net
