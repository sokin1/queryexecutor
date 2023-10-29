package com.grepr.apiserver.resources;

import javax.ws.rs.Path;
import javax.ws.rs.GET;

@Path("/hello")
public class HelloWorldResource {
    @GET
    public String get() {
        return "Hello World";
    }
}
