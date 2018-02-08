package it.automator.servlets;

import java.util.stream.Collectors;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;

import org.apache.log4j.Logger;

import it.automator.mapper.servlets.ServlerResponseObject;
import it.automator.mapper.servlets.ServletRequestObject;


@Path("tasks")
public class TaskInitiatorRestApi {
	
	public TaskInitiatorRestApi() {
		log = Logger.getLogger(TaskInitiatorRestApi.class);
	}

	@POST
	@Path("exec")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public ServlerResponseObject taskExec(ServletRequestObject req, @Context HttpHeaders headers) {
		MultivaluedMap<String, String> rh = headers.getRequestHeaders();
	    String str = rh.entrySet()
	                     .stream()
	                     .map(e -> e.getKey() + " = " + e.getValue())
	                     .collect(Collectors.joining("\n"));
	    
	    System.out.println(str);
	      
		log.debug(String.format("Request [type] %s", req.getType()));
		log.debug(String.format("Request [type] %d", req.getHostid()));
		log.debug(String.format("Request [type] %s", req.getHostname()));
		log.debug(String.format("Request [type] %d", req.getCommand()));
		
		return null;
	}
	
	
	private Logger log = null;
}
