Overview

There are 3 main packages in the app (components/client/common).

The common contains the model objects and the service interfaces
The components has 2 elements - a polling container and a remote service
The client currently has 2 apps - MarketDataFeeder and MarketDataSearch.
**** In the client files - MarketDataFeeder and MarketDataSearch, you may need to specify ?groups=local in the URL if you changed your default

Flow
The feeder sends MarketDataEvent objects to the remote service - these are routed by symbol-exchange.  The remote service - MarketDataServiceImpl 
will save the object to the space (the partitioned space).
These objects are picked up by the polling container - MarketDataEventProcessor - in a batch read

For each event, the system will look to see if any one of the specified types ("minute","day",...) exist for that symbol/exchange/trade time combo
If it does, it will update the existing object.  If not, it will create a new one.

To build:
mvn clean package

Deploy
Start default gs-agent.bat

%GS_HOME%\bin\gs deploy -cluster schema=partitioned-sync2backup total_members=2,1 /workspace/timeseries/biz/components/.target/timeseries-components.jar

To run feeder/search
First run %GS_HOME%/bin/setenv
Go to your workspace/timeseries/client/.target/timeseries-client dir

run the following
java -cp .;lib/*;%GS_JARS% org.openspaces.timeseries.client.MarketDataFeeder
java -cp .;lib/*;%GS_JARS% org.openspaces.timeseries.client.MarketDataSearch IBM NASDAQ minute

