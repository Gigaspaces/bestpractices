<!doctype html>

<head>
	<title>Time Series Demo</title>

	<link type="text/css" rel="stylesheet" href="http://ajax.googleapis.com/ajax/libs/jqueryui/1.8/themes/base/jquery-ui.css">
	<link type="text/css" rel="stylesheet" href="css/graph.css">
	<link type="text/css" rel="stylesheet" href="css/detail.css">
	<link type="text/css" rel="stylesheet" href="css/legend.css">
	<link type="text/css" rel="stylesheet" href="css/extensions.css?v=2">
	
	<script src="https://ajax.googleapis.com/ajax/libs/jquery/1.6.2/jquery.min.js"></script>
	<script src="https://ajax.googleapis.com/ajax/libs/jqueryui/1.8.15/jquery-ui.min.js"></script>
	
	<script src="js/d3/d3.v3.js"></script>
	
	<script src="js/rickshaw/Rickshaw.js"></script>
	<script src="js/rickshaw/Rickshaw.Class.js"></script>
	<script src="js/rickshaw/Rickshaw.Compat.ClassList.js"></script>
	<script src="js/rickshaw/Rickshaw.Graph.js"></script>
	<script src="js/rickshaw/Rickshaw.Graph.Renderer.js"></script>
	<script src="js/rickshaw/Rickshaw.Graph.Renderer.Area.js"></script>
	<script src="js/rickshaw/Rickshaw.Graph.Renderer.Line.js"></script>
	<script src="js/rickshaw/Rickshaw.Graph.Renderer.Bar.js"></script>
	<script src="js/rickshaw/Rickshaw.Graph.Renderer.ScatterPlot.js"></script>
	<script src="js/rickshaw/Rickshaw.Graph.Renderer.Stack.js"></script>
	<script src="js/rickshaw/Rickshaw.Graph.RangeSlider.js"></script>
	<script src="js/rickshaw/Rickshaw.Graph.RangeSlider.Preview.js"></script>
	<script src="js/rickshaw/Rickshaw.Graph.HoverDetail.js"></script>
	<script src="js/rickshaw/Rickshaw.Graph.Annotate.js"></script>
	<script src="js/rickshaw/Rickshaw.Graph.Legend.js"></script>
	<script src="js/rickshaw/Rickshaw.Graph.Axis.Time.js"></script>
	<script src="js/rickshaw/Rickshaw.Graph.Behavior.Series.Toggle.js"></script>
	<script src="js/rickshaw/Rickshaw.Graph.Behavior.Series.Order.js"></script>
	<script src="js/rickshaw/Rickshaw.Graph.Behavior.Series.Highlight.js"></script>
	<script src="js/rickshaw/Rickshaw.Graph.Smoother.js"></script>
	<script src="js/rickshaw/Rickshaw.Fixtures.Time.js"></script>
	<script src="js/rickshaw/Rickshaw.Fixtures.Time.Local.js"></script>
	<script src="js/rickshaw/Rickshaw.Fixtures.Number.js"></script>
	<script src="js/rickshaw/Rickshaw.Fixtures.RandomData.js"></script>
	<script src="js/rickshaw/Rickshaw.Fixtures.Color.js"></script>
	<script src="js/rickshaw/Rickshaw.Color.Palette.js"></script>
	<script src="js/rickshaw/Rickshaw.Graph.Axis.Y.js"></script>
	
	<script src="js/extensions.js"></script>
	<script src="js/TimeSeriesContainer.js"></script>
	<script src="js/TimeSeriesServletUtils.js"></script>
	
</head>

<body>
<div id="content">
        <form id="side_panel">
                <img src="images/airplane.png" alt="Airplane Image" width="250" height="225">
                <section><div id="legend"></div></section>
                <section>
                        <div id="renderer_form" class="toggler">
                                <input type="radio" name="renderer" id="line" value="line" checked>
                                <label for="line">line</label>
                                <input type="radio" name="renderer" id="area" value="area">
                                <label for="area">area</label>
                                <input type="radio" name="renderer" id="bar" value="bar">
                                <label for="bar">bar</label>
                                <input type="radio" name="renderer" id="scatter" value="scatterplot">
                                <label for="scatter">scatter</label>
                        </div>
                </section>
                <section>
                        <div id="offset_form">
                                <label for="value">
                                        <input type="radio" name="offset" id="value" value="value" checked>
                                        <span>value</span>
                                </label>
                                <label for="stack">
                                        <input type="radio" name="offset" id="stack" value="zero">
                                        <span>stack</span>
                                </label>
                                <label for="stream">
                                        <input type="radio" name="offset" id="stream" value="wiggle">
                                        <span>stream</span>
                                </label>
                                <label for="pct">
                                        <input type="radio" name="offset" id="pct" value="expand">
                                        <span>pct</span>
                                </label>
                        </div>
                        <div id="interpolation_form">
                                <label for="cardinal">
                                        <input type="radio" name="interpolation" id="cardinal" value="cardinal" checked>
                                        <span>cardinal</span>
                                </label>
                                <label for="linear">
                                        <input type="radio" name="interpolation" id="linear" value="linear">
                                        <span>linear</span>
                                </label>
                                <label for="step">
                                        <input type="radio" name="interpolation" id="step" value="step-after">
                                        <span>step</span>
                                </label>
                        </div>
                </section>
        </form>

        <div id="chart_container">
        		<h1>Flight Operations Dashboard</h1>
                <div id="chart"></div>
                <div id="timeline"></div>
                <div id="preview"></div>
        </div>

</div>

<script>

	var seriesData = [ [], [], [], [], [] ];
	var seriesLength = seriesData.length;
	
	var sourceDestinationArray = sendRequestToServletForAllSourceDestinations("getAllIntervals", "AA", 0);
	var lastInterval = loadDataIntoTimeSeriesGraph(sourceDestinationArray, seriesData);
	
	var palette = new Rickshaw.Color.Palette( { scheme: 'classic9' } );
	
	//Instantiate the graph
	var graph = new Rickshaw.Graph( {
	        element: document.getElementById("chart"),
	        width: 900,
	        height: 500,
	        renderer: 'line',
	        stroke: true,
	        preserve: true,
	        series: [
	                {
	                        color: palette.color(),
	                        data: seriesData[0],
	                        name: 'FAI-JFK'
	                }, {
	                        color: palette.color(),
	                        data: seriesData[1],
	                        name: 'BHM-DHN'
	                }, {
	                        color: palette.color(),
	                        data: seriesData[2],
	                        name: 'HSV-MOB'
	                }, {
	                        color: palette.color(),
	                        data: seriesData[3],
	                        name: 'MGM-ANI'
	                }, {
	                        color: palette.color(),
	                        data: seriesData[4],
	                        name: 'MRI-ENM'
	                }
	        ]
	} );
	
	graph.render();
	
	var preview = new Rickshaw.Graph.RangeSlider.Preview( {
	        graph: graph,
	        element: document.getElementById('preview'),
	} );
	
	var hoverDetail = new Rickshaw.Graph.HoverDetail( {
	        graph: graph,
	        xFormatter: function(x) {
	                return new Date(x * 1000).toString();
	        }
	} );
	
	var legend = new Rickshaw.Graph.Legend( {
	        graph: graph,
	        element: document.getElementById('legend')
	
	} );
	
	var shelving = new Rickshaw.Graph.Behavior.Series.Toggle( {
	        graph: graph,
	        legend: legend
	} );
	
	var order = new Rickshaw.Graph.Behavior.Series.Order( {
	        graph: graph,
	        legend: legend
	} );
	
	var highlighter = new Rickshaw.Graph.Behavior.Series.Highlight( {
	        graph: graph,
	        legend: legend
	} );
	
	var smoother = new Rickshaw.Graph.Smoother( {
	        graph: graph,
	        element: $('#smoother')
	} );
	
	var ticksTreatment = 'glow';
	
	var xAxis = new Rickshaw.Graph.Axis.Time( {
	        graph: graph,
	        ticksTreatment: ticksTreatment,
	        timeFixture: new Rickshaw.Fixtures.Time.Local()
	} );
	
	xAxis.render();
	
	var yAxis = new Rickshaw.Graph.Axis.Y( {
	        graph: graph,
	        tickFormat: Rickshaw.Fixtures.Number.formatKMBT,
	        ticksTreatment: ticksTreatment
	} );
	
	yAxis.render();
	
	var controls = new RenderControls( {
	        element: document.querySelector('form'),
	        graph: graph
	} );
	
	//Update the graph every 2 seconds as new intervals become available in the space
	setInterval( function() {
        
		for(i=0; i < seriesLength; i++) {
        	seriesData[i].shift(); //Remove earliest interval displayed on the graph
        }
        
		var latestSourceDestinationArray = sendRequestToServletForAllSourceDestinations("getNextInterval", "AA", lastInterval);
        lastInterval = loadDataIntoTimeSeriesGraph(latestSourceDestinationArray, seriesData);

        graph.update();
	
	}, 2000 ); //Make sure this is offset greater than the timeSeriesInterval by at least 200ms to account for data availability
	
	var previewXAxis = new Rickshaw.Graph.Axis.Time( {
        graph: preview.previews[0],
        timeFixture: new Rickshaw.Fixtures.Time.Local(),
        ticksTreatment: ticksTreatment
	});
	
	previewXAxis.render();

</script>

</body>