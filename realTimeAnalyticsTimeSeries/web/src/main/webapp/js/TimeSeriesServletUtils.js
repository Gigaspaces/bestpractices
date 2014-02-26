function generateHttpObjectBasedOnBrowser() {
	try {return new XMLHttpRequest();}
	catch (error) {}
  
	try {return new ActiveXObject("Msxml2.XMLHTTP");}
	catch (error) {}
  
	try {return new ActiveXObject("Microsoft.XMLHTTP");}
	catch (error) {}

	throw new Error("Could not create HTTP request object.");
}

function sendRequestToServletForAllSourceDestinations(service, airline, lastInterval) {
	var request = generateHttpObjectBasedOnBrowser();
	
	request.open("GET", "TimeSeriesServlet?service= " + service + "&airline=" + airline + "&lastInterval=" + lastInterval, false);
	request.send(null);
	
	var sourceDestinationArray = new Array();
	var outerCounter = 0;
	
	if(request.readyState == 4) {
    	if(request.status == 200) {	
    		var dom = (new DOMParser()).parseFromString(request.responseText, "text/xml");
    		
    		var sourceDestinationTags = dom.getElementsByTagName("sourceDestination");
        	for(i=0; i < sourceDestinationTags.length; i++) {
        		
	        	var sourceDestinationIdTag = sourceDestinationTags[i].getElementsByTagName("id");
	        	var sourceDestinationValueTag = sourceDestinationIdTag[0].childNodes[0].nodeValue;
	        	var intervalArray = new Array();
	        	var innerCounter = 0;
	        	
	        	var airlineTags = sourceDestinationTags[i].getElementsByTagName("airline");
	        	for(j=0; j < airlineTags.length; j++) {
	        		
	        		var airlineNameTag = airlineTags[j].getElementsByTagName("airlineName");
	            	var airlineValueTag = airlineNameTag[0].childNodes[0].nodeValue;
	            	
	            	var intervalTags = airlineTags[j].getElementsByTagName("interval");
	            	for(k=0; k < intervalTags.length; k++) {
	            		
	            		var intervalNoTag = intervalTags[k].getElementsByTagName("intervalNo");
	            		var countTag = intervalTags[k].getElementsByTagName("count");
	            			            		
	            		var interval = new IntervalContainer( {
	            	        interval: intervalNoTag[0].childNodes[0].nodeValue,
	            	        airline: airlineValueTag,
	            	        count: countTag[0].childNodes[0].nodeValue
	            		} );
	            		intervalArray[innerCounter++] = interval;
	            	}
	        	}
	        	var sourceDestinationContainer = new SourceDestinationContainer( {
	        		sourceDestination: sourceDestinationValueTag,
	        		intervalContainerArray: intervalArray
        		} );
	        	sourceDestinationArray[outerCounter++] = sourceDestinationContainer;
        	}
    	}else {
    		alert(request.status + request.statusText);
		}
	}
	return sourceDestinationArray;
}

function loadDataIntoTimeSeriesGraph(sourceDestinationArray, series, interval) {
		
	for(i=0; i < sourceDestinationArray.length; i++) {
		var sourceDestinationId = sourceDestinationArray[i].sourceDestination;

		var intervalContainerArray = sourceDestinationArray[i].intervalContainerArray;
		for(j=0; j < intervalContainerArray.length; j++) {
			if("AA" == intervalContainerArray[j].airline) {
				series[sourceDestinationId].push( { x: intervalContainerArray[j].interval * 3600, y: intervalContainerArray[j].count } );
				interval = intervalContainerArray[j].interval;
			}
		}
	}
	
	return interval;
}