===============================================
=== GigaSpaces RESTFul WebServices Example ===
===============================================

# Overview

This is a WebServices example based on Spring MVC that can run in GigaSpaces Web Container. Example application uses GigaSpaces as a datastore for a list of Persons and demonstrates how to expose this information as RESTFul service.

It is inspired by the Developer Works article here, http://www.ibm.com/developerworks/webservices/library/wa-spring3webserv/index.html.

Example also uses ContentNegotiatingViewResolver which lets you pick the format of the data served by the webservice at run time. It supports HTML/XML or JSON format.

For exposing your GigaSpaces data as RESTFul WebService without any business logic you can use the RESTData project which is hosted here, https://github.com/OpenSpaces/RESTData.

# Project Structure

This example is a maven project and is a typical Spring MVC web application. For simplicity, GigaSpace bean defined in the servlet start the space.
	    
# Build AND Deployment 

The example uses Maven 2 as its build tool. Just follow the standard build lifecycle phases to construct the WAR (gs-rest-example.war).

You can run the example by launching the Service Grid (use gs-agent to start Service grid), and deploying the WAR using the GS-UI or GS CLI.

# Accessing the Web Application

Invoke for html format of the WebService using,
http://127.0.0.1:8080/gs-rest-example/personsearch/ or http://127.0.0.1:8080/gs-rest-example/personsearch/person/1 to see the output.
 
As mentioned above this web service also supports xml and json data formats which you can trigger by passeing the Content type in header. 

If you are using curl you can modify headers using following syntax, <p>
<code>curl -HAccept:application/xml http://127.0.0.1:8080/gs-rest-example/personsearch/</code>

If you use Google Chrome, there is a "Advanced REST client Application" extension available here, https://chrome.google.com/webstore/detail/hgmloofddffdnphfgcellkdfbfbjeloo. This extension also lets you pass additional parameters in the headers (and content type). For e.g., pass "Accept: application/xml" to get the data in xml format. This chrome extension also has other features (save requests) that make it really easy to access and test your RESTFul web services.

# Other options for RESTFul WebServices

There are many java frameworks that can be used to create WebServices (SOAP/REST). Apache CXF and Spring-WS are among some of the popular ones. GigaSpaces Web Service PU best practice page shows and example implementation using CXF, http://www.gigaspaces.com/wiki/display/SBP/Web+Service+PU.

The differences between each of these frameworks is subjective and depends on end users circumstance and preferences. 
There is Stack Overflow discussion regarding this and provides some good guidance, http://stackoverflow.com/questions/297033/which-framework-is-better-cxf-or-spring-ws.
