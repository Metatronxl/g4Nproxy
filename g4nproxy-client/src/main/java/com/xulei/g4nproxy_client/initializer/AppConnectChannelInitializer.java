package com.xulei.g4nproxy_client.initializer;


import com.xulei.g4nproxy_client.handler.HttpConnectHandler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;

/**
 * @author lei.X
 * @date 2019/3/25 4:48 PM
 */
public class AppConnectChannelInitializer extends ChannelInitializer<SocketChannel> {

    /**
     * 与客户端连接的处理器(ProxyServerHandler)中的ctx,
     * 用于将目标主机响应的消息 发送回 客户端
     *
     * 此处将其传给http连接对应的处理器类
     */
    private ChannelHandlerContext ctx;


    public AppConnectChannelInitializer(ChannelHandlerContext ctx){
        this.ctx = ctx;
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ch.pipeline()
                .addLast(new HttpClientCodec())
                .addLast(new HttpObjectAggregator(Integer.MAX_VALUE))
                .addLast(new HttpConnectHandler(ctx));
    }
}
