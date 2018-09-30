package com.mgatelabs.piper.server;

import org.glassfish.jersey.server.ResourceConfig;
import org.springframework.stereotype.Component;

import javax.ws.rs.ApplicationPath;

/**
 *
 * Created by @mgatelabs (Michael Fuller) on 2/14/2018
 */

@Component
@ApplicationPath("/")
public class JerseyConfig extends ResourceConfig {
    public JerseyConfig() {
        registerEndpoints();
    }
    private void registerEndpoints() {
        register(WebResource.class);
    }
}
