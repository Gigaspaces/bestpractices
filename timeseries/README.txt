Time series demo

Current iteration includes:

* Tick generator
* Analytics generators
* UI
* Cassandra mirror
* Maven project structure

OVERALL STATE OF PROJECT

It's still in an alpha state.  Some of near future tasks to be done:

* Cloudify it
* More UI work. Graph still sucks, and more controls might be nice.
* Maven dependencies could be cleaned up (some may be unneeded)
* Code commentary, refactoring
* External tick generator (MQ based)
* Clustered testing.  Current testing done on one box.



HOWTO

Building:

Some of the Maven dependencies are not available in the default repo, so
must be added to the build repo.  gs-openspaces and gs-runtime are
two examples.  

The top level pom can drive everything.  If you want to mod the ui, you'll
need to do a GWT compile ( I always did this from eclipse).

The basic dependency structure is:

model
services
	components
	timeseries-mirror-pu
	timeseries-pu
	timeseries-web


Running:

Get Cassandra running.  The mirror has a url that can be configured, but
the value included works with the basic Cassandra install (unzip and run).

Load the schema -

	cassandra-cli.bat -host localhost -port 9160 -f cassandra\ticks-schema.txt

Deploy timeseries-pu
Deploy timeseries-mirror-pu
Deploy timeseries-web war (takes awhile)

Access the deployed war url, enter a symbol, a price basis of 5, min and max volumes something like 5,200 and press the start button.

The tick count in the space is displayed in the ui.
Synthetic tick values (e.g. vwap) are graphed in the chart (too slowly)
You can see the mirror writing the ticks in the background to Cassandra

Final NOTES:

* The space is not configured to load from the Cassandra EDS
* I tweaked the EDS for this application (it's not entirely generic)
* The EDS uses XStream to serialize embbeded objects as XML in Cassandra columns.  I'd probably use binary protobufs in the real world (or some other efficient serialization)
* The UI still sucks.  It needs legends, and needs to be made faster.  FYI the bottom graph shows trade volume (also an application analytic) but it's not being scaled properly yet.
* I'm hoping this could be a starting point for other directions, and I'll be continuing to push to this project.

def



