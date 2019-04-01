package com.xulei.g4nproxy_server.controller;



import com.xulei.g4nproxy_server.server.AvailablePortManager;
import com.xulei.g4nproxy_server.server.ProxyServer;

import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by virjar on 2019/2/23.
 *
 *   http://127.0.0.1:8080/deviceMapping
 */
@RestController
public class AvailablePortController {

    @GetMapping("portList")
    public String availablePortMapping() {
        return StringUtils.join(ProxyServer.getInstance().mappingPort(), "\r\n");
    }

    @GetMapping("deviceMapping")
    @ResponseBody
    public Object deviceMapping() {
        return AvailablePortManager.getInstance().deviceMapping();
    }
}
