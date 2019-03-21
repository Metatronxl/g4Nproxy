package com.xulei.g4nproxy_client.initializer;

import com.xulei.g4nproxy_client.handler.HttpsConnectHandler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;

/**
 * @author lei.X
 * @date 2018/11/7
 */
public class HttpsConnectChannelInitializer extends ChannelInitializer<SocketChannel> {


    /**
     * 与客户端连接的处理器(ProxyServerHandler)中的ctx,
     * 用于将目标主机响应的消息 发送回 客户端
     *
     * 此处将其传给http连接对应的处理器类
     */
    private final ChannelHandlerContext ctx;

    public HttpsConnectChannelInitializer(ChannelHandlerContext ctx) {
        this.ctx = ctx;
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        //https请求无法解析,不做任何编解码操作
        //自定义处理器
        ch.pipeline().addLast(new HttpsConnectHandler(ctx));
    }
}
