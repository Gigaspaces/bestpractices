package com.mycompany.app.web.servlet;

import java.io.IOException;
import java.util.SortedMap;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.util.Assert;

import com.mycompany.app.common.utils.StringUtils;
import com.mycompany.app.common.vo.Airline;
import com.mycompany.app.common.vo.Interval;
import com.mycompany.app.common.vo.SourceDestinationAirports;
import com.mycompany.app.web.delegate.TaskDelegate;


public class TimeSeriesServlet extends HttpServlet {
    private Logger log = Logger.getLogger(this.getClass().getName());

	private static final long serialVersionUID = -3774809284043159067L;
	
	private TaskDelegate taskDelegate;
	
	@Override
	public void init() throws ServletException { 
		taskDelegate = (TaskDelegate) getServletContext().getAttribute("taskDelegate");
		if(taskDelegate == null) {
			throw new IllegalStateException("taskDelegate cannot be null"); 
		}
	}
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doPost(request, response);
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Assert.notNull(taskDelegate, "**** taskDelegate is a required property ****");
		
        String service = StringUtils.trim(request.getParameter("service"));
        String airline = StringUtils.trim(request.getParameter("airline"));
        Integer lastInterval = StringUtils.stringToInteger(request.getParameter("lastInterval"));
        
        SortedMap<String, SourceDestinationAirports> sourceDestinationMap = null;
        if("getAllIntervals".equals(service)) {
        	sourceDestinationMap = taskDelegate.retrieveCompletedBookingTimeSeriesIntervals(airline, 12);

        }else if("getNextInterval".equals(service)) {
        	sourceDestinationMap = taskDelegate.retrieveNextActiveBookingTimeSeries(airline, lastInterval);
        }
        
        if(sourceDestinationMap != null) {
        	response.setContentType("text/html");
            response.getWriter().println(createXmlResponsePayload(sourceDestinationMap));
        }
	}
	
	private String createXmlResponsePayload(SortedMap<String, SourceDestinationAirports> sourceDestinationMap) {
		StringBuffer sb = new StringBuffer();		
		
		sb.append("<timeSeries>");
		for(String sourceDestination : sourceDestinationMap.keySet()) {
			sb.append("<sourceDestination>");
			
			SourceDestinationAirports airports = sourceDestinationMap.get(sourceDestination);
			sb.append("<id>") .append(airports.getId()) .append("</id>");			
			
			SortedMap<String, Airline> airlines = airports.getAirlines();
			for(String airlineName : airlines.keySet()) {
				Airline airline = airlines.get(airlineName);
				sb.append("<airline>");
				sb.append("<airlineName>") .append(airlineName) .append("</airlineName>");
				
				SortedMap<Integer, Interval> intervals = airline.getIntervals();
				for(Integer intervalNo : intervals.keySet()) {
					Interval interval = intervals.get(intervalNo);
					sb.append("<interval>");
					sb.append("<intervalNo>") .append(intervalNo) .append("</intervalNo>");
					sb.append("<count>") .append(interval.getCount()) .append("</count>");
					sb.append("</interval>");
				}
				sb.append("</airline>");
			}
			sb.append("</sourceDestination>");
		}
		sb.append("</timeSeries>");
		
		return sb.toString();
	}

}