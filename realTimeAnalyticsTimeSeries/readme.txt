Creates a basic SBA application with two processing units. The Feeder
processing unit sends Data objects through the Space to a Processor.
The Space and the Processor are collocated in the same processing unit.
JVM: >= 5.

GENERAL DESCRIPTION:
--------------------

  The project consists of three modules: common, processor and feeder. The common
module includes all the shared resources and classes between both the processor
and the feeder. In our case, the common module includes the "Data" class which
is written and taken from the Space.

  The processor module, which is a processing unit, starts up a Space and on top of
it starts a polling container that performs a take from the Space of unprocessed Data
entries. The take operation results in an "event" that will end up executing the 
"Processor" class. The Processor "processes" the Data object (by setting its processed
flag to true) and returns it. The return value is automatically written back to the Space.
  The processor also comes with both a unit test and integration test that verifies its behavior.

  The feeder module, which is also a processing unit, connects to a Space remotely and
writes unprocessed Data objects to the Space (resulting in events firing up within
the processor processing unit).

BUILDING, PACKAGING, RUNNING, DEPLOYING
---------------------------------------

Quick list:

* mvn compile: Compiles the project.
* mvn os:run: Runs the project.
* mvn test: Runs the tests in the project.
* mvn package: Compiles and packages the project.
* mvn os:run-standalone: Runs a packaged application (from the jars).
* mvn os:deploy: Deploys the project onto the Service Grid.
* mvn os:undeploy: Removes the project from the Service Grid.

  In order to build the example, a simple "mvn compile" executed from the root of the 
project will compile all the different modules.

  Packaging the application can be done using "mvn package" (note, by default, it also
runs the tests, in order to disable it, use -DskipTests). The packaging process jars up 
the common module. The feeder and processor modules packaging process creates a 
"processing unit structure" directory within the target directory called [app-name]-[module].
It also creates a jar from the mentioned directory called [app-name]-[module].jar.

  In order to simply run both the processor and the feeder (after compiling), "mvn os:run" can be used.
This will run a single instance of the processor and a single instance of the feeder within
the same JVM using the compilation level classpath (no need for packaging). 
  A specific module can also be executed by itself, which in this case, executing more than 
one instance of the processing unit can be done. For example, running the processor module with 
a cluster topology of 2 partitions, each with one backup, the following command can be used:
mvn os:run -Dmodule=processor -Dcluster="total_members=2,1".

  In order to run a packaged processing unit, "mvn package os:run-standalone" can be used (if
"mvn package" was already executed, it can be omitted). This operation will run the processing units
using the packaged jar files. Running a specific module with a cluster topology can be executed using:
mvn package os:run-standalone -Dmodule=processor -Dcluster="total_members=2,1".

  Deploying the application requires starting up a GSM and at least 2 GSCs (scripts located under
the bin directory within the GigaSpaces installation). Once started, running "mvn package os:deploy"
will deploy the two processing units. 
  When deploying, the SLA elements within each processing unit descriptor (pu.xml) are taken into 
account. This means that by default when deploying the application, 2 partitions, each with 
one backup will be created for the processor, and a single instance of the feeder will be created.
  A special note regarding groups and deployment: If the GSM and GSCs were started under a specific 
group, the -Dgroups=[group-name] will need to be used in the deploy command.

WORKING WITH ECLIPSE
--------------------

  In order to generate eclipse project the following command need to be executed from the root of
the application: "mvn eclipse:eclipse". Pointing the Eclipse import existing project wizard
to the application root directory will result in importing the three modules.
If this is a fresh Eclipse installation, the M2_REPO needs be defined and pointed to the local 
maven repository (which resides under USER_HOME/.m2/repository).

After generating the projects, configure them to work with maven:
for each project, right click on it and select Configure > Convert to Maven Project.

  The application itself comes with built in launch targets allowing to run the processor and the 
feeder using Eclipse run (or debug) targets.

A NOTE OF CLUSTERING
--------------------

  This application focus on showing how SBA is used. The processor starts up an embedded Space and 
works directly on it. When deploying 2 partitions of the processor, two embedded spaces (within the
same cluster) will be created, with each polling container working only on the cluster member it 
started in an in memory and transactional manner. This is the power of such an architecture, where
the processing of the Data happens in a collocated manner with the Data. If we want to add High
Availability to the processor, we can deploy 2 partitions, each with one backup (2,1). In this 
case, the processor instances that ends up starting a cluster member Space which is the backup
will not perform any processing since the polling container identifies the Space state and won't
perform the take operation. If one of the processor primaries instances will fail, the backup
instance will become primary (with an up to date data), and its polling container will start
processing all the relevant Data. Note, when deploying on top of the Service Grid, the Service
Grid will also identify that one instance failed, and will automatically start it over in another
container (GSC).

  The feeder works with a clustered view of the Space (the 2,1 cluster topology looking as one), and 
simply writes unprocessed Data objects to the Space. The routing (@SpaceRouting) controls to which
partition the unprocessed Data will be written and consequently which instance will process it.

MAVEN PLUGIN WIKI PAGE
---------------------------------

  For more information about the Maven Plugin please refer to:
http://www.gigaspaces.com/wiki/display/XAP91/Maven+Plugin