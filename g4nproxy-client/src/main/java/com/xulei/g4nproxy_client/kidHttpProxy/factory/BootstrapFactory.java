package com.xulei.g4nproxy_client.kidHttpProxy.factory;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

/**
 * @author lei.X
 * @date 2019/4/14 6:25 PM
 */
public class BootstrapFactory {

    private Bootstrap bootstrap;


    private static BootstrapFactory instance = new BootstrapFactory();


    private BootstrapFactory() {
    }

    public static BootstrapFactory getInstance() {
        return instance;
    }

    public Bootstrap build(){
        this.bootstrap = new Bootstrap()
                .group(new NioEventLoopGroup())
                .channel(NioSocketChannel.class)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 3000);

        return bootstrap;
    }




}
