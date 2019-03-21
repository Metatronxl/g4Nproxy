package com.xulei.g4nproxy_client.util;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

/**
 * @author lei.X
 * @date 2019/3/21 2:51 PM
 */
public class BootStrapFactory {


    private final Bootstrap bootStrap;


    public BootStrapFactory() {
        bootStrap = new Bootstrap()
                .group(new NioEventLoopGroup())
                .channel(NioSocketChannel.class)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 20000);
    }

    public Bootstrap build(){return bootStrap.clone();}
}
