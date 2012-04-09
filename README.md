Requirements
-------------

The Focal Server requires that participating remote MBeanServers be available over the network and be registered with either
a Jini Lookup Service or have its URL available manually.
The user code must be instrumented according to JMX standards and registered with the exported MBeanServer.
In particular, JMX supports only primitive types (and their wrappers), standard Java collections and JMX specific CompositeData and Tabular-Data.
All attributes and operation parameters must use this limited set of types.

Configuration
-------------

Using Jini Configuration to configure the Focal Server adds the flexibility to fine tune the server, and supports the registration of end-user MBeans and adapters.

Required Steps to use the Focal Server
--------------------------------------

To use the Focal Server:

Start <GigaSpaces Root>\bin\gs-focalserver.bat/.sh.

Wait until the Focal Server finishes scanning the network for all available Jini Lookup Services and registered MBeans.
Open JConsole and connect using the default settings (you can change them in <GigaSpaces Root>\config\tools\focalserver.xml).

    service:jmx:rmi://localhost/jndi/rmi://localhost:1099/rmiConnector

Open a browser (Internet Explorer, Firefox, etc.) and connect to http://localhost:8082.

Performance Impact
------------------

In general JMX is pull-based, meaning whatever the overhead that is needed in order to return the JMX data is only needed when JMX calls for it. 
This usually results from the operator refreshing the JMX frontend screen or periodically pulled by scripts for automated monitoring.
In comparison to the original Java Class they were converted from, use of CompositeData and TabularData is heavier in terms of memory 
usage and network bandwidth usage. Return of too much data as a result should be taken into account.