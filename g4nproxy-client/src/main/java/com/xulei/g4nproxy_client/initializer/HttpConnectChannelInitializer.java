package com.xulei.g4nproxy_client.initializer;

import com.xulei.g4nproxy_client.handler.HttpConnectHandler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;


/**
 * @author lei.X
 * @date 2018/11/7
 */
@NoArgsConstructor
@Slf4j
public class HttpConnectChannelInitializer extends ChannelInitializer<SocketChannel> {

    /**
     * 与客户端连接的处理器(ProxyServerHandler)中的ctx,
     * 用于将目标主机响应的消息 发送回 客户端
     *
     * 此处将其传给http连接对应的处理器类
     */
    private ChannelHandlerContext ctx;

    public HttpConnectChannelInitializer(ChannelHandlerContext ctx) {
        this.ctx = ctx;
    }


    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ch.pipeline()
                .addLast(new HttpServerCodec())
                .addLast(new HttpObjectAggregator(Integer.MAX_VALUE))
                .addLast(new HttpConnectHandler(ctx));

    }

//    public void init(ProxyConfig proxyConfig){
//        HttpConnectChannelInitializer.proxyConfig = proxyConfig;
//    }
}
