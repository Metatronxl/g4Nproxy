package com.xulei.g4nproxy_server;

import com.xulei.g4nproxy_server.server.ProxyServer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.server.WebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

import java.io.InputStream;

/**
 * @author lei.X
 * @date 2019/3/18 10:43 AM
 */
@SpringBootApplication
class G4nProxyServerApplication extends SpringBootServletInitializer{

    public static void main(String[] args) {
        InputStream resourceAsStream = G4nProxyServerApplication.class.getClassLoader().getResourceAsStream("application.properties");
        System.out.println(resourceAsStream);
        ProxyServer.getInstance().start();

        SpringApplication.run(G4nProxyServerApplication.class, args);

    }



    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        return builder.sources(G4nProxyServerApplication.class);
    }
}
