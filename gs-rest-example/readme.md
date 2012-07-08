===============================================
=== GigaSpaces RESTFul WebServices Example ===
===============================================

<h1> 1. Overview </h1>

This is a WebServices example based on Spring MVC that can run in GigaSpaces Web Container. App stores a list of Persons and shows how to expose this information as RESTFul service.

It is inspired by the Developer Works article here, http://www.ibm.com/developerworks/webservices/library/wa-spring3webserv/index.html.

Example also uses ContentNegotiatingViewResolver which lets you pick the format of the data served by the webservice at run time. It supports HTML/XML or JSON format.

<h1> 2. Project Structure </h1> 

This example is a maven project and is a typical Spring MVC web application. For simplicity, GigaSpace bean defined in the servlet start the space.
	    
<h1> 3. Build AND Deployment </h1>

The example uses Maven 2 as its build tool. Just follow the standard build lifecycle phases to construct the WAR (gs-rest-example.war).

You can run the example by launching the Service Grid (use gs-agent to start Service grid), and deploying the WAR using the GS-UI or GS CLI.

<h1> 4. Accessing the Web Application </h1>

Invoke for html format of the WebService using,
http://127.0.0.1:8080/gs-rest-example/personsearch/ or http://127.0.0.1:8080/gs-rest-example/personsearch/person/1 to see the output.
 
As mentioned above this web service also supports xml and json data formats which you can trigger by passeing the Content type in header. 

If you are using curl you can modify headers using following syntax, <p>
<code>curl -HAccept:application/xml http://127.0.0.1:8080/gs-rest-example/personsearch/</code>

If you use Google Chrome, there is a "Advanced REST client Application" extension available here, https://chrome.google.com/webstore/detail/hgmloofddffdnphfgcellkdfbfbjeloo. This extension also lets you pass additional parameters in the headers (and content type). For e.g., pass "Accept: application/xml" to get the data in xml format. This chrome extension also has other features (save requests) that make it really easy to access and test your RESTFul web services.
