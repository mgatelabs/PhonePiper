package com.mgatelabs.ffbe.server;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.awt.*;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by @mgatelabs (Michael Fuller) on 2/13/2018.
 */
@RestController
@RequestMapping("/ffbe")
public class WebResource {

    @RequestMapping(value = "/control", method = RequestMethod.GET, produces = MediaType.TEXT_HTML_VALUE)
    public String control() {

        URL url = Resources.getResource("pages/index.html");

        try {
            return Resources.toString(url, Charsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }

    @RequestMapping(value = "/test", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public String test() {
        return "Hello";
    }

}
