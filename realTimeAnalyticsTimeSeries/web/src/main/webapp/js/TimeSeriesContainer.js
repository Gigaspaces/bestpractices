var SourceDestinationContainer = function(args) {

	this.sourceDestination = parseInt(args.sourceDestination);
	this.intervalContainerArray = args.intervalContainerArray;
	
};

var IntervalContainer = function(args) {

	this.interval = parseInt(args.interval);
	this.airline = args.airline;
	this.count = parseInt(args.count);
	
};