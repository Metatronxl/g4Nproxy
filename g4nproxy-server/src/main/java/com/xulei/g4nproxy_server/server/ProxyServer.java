package com.xulei.g4nproxy_server.server;

import com.xulei.g4nproxy_protocol.protocol.Constants;
import com.xulei.g4nproxy_protocol.protocol.ProxyMessageDecoder;
import com.xulei.g4nproxy_protocol.protocol.ProxyMessageEncoder;
import com.xulei.g4nproxy_server.handler.NatServerChannelHandler;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;

/**
 * @author lei.X
 * @date 2019/3/18 10:43 AM
 */
@Slf4j
public class ProxyServer {

    /**
     * max packet is 2M.
     */
    private static final int MAX_FRAME_LENGTH = 2 * 1024 * 1024;

    private static final int LENGTH_FIELD_OFFSET = 0;

    private static final int LENGTH_FIELD_LENGTH = 4;

    private static final int INITIAL_BYTES_TO_STRIP = 0;

    private static final int LENGTH_ADJUSTMENT = 0;

    private ServerBootstrap natServerBootStrap;
    private ServerBootstrap userMapServerBootStrap;

    private static ProxyServer instance = new ProxyServer();

    private ProxyServer() {
    }

    public static ProxyServer getInstance() {
        return instance;
    }

    public void start(){
         natServerBootStrap = new ServerBootstrap();
        NioEventLoopGroup serverBossGroup = new NioEventLoopGroup();
        NioEventLoopGroup serverWorkerGroup = new NioEventLoopGroup();
        natServerBootStrap.group(serverBossGroup, serverWorkerGroup).channel(NioServerSocketChannel.class).childHandler(new ChannelInitializer<SocketChannel>() {
            @Override
            public void initChannel(SocketChannel ch) throws Exception {
                ch.pipeline().addLast(new ProxyMessageDecoder(MAX_FRAME_LENGTH, LENGTH_FIELD_OFFSET, LENGTH_FIELD_LENGTH, LENGTH_ADJUSTMENT, INITIAL_BYTES_TO_STRIP));
                ch.pipeline().addLast(new ProxyMessageEncoder());
//                ch.pipeline().addLast(new ServerIdleCheckHandler());
                ch.pipeline().addLast(new NatServerChannelHandler());
            }
        });

        log.info("start netty proxy server, port"+ Constants.g4nproxyServerPort);

        //开启server端的服务
        natServerBootStrap.bind(Constants.g4nproxyServerPort);
    }

}
