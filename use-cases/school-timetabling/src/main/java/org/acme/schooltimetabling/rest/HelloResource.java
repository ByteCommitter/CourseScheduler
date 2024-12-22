package org.acme.schooltimetabling.rest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@Path("/hello")
@Tag(name = "Hello Resource", description = "hello endpoint")
public class HelloResource {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Returns a hello message", description = "A simple endpoint to test the API")
    public String hello() {
        return "{\"message\":\"hello\"}";
    }
}
