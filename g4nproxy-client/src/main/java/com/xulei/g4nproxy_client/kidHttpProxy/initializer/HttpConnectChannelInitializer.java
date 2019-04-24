package com.xulei.g4nproxy_client.kidHttpProxy.initializer;

import com.xulei.g4nproxy_client.kidHttpProxy.handler.HttpConnectHandler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.stream.ChunkedWriteHandler;
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
     * <p>
     * 此处将其传给http连接对应的处理器类
     */
    private ChannelHandlerContext ctx;

    public HttpConnectChannelInitializer(ChannelHandlerContext ctx) {
        this.ctx = ctx;
    }


    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ch.pipeline()
                .addLast(new HttpClientCodec())
                //TODO
//                .addLast(new HttpObjectAggregator(10 * 1024 * 1024))
//        支持异步发送过大数据流情况，不占用过多内存，防止JAVA内存溢出的问题
//                .addLast(new ChunkedWriteHandler())
                .addLast(new HttpConnectHandler(ctx));
    }

}
