==================
manageGrid project
==================

manageGrid project is an implementation of the grid life cycle steps using GigaSpaces Admin API. Typical cluster management involves following lifecycle steps and manageGrid implements these steps,
* start 	 – Start Grid Infrastructure on all the machines and deploy the services
*	stop 	   – Undeploy the services and stop the Grid infrastructure on all the machines
*	reStart  – Stop and Start the Grid infrastructure
*	startOne – Start Grid infrastructure on one machine
*	stopOne  – Stop Grid infrastructure on one machine

## Usage

manageGrid can be used to manage the Grid Infrastructure including GSM's and GSC's.  
It expects the agents to be already started on the machines that are part of the grid. Atleast one of the Lookup Servers should be started along with the agent.

## Configuration

manageGrid tool is designed so that you can easily reuse it across any environment by passing a different configuration file as input. Expected component information is passed using an input configuration file (properties file syntax).
Typical parameters passed in this file include,
* `machines` - list of machine ips or hostnames comma separated  
     example `machines=127.0.0.1,192.168.56.1`
* `deployermachines` - list of machine ips where deploy should be invoked   
     example `deployermachines=127.0.0.1`
* `<machineip>.commonjvmargs` - java arguments used for all components on this machine,    
     example `192.168.0.1.commonjvmargs=-Dcom.gs.multicast.enabled=false`  
* `<machineip>.gsm` - number of gsm's to start,   
 		example `192.168.0.1.gsm=1`   
       above will make GridManager start 1 gsm on host with 192.168.0.1
* `<machineip>.gsm.jvmargs` - java arguments used for gsm's on this machine,  
 		example `192.168.0.1.gsm.jvmargs=-Xmx256M -Xms256M`  
* `<machineip>.gsc` - number of gsc to start,   
 		example `192.168.0.1.gsc=2`  
       above will make GridManager start 2 gsc's on host with 192.168.0.1   
* `<machineip>.gsc.jvmargs` - java arguments used for gsc's on this machine,   
 		example `192.168.0.1.gsc.jvmargs=-Xmx256M -Xms256M`  
* `pulist` - list of processing units to be deployed. They will be deployed in the order of left to right  
 		example `pulist=mainApp,dependencyApp`  
       In the above example GridManager will deploy mainApp first and dependencyApp next 
* `<appName>.deployment` - deployment package location of <appName> (can be jar/zip)  
       example `processor.deployment=C:/helloworld/helloworld-processor.jar`  
* `<appName>.deployment.options` - <optional> deployment options for <appName>. `-sla/-cluster/-user/-password/-secured` are the supported options
 		Syntax of the options is same as GigaSpaces deploy command described here, http://www.gigaspaces.com/wiki/display/XAP9/deploy+-+GigaSpaces+CLI  
       examples  
			 `processor.deployment.options=-sla C:/helloworld/processor/sla.xml`  
			 `processor.deployment.options=-cluster schema=partitioned-sync2backup total_members=1,1`  

## Example
Project includes an example configuration for GigaSapces maven basic application deployed across a two node cluster. 
* maven basic application is very similar to the helloworld example that comes with GigaSpaces and includes two processing units, a processor and a feeder.  
* Example folder also includes the scripts for starting the agents on two machines. Each of these agents also starts the Lookup Service.  
* Example scripts for each of the grid management lifecylce are also included.  




